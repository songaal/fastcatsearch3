package org.fastcatsearch.transport.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.ir.config.IRSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fastcatsearch.common.Strings;
import org.fastcatsearch.common.io.StreamInput;

public class FileTransportHandler {
	private static Logger logger = LoggerFactory.getLogger(FileTransportHandler.class);
	
	private Map<String, FileStreamHandle> fileMap;
	
	public FileTransportHandler(){
		fileMap = new ConcurrentHashMap<String, FileStreamHandle>();
	}
	
	public void handleFile(int seq, String filePath, long fileSize, long checksumCRC32, String fileKey, StreamInput input) throws IOException {
		FileStreamHandle fileHandle = fileMap.get(fileKey);
		if(seq == 0){
			//새로 생성.
			if(fileHandle == null){
				String absolutePath = IRSettings.path(filePath);
				try {
					fileHandle = new FileStreamHandle(absolutePath, fileSize, checksumCRC32);
					fileMap.put(fileKey, fileHandle);
					logger.debug("File handle map size = {}", fileMap.size());
				} catch (FileNotFoundException e) {
					logger.error("파일을 생성할수 없습니다.", e);
					throw new IOException("파일을 생성할수 없습니다.", e);
				}
			}else{
				//발생하면 안되는 경우.
				logger.error("seq가 0인데 map에 동일 파일전송이 남아있습니다.seq={}, filePath={}", seq, filePath);
			}
		}
		
		if(fileHandle == null){
			//null일경우는 이전 seq에서 에러발생한 경우.
			//더 이상 파일을 기록하지 않는다.
			throw new IOException("파일핸들이 없습니다.");
//			return;
		}
		
		try{
			fileHandle.write(input);
		}catch(IOException e){
			fileMap.remove(fileHandle);
			throw e;
		}
		
		if(fileHandle.isDone()){
			try{
				fileHandle.close();
			} finally {
				fileMap.remove(fileHandle);
			}
			fileHandle.doChecksumValidation();
		}
		
		
	}
	
	class FileStreamHandle {
		String filePath;
		long fileSize;
		long checksumCRC32;
		File file;
		FileOutputStream fos;
		long wroteBytes;
		byte[] buf = new byte[1024];
		long startTime = System.currentTimeMillis();
		
		public FileStreamHandle(String filePath, long fileSize, long checksumCRC32) throws IOException{
			this.file = new File(filePath);
			this.filePath = filePath;
			this.fileSize = fileSize;
			this.checksumCRC32 = checksumCRC32;
			try {
				fos = new FileOutputStream(filePath);
			} catch (FileNotFoundException e) {
				logger.error("파일을 생성할수 없습니다.", e);
				throw new IOException("파일을 생성할수 없습니다.", e);
			}
		}
		
		public void close() throws IOException {
			fos.flush();
			fos.close();
		}

		public boolean isDone() {
//			logger.debug("isDone wrote = {}/ {}", wroteBytes, fileSize);
			if(wroteBytes == fileSize){
				logger.info("파일전송완료. time={}, file={}", Strings.getHumanReadableTimeInterval(System.currentTimeMillis() - startTime), filePath);
			}else if(wroteBytes > fileSize) {
				logger.error("파일 사이즈가 더 큽니다. actual={}, expected={}, file={}", new Object[]{wroteBytes, fileSize, filePath});
			}
			return wroteBytes == fileSize;
		}
		
		public void doChecksumValidation() throws IOException{
			//checksumCRC32
			long actualChecksum = FileUtils.checksumCRC32(file);
			if(actualChecksum != checksumCRC32){
				throw new IOException("파일의 checksum이 일치하지 않습니다.expected="+checksumCRC32+", actual="+actualChecksum+", file="+filePath);
			}else{
				logger.debug("파일검증완료. checksum={}, file={}", checksumCRC32, filePath);
			}
			
		}

		public synchronized void write(StreamInput input) throws IOException {
			int dataLength = input.readVInt();
			logger.debug("fileHandler write dataLength={}", dataLength);
			int nRead = 0;
			//파일사이즈가 0이라면 루프를 돌지 않는다.
			while(nRead < dataLength){
				int n = input.read(buf);
				//읽은 데이터가 0개 인경우 재시도. 
				if(n < 0){
					logger.error("읽을 데이터가 없습니다.filePath={}", filePath);
					break;
				}
				fos.write(buf, 0, n);
				wroteBytes += n;
				nRead += n;
			}
		}
	}
}
