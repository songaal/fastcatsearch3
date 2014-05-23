package org.fastcatsearch.transport.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.common.Strings;
import org.fastcatsearch.env.Path;
import org.fastcatsearch.ir.io.DataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileTransportHandler {
	private static Logger logger = LoggerFactory.getLogger(FileTransportHandler.class);
	
	private Map<String, FileStreamHandle> fileMap;
	private Path path;
	
	public FileTransportHandler(Path path){
		fileMap = new ConcurrentHashMap<String, FileStreamHandle>();
		this.path = path;
	}
	
	//마지막 청크기록으로 모두 끝났다면 true 전송.
	//IO에러 또는 체크섬 불일치 에러는 IOException를 던진다.
	public boolean handleFile(int seq, String filePath, long fileSize, long checksumCRC32, String fileKey, DataInput input) throws IOException {
		FileStreamHandle fileHandle = fileMap.get(fileKey);
		if(seq == 0){
			//새로 생성.
			if(fileHandle == null){
				File file = path.makePath(filePath).file();
				logger.debug("## {} >> {}", filePath, file);
				try {
//					File file = new File(absolutePath);
					File dir = file.getParentFile();
					if(dir != null){
						dir.mkdirs();
					}
					fileHandle = new FileStreamHandle(file, fileSize, checksumCRC32);
					fileMap.put(fileKey, fileHandle);
					logger.debug("File handle map size = {}", fileMap.size());
				} catch (FileNotFoundException e) {
					logger.error("파일을 생성할수 없습니다.", e);
					throw new IOException("파일을 생성할수 없습니다.", e);
				}
			}else{
				//발생하면 안되는 경우.
				logger.error("seq가 0인데 map에 동일 파일수신이 남아있습니다.seq={}, filePath={}", seq, filePath);
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
			fileMap.remove(fileKey);
			throw e;
		}
		
		if(fileHandle.isDone()){
			try{
				fileHandle.close();
			} finally {
				fileMap.remove(fileKey);
			}
			fileHandle.doChecksumValidation();
			
			//모두 기록했다면 true
			return true;
		}
		
		return false;
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
		
		public FileStreamHandle(File file, long fileSize, long checksumCRC32) throws IOException{
			this.file = file;
			this.filePath = file.getAbsolutePath();
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
		
		public long wroteBytes(){
			return wroteBytes;
		}
		
		public long fileSize(){
			return fileSize;
		}
		
		public boolean isDone() {
//			logger.debug("isDone wrote = {}/ {}", wroteBytes, fileSize);
			if(wroteBytes == fileSize){
				logger.info("파일수신완료. time={}, file={}", Strings.getHumanReadableTimeInterval(System.currentTimeMillis() - startTime), filePath);
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

		public synchronized void write(DataInput input) throws IOException {
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
