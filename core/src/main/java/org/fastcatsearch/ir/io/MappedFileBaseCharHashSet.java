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
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 대용량의 hashset 처리를 위해서 파일기반으로 hashset를 처리하는 클래스.
 * key는 고정길이 String이다.
 *
 * */
public class MappedFileBaseCharHashSet {
	private static final Logger logger = LoggerFactory.getLogger(MappedFileBaseCharHashSet.class);

    private File f;

    private RandomAccessFile raf;
    private FileChannel chan;
    private MappedByteBuffer buf;
    private int segmentOffset;

    private int index;
    private int bucketSize;
    private static final int HEADER_LENGTH = 12;
    private static final int bucketOffset = 12;
    private static final int BUCKET_WIDTH = 4;
    private static final int NEXT_WIDTH = 4;
    private static final int KEYPOS_WIDTH = 4;

    private int nextLength;
    private int keyPosLength;

    private int keyCharSize;
    private int keyByteSize;
    private int segmentEntrySize;
    private int bucketLength;
    private int segmentWidth;
    private int limit;

	public MappedFileBaseCharHashSet(File f, int bucketSize, int keySize){
        this.f = f;
        if(f.exists()) {
            f.delete();
        }

        try {
            f.createNewFile();
            this.raf = new RandomAccessFile(f, "rw");
            this.chan = raf.getChannel();

            int index = 1;
            init(bucketSize, keySize, index);
            spanFileToSegment(0);
            buf.position(0);
            buf.putInt(bucketSize);
            buf.putInt(keySize);
            buf.putInt(index);
        } catch (IOException e) {
            logger.error("", e);
        }
	}

    public void init(int bucketSize, int keyCharSize, int index){
        this.bucketSize = bucketSize;
        segmentEntrySize = bucketSize;

        this.keyCharSize = keyCharSize;
        this.keyByteSize = keyCharSize * 2;
        nextLength = NEXT_WIDTH * segmentEntrySize;
        keyPosLength = KEYPOS_WIDTH * segmentEntrySize;

        bucketLength = BUCKET_WIDTH * segmentEntrySize;
        segmentOffset = HEADER_LENGTH + bucketLength;
        segmentWidth = (NEXT_WIDTH + KEYPOS_WIDTH + keyByteSize) * segmentEntrySize;
        logger.debug("segment off[{}] width[{}]", segmentOffset, segmentWidth);
        this.index = index;
        this.limit = bucketSize;////;(index / segmentEntrySize + 1) * segmentEntrySize; //엔트리 제한 갯수. 이 수치를 넘어서면 파일길이를 늘린다.
    }

    public int getNextOffset(int segment, int index) {
        if(segment > 0) {
            index = index % segmentEntrySize;
        }
        return segmentOffset + segment * segmentWidth + index * NEXT_WIDTH;
    }
    public int getKeyPosOffset(int segment, int index) {
        if(segment > 0) {
            index = index % segmentEntrySize;
        }
        return segmentOffset + segment * segmentWidth + nextLength + index * KEYPOS_WIDTH;
    }
    public int getKeyArrayOffset(int segment, int index) {
        if(segment > 0) {
            index = index % segmentEntrySize;
        }
        return segmentOffset + segment * segmentWidth + nextLength + keyPosLength + index * keyByteSize;
    }

    private void spanFileToSegment(int segment) {
        try {
            int expectedLength = segmentOffset + (segment + 1) * segmentWidth;//HEADER_LENGTH + bucketLength + (segment + 1) * segmentWidth;
            logger.debug("## span file seg[{}] len[{}]", segment, expectedLength);
            raf.setLength(expectedLength);
            chan = raf.getChannel();
            buf = chan.map(FileChannel.MapMode.READ_WRITE, 0, expectedLength);
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    private int newKeyPos(int id) throws IOException {
        int segment = id / segmentEntrySize;
        int offset = getKeyPosOffset(segment, id);//keyPosOffset + segment * segmentEntrySize + id * KEYPOS_WIDTH;
        int keyOffset = getKeyArrayOffset(segment, id);//keyArrayOffset + id * keyByteSize;
        buf.position(offset);
        buf.putInt(keyOffset);
        return keyOffset;
    }
    private int readKeyPos(int id) throws IOException {
        int segment = id / segmentEntrySize;
        int offset = getKeyPosOffset(segment, id);//keyPosOffset + segment * segmentEntrySize + id * KEYPOS_WIDTH;
        buf.position(offset);
        return buf.getInt();
    }
	private boolean isTheSame(String term, int id) throws IOException {
		int pos = readKeyPos(id);
        logger.debug("isTheSame {}=={} at {}", term, id, pos);
        if(pos <= 0) {
            return false;
        }
        buf.position(pos);
        for (int i = 0; i < keyCharSize; i++) {
            char ch = buf.getChar();
            logger.debug("comp {}:{}", term.charAt(i), ch);
            if (term.charAt(i) != ch)
//            if (term.charAt(i) != raf.readChar())
                return false;
        }
        return true;
	}

    private int readNextIndex(int id) throws IOException {
        int segment = id / segmentEntrySize;
        int offset = getNextOffset(segment, id);//nextOffset + segment * segmentEntrySize + id * NEXT_WIDTH;
        buf.position(offset);
        return buf.getInt();
    }
    private void writeNextIndex(int id, int nextId) throws IOException {
        int segment = id / segmentEntrySize;
        int offset = getNextOffset(segment, id);//nextOffset + segment * segmentEntrySize + id * NEXT_WIDTH;
        buf.position(offset);
        buf.putInt(nextId);
    }
    private int readBucket(int hashValue) throws IOException {
        int offset = bucketOffset + hashValue * BUCKET_WIDTH;
        buf.position(offset);
        return buf.getInt();
    }

    private void writeBucket(int hashValue, int id) throws IOException {
        int offset = bucketOffset + hashValue * BUCKET_WIDTH;
        buf.position(offset);
        buf.putInt(id);
    }

	public boolean add(String key) {
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

            int offset = newKeyPos(idx);
            buf.position(offset);
            logger.debug("write {} at {}", key, offset);
            for (int i = 0; i < key.length(); i++) {
                buf.putChar(key.charAt(i));

            }

            if (prev > 0) {
                writeNextIndex(prev, idx);
                logger.debug("writeNext prev[{}], id[{}]", prev, idx);
            } else {
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
            limit += segmentEntrySize;
        }
        return index++;
	}

	public int lastIndex() {
		return index;
	}

    public void clean() {
        try {
            chan.close();
            f.delete();
        } catch (IOException e) {
            logger.error("", e);
        }
    }
}
