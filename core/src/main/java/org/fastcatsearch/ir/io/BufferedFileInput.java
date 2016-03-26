package org.fastcatsearch.ir.io;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BufferedFileInput extends IndexInput implements Cloneable {
	private static Logger logger = LoggerFactory.getLogger(BufferedFileInput.class);
	
	private static final int chunkSize = 100 * 1024 * 1024;
	protected boolean isClone;

	protected final RandomAccessFile file;
	protected final File f;
	
	protected byte[] buffer;

	private long bufferStart = 0; // position in file of buffer
	private int bufferLength = 0; // end of valid bytes
	private int bufferPosition = 0; // next byte to read

	protected final long off;
	protected final long end;

	private ByteBuffer byteBuf; // wraps the buffer for NIO
	private int bufferSize = IOUtil.FILEBLOCKSIZE;
	final protected FileChannel channel;

	public BufferedFileInput(String filename) throws IOException {
		this(null, filename);
	}

	public BufferedFileInput(File dir, String filename) throws IOException {
		this(new File(dir, filename));
	}

	public BufferedFileInput(File f) throws IOException {
		off = 0L;
		end = f.length();
		this.f = f;
		file = new RandomAccessFile(f, "r");
		channel = file.getChannel();
		byteBuf = ByteBuffer.allocate(bufferSize);
		byteBuf.flip(); // pos와 limit을 모두 0으로 만들어준다.
//		logger.debug("File open {}", f.getAbsolutePath());
	}

	public String toString(){
		return "["+getClass().getName()+"]"+f.getName()+", length="+f.length();
	}
	
	@Override
	public BufferedFileInput clone() {
		BufferedFileInput clone = (BufferedFileInput) super.clone();
		clone.buffer = null;
		clone.byteBuf = null;
		clone.bufferLength = 0;
		clone.bufferPosition = 0;
		clone.bufferStart = position();
		clone.isClone = true;
		return clone;
	}

	@Override
	public final short readShort() throws IOException {
		if (2 <= (bufferLength - bufferPosition)) {
			return (short) (((buffer[bufferPosition++] & 0xFF) << 8) | (buffer[bufferPosition++] & 0xFF));
		} else {
			return super.readShort();
		}
	}

	@Override
	public final int readInt() throws IOException {
		if (4 <= (bufferLength - bufferPosition)) {
			return ((buffer[bufferPosition++] & 0xFF) << 24) | ((buffer[bufferPosition++] & 0xFF) << 16) | ((buffer[bufferPosition++] & 0xFF) << 8)
					| (buffer[bufferPosition++] & 0xFF);
		} else {
			return super.readInt();
		}
	}

	@Override
	public final long readLong() throws IOException {
		if (8 <= (bufferLength - bufferPosition)) {
			final int i1 = ((buffer[bufferPosition++] & 0xff) << 24) | ((buffer[bufferPosition++] & 0xff) << 16) | ((buffer[bufferPosition++] & 0xff) << 8)
					| (buffer[bufferPosition++] & 0xff);
			final int i2 = ((buffer[bufferPosition++] & 0xff) << 24) | ((buffer[bufferPosition++] & 0xff) << 16) | ((buffer[bufferPosition++] & 0xff) << 8)
					| (buffer[bufferPosition++] & 0xff);
			return (((long) i1) << 32) | (i2 & 0xFFFFFFFFL);
		} else {
			return super.readLong();
		}
	}

	@Override
	public final byte readByte() throws IOException {
		if (bufferPosition >= bufferLength) {
			refill();
		}
		return buffer[bufferPosition++];
	}

	@Override
	public void readBytes(byte[] b, int offset, int len) throws IOException {
		readBytes(b, offset, len, true);
	}

	public final void readBytes(byte[] b, int offset, int len, boolean useBuffer) throws IOException {

		if (len <= (bufferLength - bufferPosition)) {
			// the buffer contains enough data to satisfy this request
			if (len > 0) // to allow b to be null if len is 0...
				System.arraycopy(buffer, bufferPosition, b, offset, len);
			bufferPosition += len;
		} else {
			// the buffer does not have enough data. First serve all we've got.
			int available = bufferLength - bufferPosition;
			if (available > 0) {
				System.arraycopy(buffer, bufferPosition, b, offset, available);
				offset += available;
				len -= available;
				bufferPosition += available;
			}
			// and now, read the remaining 'len' bytes:
			if (useBuffer && len < bufferSize) {
				// If the amount left to read is small enough, and
				// we are allowed to use our buffer, do it in the usual
				// buffered way: fill the buffer and copy from it:
				refill();
				if (bufferLength < len) {
					// Throw an exception when refill() could not read len
					// bytes:
					System.arraycopy(buffer, 0, b, offset, bufferLength);
					throw new EOFException("read past EOF: " + this);
				} else {
					System.arraycopy(buffer, 0, b, offset, len);
					bufferPosition = len;
				}
			} else {
				// The amount left to read is larger than the buffer
				// or we've been asked to not use our buffer -
				// there's no performance reason not to read it all
				// at once. Note that unlike the previous code of
				// this function, there is no need to do a seek
				// here, because there's no need to reread what we
				// had in the buffer.
				long after = bufferStart + bufferPosition + len;
				if (after > length())
					throw new EOFException("read past EOF: " + this);
				readInternal(b, offset, len);
				bufferStart = after;
				bufferPosition = 0;
				bufferLength = 0; // trigger refill() on read
			}
		}

	}

	private void refill() throws IOException {
		long start = bufferStart + bufferPosition;
		long end = start + bufferSize;
		if (end > length()) // don't read past EOF
			end = length();
		int newLength = (int) (end - start);
		if (newLength <= 0)
			throw new EOFException("read past EOF: " + this);

		if (buffer == null) {
			newBuffer(new byte[bufferSize]); // allocate buffer lazily
			seek(bufferStart);
		}
		readInternal(buffer, 0, newLength);
		bufferLength = newLength;
		bufferStart = start;
		bufferPosition = 0;
//		logger.debug("refill len={}, stat={}, pos={}", bufferLength, bufferStart, position());
	}

	protected void newBuffer(byte[] newBuffer) {
		buffer = newBuffer;
		byteBuf = ByteBuffer.wrap(newBuffer);
	}

	protected void readInternal(byte[] b, int offset, int len) throws IOException {

//        if(Thread.currentThread().isInterrupted()) {
//            throw new IOException("Got request to kill reading.");
//        }
		final ByteBuffer bb;

		// Determine the ByteBuffer we should use
		if (b == buffer && 0 == offset) {
			// Use our own pre-wrapped byteBuf:
			assert byteBuf != null;
			byteBuf.clear();
			byteBuf.limit(len);
			bb = byteBuf;
		} else {
			bb = ByteBuffer.wrap(b, offset, len);
		}

		int readOffset = bb.position();
		int readLength = bb.limit() - readOffset;
		assert readLength == len;

		long pos = position() + off;

		if (pos + len > end) {
			throw new EOFException("read past EOF: " + this);
		}

		try {
			while (readLength > 0) {
				final int limit;
				if (readLength > chunkSize) {
					// // LUCENE-1566 - work around JVM Bug by breaking
					// // very large reads into chunks
					limit = readOffset + chunkSize;
				} else {
					limit = readOffset + readLength;
				}
				bb.limit(limit);

//                if(!channel.isOpen()) {
//                    logger.error("Channel is closed! f={}, chan={}", f.getName(), channel);
//                }
				int i = channel.read(bb, pos);
				pos += i;
				readOffset += i;
				readLength -= i;
			}
		} catch (OutOfMemoryError e) {
			// propagate OOM up and add a hint for 32bit VM Users hitting the
			// bug
			// with a large chunk size in the fast path.
			final OutOfMemoryError outOfMemoryError = new OutOfMemoryError("OutOfMemoryError likely caused by the Sun VM Bug described in "
					+ "https://issues.apache.org/jira/browse/LUCENE-1566; try calling FSDirectory.setReadChunkSize "
					+ "with a value smaller than the current chunk size. readLength= " + readLength);
			outOfMemoryError.initCause(e);
			throw outOfMemoryError;
		} catch (IOException ioe) {
			throw new IOException(ioe.getMessage() + ": " + this, ioe);
		}
	}

	@Override
	public void close() throws IOException {
		if (!isClone) {
			file.close();
		}
	}

	public long position() {
		return bufferStart + bufferPosition;
	}

	@Override
	public void seek(long pos) throws IOException {
		if (pos >= bufferStart && pos < (bufferStart + bufferLength))
			bufferPosition = (int) (pos - bufferStart); // seek within buffer
		else {
			bufferStart = pos;
			bufferPosition = 0;
			bufferLength = 0; // trigger refill() on read()
			// seekInternal(pos);
		}
	}

	@Override
	public final long length() {
		return end;
	}


	@Override
	public void reset() throws IOException {
		
	}

	@Override
	public int read() throws IOException {
		return readByte() & 0xff;
	}


}
