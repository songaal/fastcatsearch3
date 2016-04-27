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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.fastcatsearch.ir.index.IndexWriteInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author sangwook
 * 
 */
public class BufferedFileOutput extends IndexOutput {

	private static Logger logger = LoggerFactory.getLogger(BufferedFileOutput.class);

	private File f;
	protected RandomAccessFile raf;

	protected byte buf[];
	protected int count;

	protected IndexWriteInfo writeInfo;
	
	public BufferedFileOutput(String filename) throws IOException {
		this(new File("."), filename, false);
	}

	public BufferedFileOutput(File file) throws IOException {
		this(file, false);
	}

	public BufferedFileOutput(File dir, String filename) throws IOException {
		this(dir, filename, false);
	}

	public BufferedFileOutput(File dir, String filename, boolean append) throws IOException {
		this(new File(dir, filename), append);
	}

	public BufferedFileOutput(File file, boolean append) throws IOException {
		f = file;

		File pf = f.getParentFile();
		if (pf != null)
			pf.mkdirs();

		if (!append) {
			f.delete();
		}
		buf = new byte[IOUtil.PAGESIZE];
		raf = new RandomAccessFile(f, "rw");
		if (append) {
			long fp = raf.length();
			raf.seek(fp);
		}
		writeInfo = new IndexWriteInfo(f);
	}

	public String toString() {
		return "BufferedFileOutput path=" + f.getAbsolutePath();
	}

	public void flush() throws IOException {
		if (count > 0) {
			raf.write(buf, 0, count);
			count = 0;
		}
	}

	@Override
	public long length() throws IOException {
		return raf.length() + (long) count;
	}

	public File getFile() {
		return f;
	}

	public void close() throws IOException {
		flush();
		writeInfo.close(raf.length());
		raf.close();
	}

	public long position() throws IOException {
		return raf.getFilePointer() + count;
	}

	public void seek(long p) throws IOException {
		flush();
		raf.seek(p);
	}

	public void writeBytes(byte b[], int off, int len) throws IOException {
		if (len >= buf.length) {
			flush();
			raf.write(b, off, len);
			return;
		}
		if (len > buf.length - count) {
			flush();
		}
		try {
			System.arraycopy(b, off, buf, count, len);
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.error("len = " + len + " , count=" + count + " off=" + off + ", buf.length=" + buf.length, e);
			throw e;
		}
		count += len;
	}

	public void writeBytes(BytesBuffer dst) throws IOException {
		writeBytes(dst.bytes, dst.pos(), dst.remaining());
	}

	public void setLength(long newLength) throws IOException {
		flush();
		raf.setLength(newLength);
	}

	public void writeByte(byte b) throws IOException {
		if (count >= buf.length) {
			flush();
		}
		buf[count++] = (byte) b;
	}

	@Override
	public void reset() throws IOException {
		//do nothing
	}

	@Override
	public IndexWriteInfo getWriteInfo() {
		return writeInfo;
	}

    public RandomAccessFile getRaf() {
        return raf;
    }
}
