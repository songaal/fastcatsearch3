package org.fastcatsearch.transport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fastcatsearch.common.BytesArray;
import org.fastcatsearch.common.BytesReference;

public class FileChunkEnumeration implements Enumeration<BytesReference>{
	private static Logger logger = LoggerFactory.getLogger(FileChunkEnumeration.class);
	
	private byte[] buffer;
	private FileInputStream fis;
	private long nRead;
	private long fileSize;
	
	private boolean isRead;
	public FileChunkEnumeration(File file, int fileChunkSize) throws FileNotFoundException{
		if(file.exists()){
			fis = new FileInputStream(file);
			fileSize = file.length();
			
			buffer = new byte[fileChunkSize];
		}
	}
	
	
	/*
	 * 파일크기가 0이라도 기록해야하므로, true로 준다.
	 * */
	@Override
	public boolean hasMoreElements() {
		//파일크기가 0일때에는 아직읽지않은 상태이면 true. 읽었으면 false
		return (fileSize == 0 && !isRead) || nRead < fileSize;
	}

	@Override
	public BytesReference nextElement() {
		isRead = true;
		try {
			int n = fis.read(buffer);
			if(n == -1){ 
				if(fileSize == 0){
					n = 0;
				}else{
					throw new IOException("end of file while reading. nRead="+nRead+", fileSize="+fileSize);
				}
			}
			nRead += n;
//			logger.debug("read = {}, total = {} < {}", new Object[]{n, nRead, fileSize});
			return new BytesArray(buffer, 0, n);
		} catch (IOException e) {
			logger.error("", e);
		}
		return null;
	}


	public void close() {
		// 파일닫기.
		if(fis != null){
			try {
				fis.close();
			} catch (IOException e) {
				logger.error("", e);
			}
		}
	}


}
