/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.fastcatsearch.common.io;

import java.io.IOException;
import java.util.Arrays;

import org.fastcatsearch.common.Bytes;
import org.fastcatsearch.common.BytesArray;
import org.fastcatsearch.common.BytesReference;
import org.fastcatsearch.ir.io.DataOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class BytesStreamOutput extends DataOutput implements BytesStream {
	protected static Logger logger = LoggerFactory.getLogger(BytesStreamOutput.class);
	
    /**
     * The buffer where data is stored.
     */
    protected byte buf[];

    /**
     * The number of valid bytes in the buffer.
     */
    protected int count;

    public BytesStreamOutput() {
        this(1024);
    }

    public BytesStreamOutput(int size) {
        this.buf = new byte[size];
    }

    @Override
    public boolean seekPositionSupported() {
        return true;
    }

    @Override
    public long position() throws IOException {
        return count;
    }

    @Override
    public void seek(long position) throws IOException {
        if (position > Integer.MAX_VALUE) {
            throw new UnsupportedOperationException();
        }
        count = (int) position;
    }

    @Override
    public void writeByte(byte b) throws IOException {
        int newcount = count + 1;
        if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, Bytes.oversize(newcount, 1));
        }
        buf[count] = b;
        count = newcount;
    }

    public void skip(int length) {
        int newcount = count + length;
        if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, Bytes.oversize(newcount, 1));
        }
        count = newcount;
    }

    @Override
    public void writeBytes(byte[] b, int offset, int length) throws IOException {
        if (length == 0) {
            return;
        }
        int newcount = count + length;
        if (newcount > buf.length) {
        	logger.debug("Oversize {} > {}", newcount, buf.length);
            buf = Arrays.copyOf(buf, Bytes.oversize(newcount, 1));
        }
        System.arraycopy(b, offset, buf, count, length);
        count = newcount;
    }

    public void seek(int seekTo) {
        count = seekTo;
    }

    public void reset() {
        count = 0;
    }

    @Override
    public void flush() throws IOException {
        // nothing to do there
    }

    @Override
    public void close() throws IOException {
        // nothing to do here
    }

    /**
     * Returns the current size of the buffer.
     *
     * @return the value of the <code>count</code> field, which is the number
     *         of valid bytes in this output stream.
     * @see java.io.ByteArrayOutputStream#count
     */
    public long size() {
        return count;
    }
    
    public byte[] bytes(){
    	return buf;
    }
    @Override
    public BytesReference bytesReference() {
        return new BytesArray(buf, 0, count);
    }
    
    public int length(){
    	return buf.length;
    }

//	@Override
//	public void setLength(long newLength) throws IOException {
//		if(count > newLength){
//			count = (int) newLength;
//		}
//	}

}
