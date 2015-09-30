/*
 * Copyright 2013 Websquared, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fastcatsearch.ir.io;

import org.fastcatsearch.al.HashFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 대용량의 hashset 처리를 위해서 파일기반으로 hashset를 처리하는 클래스.
 * key는 고정길이 String이다.
 *
 * */
public class FileBaseHashSet {
	private static final Logger logger = LoggerFactory.getLogger(FileBaseHashSet.class);

	private static final HashFunctions hfunc = HashFunctions.RSHash;

    private File f;

	private int bucketSize;
	private int count;

    private RandomAccessFile raf;

    private long bucketSizeOffset = 0L;
    private long keySizeOffset = 4L;
    private long countOffset = 8L;
    private long bucketOffset = 12L;
    private long nextOffset;
    private long keyPosOffset;
    private long keyArrayOffset;

    private static final long HEADER_LENGTH = 12L;
    private static int KEY_WIDTH;
    private static final int BUCKET_WIDTH = 4;
    private static final int NEXT_WIDTH = 4;
    private static final int KEYPOS_WIDTH = 8;

    private int SEGMENT_ENTRY_SIZE;
    private int BUCKET_LENGTH;
    private int SEGMENT_WIDTH;
    private int limit;

    /**
     * 기존파일 오픈
     * */
    public FileBaseHashSet(File f) throws IOException {
        raf = new RandomAccessFile(f, "rw");
        load();
    }
    public void load() throws IOException {
        int bucketSize = raf.readInt();
        int keySize = raf.readInt();
        int count = raf.readInt();
        init(bucketSize, keySize, count);
    }
    /**
     * 새로 생성
     * */
	public FileBaseHashSet(File f, int bucketSize, int keySize){
        if(f.exists()) {
            f.delete();
        }

        try {
            f.createNewFile();
            raf = new RandomAccessFile(f, "rw");

            int count = 0;
            init(bucketSize, keySize, count);
            raf.seek(0);
            raf.writeInt(bucketSize);
            raf.writeInt(keySize);
            raf.writeInt(count);
            spanFileToSegment(0);

            raf.seek(12);
            int tmp = raf.readUnsignedShort();

            logger.debug("tmp = {}", tmp);
        } catch (IOException e) {
            logger.error("", e);
        }
	}

    public void init(int bucketSize, int keySize, int count){
        this.bucketSize = bucketSize;
        SEGMENT_ENTRY_SIZE = bucketSize;
        KEY_WIDTH = keySize;
        nextOffset = bucketOffset + BUCKET_WIDTH * SEGMENT_ENTRY_SIZE;
        keyPosOffset = nextOffset + NEXT_WIDTH * SEGMENT_ENTRY_SIZE;
        keyArrayOffset = keyPosOffset + KEYPOS_WIDTH * SEGMENT_ENTRY_SIZE;

        BUCKET_LENGTH = BUCKET_WIDTH * SEGMENT_ENTRY_SIZE;
        SEGMENT_WIDTH = (NEXT_WIDTH + KEYPOS_WIDTH + KEY_WIDTH) * SEGMENT_ENTRY_SIZE;
        this.count = count;
        this.limit = (count / SEGMENT_ENTRY_SIZE + 1) * SEGMENT_ENTRY_SIZE; //엔트리 제한 갯수. 이 수치를 넘어서면 파일길이를 늘린다.
    }

    private void spanFileToSegment(int segment) {
        try {
            raf.setLength(HEADER_LENGTH + BUCKET_LENGTH + segment + 1 * SEGMENT_WIDTH);
        } catch (IOException e) {
            logger.error("", e);
        }
    }



//	public void save(File file) throws IRException{
//		try {
//			IndexOutput output = new BufferedFileOutput(file);
//			output.writeInt(bucketSize);
//			output.writeInt(count);
//			output.writeInt(keyUseLength);
//
//			for (int i = 0; i < bucketSize; i++) {
//				output.writeInt(bucket[i]);
//			}
//
//			for (int i = 0; i < keyUseLength; i++) {
//				output.writeUChar(keyArray[i]);
//			}
//
//			for (int i = 0; i < count; i++) {
//				output.writeInt(keyPos[i]);
//			}
//
//			for (int i = 0; i < count; i++) {
//				output.writeInt(nextIdx[i]);
//			}
//
//			output.close();
//
////			logger.info("Wrote {}, {}", Formatter.getFormatSize(output.size()), file.getAbsolutePath());
//            logger.info("Wrote {}, {}", file.length(), file.getAbsolutePath());
//		} catch (IOException e) {
//			logger.error("IOException",e);
//			throw new IRException(e);
//		}
//	}


    private long readKeyPos(int id) throws IOException {
        int segment = id / SEGMENT_ENTRY_SIZE;
        long offset = keyPosOffset + segment * SEGMENT_ENTRY_SIZE + id * KEY_WIDTH;
        raf.seek(offset);
        return raf.readLong();
    }
	private boolean isTheSame(String term, int id) throws IOException {
		long pos = readKeyPos(id);
        raf.seek(pos);
        for (int i = 0; i < KEY_WIDTH; i++) {
            if(term.charAt(i) != raf.readChar())
                return false;
        }
        return true;
	}

    private int readNextIndex(int id) throws IOException {
        int segment = id / SEGMENT_ENTRY_SIZE;
        long offset = nextOffset + segment * SEGMENT_ENTRY_SIZE + id * NEXT_WIDTH;
        raf.seek(offset);
        return raf.readInt();
    }
    private void writeNextIndex(int id, int nextId) throws IOException {
        int segment = id / SEGMENT_ENTRY_SIZE;
        long offset = nextOffset + segment * SEGMENT_ENTRY_SIZE + id * NEXT_WIDTH;
        raf.seek(offset);
        raf.writeInt(nextId);
    }
    private int readBucket(int hashValue) throws IOException {
        long offset = bucketOffset + hashValue * BUCKET_WIDTH;
        raf.seek(offset);
        return raf.readInt();
    }

    private void writeBucket(int hashValue, int id) throws IOException {
        long offset = bucketOffset + hashValue * BUCKET_WIDTH;
        raf.seek(offset);
        raf.writeInt(id);
    }

	public boolean put(String key) {
        try {
//		    logger.debug("term >> {}", term);
            int hashValue = (key.hashCode() % (bucketSize - 1)) + 1; //0을 피한다.

            int prev = 0;
            int idx = readBucket(hashValue);

            while (idx > 0) {
                if (isTheSame(key, idx)) {
                    //동일 발견시 false리턴.
                    return false;
                }

                prev = idx;
                idx = readNextIndex(idx);
            }
            idx = getNextIdx();
            logger.debug("NextIdx = {}, key = {}", idx, key);

            long offset = readKeyPos(idx);
            raf.seek(offset);
            for (int i = 0; i < key.length(); i++) {
                raf.writeChar(key.charAt(i));
            }

            if (prev > 0) {
//                nextIdx[prev] = idx;
                writeNextIndex(prev, idx);
            } else {
//                bucket[hashValue] = idx;
                writeBucket(hashValue, idx);
            }
            return true;
        }catch(Exception e) {
            logger.error("", e);
            return false;
        }
	}

	public boolean contains(String key) {
        try {
            int hashValue = (key.hashCode() % (bucketSize - 1)) + 1; //0을 피한다.
            int idx = readBucket(hashValue);

            while(idx > 0){
                if(isTheSame(key, idx)){
                    return true;
                }
                idx = readNextIndex(idx);
            }

            return false;
        }catch(Exception e) {
            logger.error("", e);
            return false;
        }
	}

	private int getNextIdx() {
        if(count >= limit) {
            spanFileToSegment(limit / bucketSize);
        }
		return count++;
	}

	//entry count
	public int count() {
		return count;
	}

}
