package org.fastcatsearch.util;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;


/**
 * 분 단위의 짧은 로그 데이터를 기록하는 로거.
 * */
public class RollingRawLogger {
	private static final Logger logger = LoggerFactory.getLogger(RollingRawLogger.class);

	private BufferedLogger aLogger;
	private File baseDir;
	private String targetFilename;
	private int sequence;

	public RollingRawLogger(File baseDir, String targetFilename) {
		this.baseDir = baseDir;
		if(!baseDir.exists()){
			baseDir.mkdirs();
		}
		this.targetFilename = targetFilename;
		this.sequence = readSequence();
		this.aLogger = new BufferedLogger(getTempFile(), true);
		logger.debug("RollongLogger use {}/{}, {}", baseDir.getAbsolutePath(), aLogger.getFile().getName(), targetFilename);
	}

	@Override
	public String toString(){
		return getClass().getSimpleName() + "] " + new File(baseDir, targetFilename).getAbsolutePath();
	}
	
	private int readSequence() {

		File sequenceFile = getSequenceFile();
		if (sequenceFile.exists()) {
			DataInputStream is = null;
			int s = 0;
			try {
				is = new DataInputStream(new FileInputStream(getSequenceFile()));
				s = is.readInt();
			} catch (IOException e) {
				logger.error("", e);
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException ignore) {
					}
				}
			}
			return s;
		} else {
			try {
				sequenceFile.createNewFile();
			} catch (IOException e) {
				logger.error("", e);
			}
			writeSequence();
			return sequence;
		}

	}

	private void writeSequence() {
		DataOutputStream os = null;
		try {
			os = new DataOutputStream(new FileOutputStream(getSequenceFile()));
			os.writeInt(sequence);
		} catch (IOException e) {
			logger.error("", e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException ignore) {
				}
			}
		}
	}

	// 0번째 요소는 카테고리Id 이다.
	public void log(String... data) {
		aLogger.log(data);
	}

	private File rollingRawLogger() {
		
		BufferedLogger prevLogger = aLogger;
		//먼저 새 로거를 만들어서 셋팅한다.
		int newSequence = (sequence + 1) % 2;
		File f = getTempFile();
		aLogger = new BufferedLogger(f);
		sequence = newSequence;
		writeSequence();
		
		if (prevLogger != null) {
			prevLogger.close();
			return prevLogger.getFile();
		}

		return null;
	}

	private File getSequenceFile() {
		return new File(baseDir, ".sequence");
	}

	private File getTempFile() {
		return new File(baseDir, System.nanoTime() + ".txt");
	}

	public void rolling() {

		File prevFile = rollingRawLogger();

		try {
			File f = new File(baseDir, targetFilename);
			logger.debug("rolling {}", f.getAbsolutePath());
			if(f.exists()){
				f.delete();
			}
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//			}
			FileUtils.moveFile(prevFile, f);
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//			}
		} catch (IOException e) {
			logger.error("", e);
		}
	}

	public void close() {

		if (aLogger != null) {
			aLogger.close();
		}
	}

}
