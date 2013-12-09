package org.fastcatsearch.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsynchronousFileLogger {
	private static final Logger logger = LoggerFactory.getLogger(AsynchronousFileLogger.class);
	
	private File logFile;
	private File storedFile;

	public AsynchronousFileLogger(File file) {
		logFile = file;
		storedFile = new File(logFile.getParentFile(), logFile.getName() +".store");
	}
	
	//1분에 한번 주기적 호출.
	public synchronized void logData(List<String> dataList) throws IOException {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true), "UTF-8"));
			for(String data: dataList){
				writer.write(data);
				logger.debug("write to File {}", data);
				writer.write('\n');
			}
		}finally{
			if(writer != null){
				try {
					writer.close();
				} catch (IOException ignore) {
				}
			}
		}
	}

	public File getStoredFile() {
		return storedFile;
	}

	public synchronized void storeAndReset() throws IOException {
		//move data to backUpFile
//		logger.debug("reset File {} > {}", logFile, storedFile);
		if(logFile.exists()){
			FileUtils.copyFile(logFile, storedFile);
			logFile.delete();
		}
	}
}
