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

    private RandomAccessFile raf;
    private long segmentOffset;

    private int index;
    private int bucketSize;
    private static final long HEADER_LENGTH = 12L;
    private static final long bucketOffset = 12L;
    private static final int BUCKET_WIDTH = 4;
    private static final int NEXT_WIDTH = 4;
    private static final int KEYPOS_WIDTH = 8;

    private int nextLength;
    private int keyPosLength;

    private int keyCharSize;
    private int keyByteSize;
    private int segmentEntrySize;
    private int bucketLength;
    private int segmentWidth;
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

            int index = 1;
            init(bucketSize, keySize, index);
            raf.seek(0);
            raf.writeInt(bucketSize);
            raf.writeInt(keySize);
            raf.writeInt(index);
            spanFileToSegment(0);

            raf.seek(12);
            int tmp = raf.readUnsignedShort();

            logger.debug("tmp = {}", tmp);
        } catch (IOException e) {
            logger.error("", e);
        }
	}

    public void init(int bucketSize, int keyCharSize, int index){
        this.bucketSize = bucketSize;
        segmentEntrySize = bucketSize;

        this.keyCharSize = keyCharSize;
        this.keyByteSize = keyCharSize * 2;
//        nextOffset = bucketOffset + BUCKET_WIDTH * segmentEntrySize;
        nextLength = NEXT_WIDTH * segmentEntrySize;
        keyPosLength = KEYPOS_WIDTH * segmentEntrySize;

        bucketLength = BUCKET_WIDTH * segmentEntrySize;
        segmentOffset = HEADER_LENGTH + bucketLength;
        segmentWidth = (NEXT_WIDTH + KEYPOS_WIDTH + keyByteSize) * segmentEntrySize;
        this.index = index;
        this.limit = bucketSize;////;(index / segmentEntrySize + 1) * segmentEntrySize; //엔트리 제한 갯수. 이 수치를 넘어서면 파일길이를 늘린다.
    }

    public long getNextOffset(int segment, int index) {
        if(segment > 0) {
            index = index % segmentEntrySize;
        }
        return segmentOffset + segment * segmentWidth + index * NEXT_WIDTH;
    }
    public long getKeyPosOffset(int segment, int index) {
        if(segment > 0) {
            index = index % segmentEntrySize;
        }
        return segmentOffset + segment * segmentWidth + nextLength + index * KEYPOS_WIDTH;
    }
    public long getKeyArrayOffset(int segment, int index) {
        if(segment > 0) {
            index = index % segmentEntrySize;
        }
        return segmentOffset + segment * segmentWidth + nextLength + keyPosLength + index * keyByteSize;
    }

    private void spanFileToSegment(int segment) {
        try {
            long expectedLength = segmentOffset + (segment + 1) * segmentWidth;//HEADER_LENGTH + bucketLength + (segment + 1) * segmentWidth;
            logger.info("## span file seg[{}] len[{}]", segment, expectedLength);
            raf.setLength(expectedLength);
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

    private long newKeyPos(int id) throws IOException {
        int segment = id / segmentEntrySize;
        long offset = getKeyPosOffset(segment, id);//keyPosOffset + segment * segmentEntrySize + id * KEYPOS_WIDTH;
        long keyOffset = getKeyArrayOffset(segment, id);//keyArrayOffset + id * keyByteSize;
        raf.seek(offset);
        raf.writeLong(keyOffset);
        return keyOffset;
    }
    private long readKeyPos(int id) throws IOException {
        int segment = id / segmentEntrySize;
        long offset = getKeyPosOffset(segment, id);//keyPosOffset + segment * segmentEntrySize + id * KEYPOS_WIDTH;
        raf.seek(offset);
        return raf.readLong();
    }
	private boolean isTheSame(String term, int id) throws IOException {
		long pos = readKeyPos(id);
        logger.debug("isTheSame {}=={} at {}", term, id, pos);
        if(pos <= 0) {
            return false;
        }
        raf.seek(pos);
        for (int i = 0; i < keyCharSize; i++) {
            char ch = raf.readChar();
            logger.debug("comp {}:{}", term.charAt(i), ch);
            if (term.charAt(i) != ch)
//            if (term.charAt(i) != raf.readChar())
                return false;
        }
        return true;
	}

    private int readNextIndex(int id) throws IOException {
        int segment = id / segmentEntrySize;
        long offset = getNextOffset(segment, id);//nextOffset + segment * segmentEntrySize + id * NEXT_WIDTH;
        raf.seek(offset);
        return raf.readInt();
    }
    private void writeNextIndex(int id, int nextId) throws IOException {
        int segment = id / segmentEntrySize;
        long offset = getNextOffset(segment, id);//nextOffset + segment * segmentEntrySize + id * NEXT_WIDTH;
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
        if(key.length() > keyCharSize) {
            key = key.substring(0, keyCharSize);
        }
        try {
            int hashValue = (Math.abs(key.hashCode()) % (bucketSize - 1)) + 1; //0을 피한다.

            int prev = 0;
            int id = readBucket(hashValue);

            logger.debug("------------------");
            logger.debug("key[{}] hash[{}] bucket[{}]", key, hashValue, id);
            while (id > 0) {
                if (isTheSame(key, id)) {
                    //동일 발견시 false리턴.
                    logger.debug("Dup entry!");
                    return false;
                }

                prev = id;
                id = readNextIndex(id);
            }
            int idx = newIndex();
            logger.debug("Index[{}] {}", idx, key);

            long offset = newKeyPos(idx);
            raf.seek(offset);
            logger.debug("write {} at {}", key, offset);
            for (int i = 0; i < key.length(); i++) {
                raf.writeChar(key.charAt(i));
            }

            if (prev > 0) {
//                nextIdx[prev] = idx;
                writeNextIndex(prev, idx);
                logger.debug("writeNext prev[{}], id[{}]", prev, idx);
            } else {
//                bucket[hashValue] = idx;
                writeBucket(hashValue, idx);
                logger.debug("writeBucket hash[{}], id[{}]", hashValue, idx);
            }
            return true;
        }catch(Exception e) {
            logger.error("", e);
            return false;
        }
	}

	public boolean contains(String key) {
        try {
            int hashValue = (Math.abs(key.hashCode()) % (bucketSize - 1)) + 1; //0을 피한다.
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

	private int newIndex() {
        logger.debug("# newIndex index[{}] limit[{}]", index, limit);
        if(index >= limit) {
            spanFileToSegment(limit / bucketSize);
            limit *= 2;
        }
        return index++;
	}

	public int lastIndex() {
		return index;
	}

}
