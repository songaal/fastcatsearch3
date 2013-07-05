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
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class MemoryMappedFileInput extends Input {
	protected FileChannel fc;
	protected ByteBuffer buf;
	protected File f;
	
	public MemoryMappedFileInput(File file) throws IOException{
		this.f = file;
		fc = new FileInputStream(file).getChannel();
		buf = fc.map(MapMode.READ_ONLY,0,fc.size());
	}

	public MemoryMappedFileInput(File dir, String filename) throws IOException{
		this(new File(dir, filename));
	}
	
	public long size() throws IOException{
		return buf.capacity();
	}
	@Override
	public void close() throws IOException {
		fc.close();
	}
	
	public void reset() throws IOException {
		buf.reset();
	}

	@Override
	public long position() throws IOException {
		return buf.position();
	}

	@Override
	public void position(long p) throws IOException {
		buf.position((int)p);
	}

	@Override
	public int readByte() throws IOException {
		return buf.get();
	}

	@Override
	public int readBytes(BytesBuffer dst) throws IOException {
		int len = dst.remaining();
		if((buf.remaining()) < len )
			throw new IOException("not enough buffer data. data length = "+(buf.remaining())+", read request = "+len);
		
		for(int i =0;i<len; i++){
			dst.write(buf.get());
		}
//		pos += len;
		return len;
	}

}
