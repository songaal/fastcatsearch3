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

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-safe하지 않은 객체이므로 동기화는 외부에서 처리해준다.
 * 내부적으로 사용하는 버퍼의 끝인 limit은 fc.position과 항상 일치해야 한다.
 * 버퍼를 채우면 버퍼의 pos는 0이고 limit은 버퍼의 마지막 위치(파일크기가 허용하는한), fc.position은 버퍼을 읽은 마지막 위치가 된다.
 * 예를 들어, 파일크기가 300byte인 파일을 열어서 버퍼를 채우면, 버퍼의 pos=0, limit=300이고, fc.position은 300이 된다.
 * 다른예로, 파일크기가 100MB인 파일을 열어서 버퍼를 채우면, 버퍼의 pos=0, limit=8192이고, fc.position은 8192이 된다.
 * 또 다른예로, 파일크기가 9000인 파일을 열어서 이 객체의 position을 7000으로 변경하고 버퍼를 채우면, 버퍼의 pos=0, limit=2000이고, fc.position은 7000이 된다.
 * 어느 경우든지, fc.position과 buf의 limit은 일치하여 데이터는 유지된다.
 * @author sangwook
 *
 */
public class CachedFileInput extends Input{
	private static Logger logger = LoggerFactory.getLogger(CachedFileInput.class);
			
	protected FileChannel fc;
	protected FileInputStream fis;
	protected ByteBuffer buf;
	protected File f;
	
	public CachedFileInput(File dir, String filename) throws IOException{
		this(new File(dir, filename));
	}
	public CachedFileInput(File file) throws IOException{
		this.f = file;
		fis = new FileInputStream(file);
		fc = fis.getChannel();
		buf = ByteBuffer.allocate(IOUtil.PAGESIZE);
		buf.flip(); //pos와 limit을 모두 0으로 만들어준다.
//		logger.debug("file {}", f.getAbsolutePath());
//		logger.debug("fc size={}, pos={}", fc.size(), fc.position());
//		logger.debug("buf.limit()={}", buf.limit());
	}
	public CachedFileInput(String filename) throws IOException{
		this(null, filename);
	}
	
	public long size() throws IOException{
		return fc.size();
	}
	//fc의 현재위치에서 buffer를 채워준다.
	private int fillBuffer() throws IOException {
		buf.clear();
		int read = 0;
		int toRead = buf.limit();
		
		while(toRead > 0){
			int n = fc.read(buf);
			if(n < 0){
				//파일끝이므로 탈출.
				break;
			}
			
			read += n;
			toRead -= n;
		}
		buf.flip();
//		if(n < 0) throw new EOFException("Get to end of file. fc.size()="+fc.size()+", fc.position()="+fc.position());
		if(read == 0){
			buf.limit(0);
			buf.position(0);
		}
		return read;
	}
	public int readByte() throws IOException {
		if(buf.remaining() == 0) {
			fillBuffer();
		}
		return buf.get() & 0xFF;
	}
	
	public String toString(){
		try {
			return "BufferedFileInput path = "+f.getAbsolutePath()+", "+fc.size()+" bytes";
		} catch (IOException e) {
			return "BufferedFileInput path = "+f.getAbsolutePath();
		}
	}
	public void close() throws IOException {
		fis.close();
		fc.close();
	}
	
	public long position() throws IOException {
		return fc.position() - buf.remaining();
	}
	
	public void position(long pos) throws IOException {
		long limit = fc.size();
		long p = fc.position();
//		logger.debug("buf.limit()={}", buf.limit());
//		logger.debug("@@@ p - buf.limit() = {} <? pos {} <? {}", new Object[]{p - buf.limit(), pos, limit});
		if(pos > limit){
			throw new IOException("파일크기를 넘어서는 위치를 지정했습니다.pos="+pos);
		}
		if((p - buf.limit()) <= pos && pos < p){
//			logger.debug("@@@1 {} - {} = {}", new Object[]{pos + buf.limit(), p, pos + buf.limit()-p});
			//TODO buf limit보다 작은지 확인필요.. limit이 0인 경우도 있어서... 또는 파일이 작아서 길이가 4,5인 경우 ? 아.. 그런경우는 안생기겠다. fc.size가 확인해주기때문에..
			buf.position((int)(pos + buf.limit() - p)); // buf.limit - (p - pos)
		}else{
//			logger.debug("@@@2 fc.position()={}, buf.position()={}", pos, buf.limit());
			fc.position(pos);
			fillBuffer();
		}
	}

	public int readBytes(BytesBuffer dst) throws EOFException, IOException {
		int n = readBytes(dst.bytes, dst.offset, dst.length - dst.offset);
		dst.offset = dst.length;
		return n;
	}
	
	public int readBytes(byte[] array, int pos, int length) throws EOFException, IOException {
		int read = 0;
//		logger.debug("readBytes >> {}", length);
		while(length > 0){
			if(length > IOUtil.PAGESIZE){
				//
				//버퍼의 데이터를 먼저 읽은후, 나머지를 fc에서 읽어들어는 방식을 사용한다.
				//
				//1. 버퍼의 데이터를 먼저 읽는다.
				int len = buf.remaining();
				buf.get(array, pos, len);
				pos += len;
				read += len;
				length -= len;
				
//				logger.debug("readBytes data is longger than buffer = "+left);
				//set virtual position to channel position
//				logger.debug("position() = "+position());
//				fc.position(position());
				ByteBuffer tempBuf = ByteBuffer.wrap(array, pos, length);
//				buf.position(buf.limit()); //invalidate buffer data
//				long fileRemain = fc.size() - fc.position();
//				if(fileRemain < tempBuf.remaining()){
//					throw new EOFException("읽으려는 데이터보다 파일이 모자랍니다.filesize="+fc.size()+", file remain="+fileRemain+", toRead="+tempBuf.remaining());
//				}
				while(length > 0){
					int n = fc.read(tempBuf);
					if(n < 0)
						throw new EOFException("Get to end of file.");
					pos += n;
					read += n;
					length -= n;
				}
				
				//fc를 사용하여 읽은 데이터가 없으므로 buf는 0인 상태로 만들어준다.
				buf.position(0);
				buf.limit(0);
			}else{
				int len = buf.remaining();
//				logger.debug("length = "+length+", len="+len);
				//has enough data 
				if(len >= length){
					//1. 버퍼에 남아있는 데이터가 요청한 길이보다 클때는 모두 버퍼에서 읽어들인다. 
					buf.get(array, pos, length);
					pos += length;
					read += length;
					length = 0;
				}else{
					//2. 버퍼에 남아있는 데이터가 요청한 길이보다 작으면, 일부는 버퍼에서 모두 읽고, 버퍼를 새롭게 채운다음 나머지는 다음 루프에서 읽어들인다.
					buf.get(array, pos, len);
					pos += len;
					read += len;
					length -= len;
					//buf is empty, read data from file channel
					buf.clear();
					int nFill = fillBuffer();
					if(nFill <= 0){
						//더이상 채울 버퍼가 없다면 읽을 수가 없으므로 loop를 빠져나간다.
						break;
					}
//					int n = fc.read(buf);
//					if(n < 0)
//						throw new EOFException("Get to end of file. filesize="+fc.size()+", pos = "+position()+", buf="+buf.remaining());
//					buf.flip();
				}
				
			}
			
		}
		return read;
	}

}
