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

package org.fastcatsearch.ir.io.cache;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.io.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author sangwook
 *
 */
public class PagedFile {
	private static Logger logger = LoggerFactory.getLogger(PagedFile.class);
	
	protected FileChannel fc;
	protected FileInputStream fis;
	protected ByteBuffer buf;
	protected File f;
	
	private int FILE_ID;
	private PageTable table;
	
	
	
	public PagedFile(File dir, String filename) throws IOException{
		this(new File(dir, filename));
	}
	public PagedFile(File file) throws IOException{
		this.f = file;
		fis = new FileInputStream(file);
		fc = fis.getChannel();
		buf = ByteBuffer.allocate(IOUtil.PAGESIZE);
		buf.flip();//empty at first
		
		table = PageTable.getInstance();
	}
	public PagedFile(String filename) throws IOException{
		this(null, filename);
	}
	
	
	public byte[] getPage(int page){
		
		byte[] data = null;//table.get(FILE_ID, page);
		
		return null;
	}
	
	public long size() throws IOException{
		return fc.size();
	}
	
	public int readByte() throws EOFException, IOException {
		if(buf.remaining() == 0) {
			buf.clear();
			int n = fc.read(buf);
			if(n < 0) throw new EOFException("Get to end of file. fc.size()="+fc.size()+", fc.position()="+fc.position());
			buf.flip();
		}
		return buf.get() & 0xFF;
	}
	
	public String toString(){
		return "BufferedFileInput path = "+f.getAbsolutePath();
	}
	public void close() throws IOException {
		fis.close();
		fc.close();
	}
	
	public long position() throws IOException {
		return fc.position() - buf.remaining();
	}
	
	public void position(long pos) throws IOException {
		long p = fc.position();
		if((p - buf.limit()) <= pos && pos < p)
			buf.position((int)(pos + buf.limit() - p)); // buf.limit - (p - pos)
		else{
			fc.position(pos);
			buf.position(buf.limit()); //invalidate buffer data
		}
	}

	public int readBytes(BytesBuffer dst) throws EOFException, IOException {
		
		int left = dst.remaining();
		int read = 0;
		
		while(left > 0){
			if(left > IOUtil.PAGESIZE){
				//TODO tuning..?
//				logger.debug("readBytes data is longger than buffer = "+left);
				//set virtual position to channel position
//				logger.debug("position() = "+position());
				fc.position(position());
				ByteBuffer tempBuf = ByteBuffer.wrap(dst.bytes, dst.offset, left);
				buf.position(buf.limit()); //invalidate buffer data
				while(left > 0){
					int n = fc.read(tempBuf);
					if(n < 0)
						throw new EOFException("Get to end of file.");
					dst.offset += n;
					read += n;
					left -= n;
				}
			}else{
				int len = buf.remaining();
//				logger.debug("left = "+left+", len="+len);
				//has enough data 
				if(len >= left){
					buf.get(dst.bytes, dst.offset, left);
					dst.offset += left;
					read += left;
					left = 0;
				}else{
					buf.get(dst.bytes, dst.offset, len);
					dst.offset += len;
					read += len;
					left -= len;
					//buf is empty, read data from file channel
					buf.clear();
					int n = fc.read(buf);
					if(n < 0)
						throw new EOFException("Get to end of file. filesize="+fc.size()+", pos = "+position()+", buf="+buf.remaining());
					buf.flip();
				}
				
			}
			
		}
		return read;
	}

}
