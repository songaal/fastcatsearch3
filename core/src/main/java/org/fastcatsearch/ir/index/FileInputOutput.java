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

package org.fastcatsearch.ir.index;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.fastcatsearch.ir.io.FastByteBuffer;




public class FileInputOutput extends InputOutput{
	private RandomAccessFile raf;
	
	public FileInputOutput(File dir, String name) throws IOException{
		this(dir, name, false);
	}
	public FileInputOutput(File dir, String name, boolean append) throws IOException{
		raf = new RandomAccessFile(new File(dir, name),"rw");
		if(!append)
			raf.setLength(0);
	}

	public void close() throws IOException {
		raf.close();
	}

	public synchronized long position() throws IOException {
		return raf.getFilePointer();
	}

	public synchronized void position(long pos) throws IOException {
		raf.seek(pos);
	}

	public synchronized int readByte() throws IOException {
		return raf.read();
	}

	public synchronized int readBytes(FastByteBuffer dst) throws IOException {
		int len = dst.limit - dst.pos;
		return raf.read(dst.array, dst.pos, len);
	}

	public synchronized void writeByte(int b) throws IOException {
		raf.write(b);
	}

	public synchronized int writeBytes(FastByteBuffer dst) throws IOException {
		int len = dst.limit - dst.pos;
		raf.write(dst.array, dst.pos, len);
		dst.pos(dst.limit);
		return len;
	}

}
