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

import org.apache.lucene.util.BytesRef;
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
 * key는 고정길이 BytesRef이다.
 *
 * */
public class MappedFileBaseByteHashSet {
	private static final Logger logger = LoggerFactory.getLogger(MappedFileBaseByteHashSet.class);

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

    private int keyByteSize;
    private int segmentEntrySize;
    private int bucketLength;
    private int segmentWidth;
    private int limit;

	public MappedFileBaseByteHashSet(File f, int bucketSize, int keySize){
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

    public void init(int bucketSize, int keyByteSize, int index){
        this.bucketSize = bucketSize;
        segmentEntrySize = bucketSize;

        this.keyByteSize = keyByteSize;
        nextLength = NEXT_WIDTH * segmentEntrySize;
        keyPosLength = KEYPOS_WIDTH * segmentEntrySize;

        bucketLength = BUCKET_WIDTH * segmentEntrySize;
        segmentOffset = HEADER_LENGTH + bucketLength;
        segmentWidth = (NEXT_WIDTH + KEYPOS_WIDTH + keyByteSize) * segmentEntrySize;
        logger.trace("segment off[{}] width[{}]", segmentOffset, segmentWidth);
        this.index = index;
        this.limit = bucketSize;
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
            int expectedLength = segmentOffset + (segment + 1) * segmentWidth;
            logger.trace("## span file seg[{}] len[{}]", segment, expectedLength);
            raf.setLength(expectedLength);
            chan = raf.getChannel();
            buf = chan.map(FileChannel.MapMode.READ_WRITE, 0, expectedLength);
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    private int newKeyPos(int id) throws IOException {
        int segment = id / segmentEntrySize;
        int offset = getKeyPosOffset(segment, id);
        int keyOffset = getKeyArrayOffset(segment, id);
        buf.position(offset);
        buf.putInt(keyOffset);
        return keyOffset;
    }
    private int readKeyPos(int id) throws IOException {
        int segment = id / segmentEntrySize;
        int offset = getKeyPosOffset(segment, id);
        buf.position(offset);
        return buf.getInt();
    }
	private boolean isTheSame(BytesRef key, int id) throws IOException {
		int pos = readKeyPos(id);
        logger.trace("isTheSame {}=={} at {}", key, id, pos);
        if(pos <= 0) {
            return false;
        }
        buf.position(pos);
        for (int i = 0; i < keyByteSize; i++) {
            byte b = buf.get();
            logger.trace("comp {}:{}", key.get(i), b);
            if (key.get(i) != b) {
                return false;
            }
        }
        return true;
	}

    private int readNextIndex(int id) throws IOException {
        int segment = id / segmentEntrySize;
        int offset = getNextOffset(segment, id);
        buf.position(offset);
        return buf.getInt();
    }
    private void writeNextIndex(int id, int nextId) throws IOException {
        int segment = id / segmentEntrySize;
        int offset = getNextOffset(segment, id);
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

	public boolean add(BytesRef key) {
        try {
            int hashValue = (Math.abs(key.hashCode()) % (bucketSize - 1)) + 1; //0을 피한다.

            int prev = 0;
            int id = readBucket(hashValue);

            logger.trace("------------------");
            logger.trace("key[{}] hash[{}] bucket[{}]", key, hashValue, id);
            while (id > 0) {
                if (isTheSame(key, id)) {
                    //동일 발견시 false리턴.
                    logger.trace("Dup entry!");
                    return false;
                }

                prev = id;
                id = readNextIndex(id);
            }
            int idx = newIndex();
            logger.trace("Index[{}] {}", idx, key);

            int offset = newKeyPos(idx);
            buf.position(offset);
            logger.trace("write {} at {}", key, offset);
            for (int i = 0; i < key.length(); i++) {
                buf.put(key.get(i));

            }

            if (prev > 0) {
                writeNextIndex(prev, idx);
                logger.trace("writeNext prev[{}], id[{}]", prev, idx);
            } else {
                writeBucket(hashValue, idx);
                logger.trace("writeBucket hash[{}], id[{}]", hashValue, idx);
            }
            return true;
        }catch(Exception e) {
            logger.error("", e);
            return false;
        }
	}

	private int newIndex() {
        logger.trace("# newIndex index[{}] limit[{}]", index, limit);
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
