package org.fastcatsearch.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 1초에 한번씩 확인해가면서 max_row를 넘어서면 file로 flush한다. 단일 type의 log파일만 관리한다.
 * 
 * */
public class BufferedLogger {
	private static final Logger logger = LoggerFactory.getLogger(BufferedLogger.class);

	private final int period = 1000; // 1초
	private String delimiter;
	private int bufferSize;
	private File file;
	private String encoding;
	private List<String[]> memoryData;
	private Timer flushTimer;
	private SimpleDateFormat hourMinuteTimeFormat;
	
	public BufferedLogger(File file) {
		this(file, false);
	}
	public BufferedLogger(File file, boolean append) {
		this(1000, file, append);
	}
	public BufferedLogger(int bufferSize, File file, boolean append) {
		this(bufferSize, file, "utf-8", "\t", append);
	}

	public BufferedLogger(int bufferSize, File file, String encoding, String delimiter, boolean append) {
		this.bufferSize = bufferSize;
		this.file = file;
		this.encoding = encoding;
		this.delimiter = delimiter;
		this.memoryData = newMemoryData();
		this.hourMinuteTimeFormat = new SimpleDateFormat("HH:mm");
		flushTimer = new Timer();
		FlushTask task = new FlushTask();
		flushTimer.schedule(task, period, period);

		File dir = file.getParentFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				logger.error("");
			}
		}else{
			if(!append){
				file.delete();
//				try {
//					Thread.sleep(100);
//				} catch (InterruptedException e1) {
//				}
				try {
					file.createNewFile();
				} catch (IOException e) {
					logger.error("");
				}
			}
		}
	}

	public int size() {
		return memoryData.size();
	}

	private List<String[]> newMemoryData() {
		return new ArrayList<String[]>((int) (bufferSize * 1.2));
	}

	public void flush() {
		//logger.debug("flush memoryData > {} ", memoryData);
		if (memoryData.size() == 0) {
			return;
		}
		
		Writer writer = null;
		try {
			// append로 연다.
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), encoding));
			List<String[]> oldData = memoryData;
			this.memoryData = newMemoryData();
			String timeFormatString = hourMinuteTimeFormat.format(new Date()); 
//			logger.debug("flush data > {} : {}", oldData, file.getAbsolutePath());
			for (String[] data : oldData) {
				
				writer.append(timeFormatString);
				
				for (int i = 0; i < data.length; i++) {
					writer.append(delimiter);
					writer.append(data[i]);
				}
				writer.append("\n");
			}
		} catch (UnsupportedEncodingException e) {
			logger.error("");
		} catch (IOException e) {
			logger.error("");
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException ignore) {
				}
			}
		}
	}

	public void log(String... data) {
		if (data != null && data.length > 0) {
			memoryData.add(data);
		}
	}

	public File getFile(){
		return file;
	}
	public void close() {
		flush();
		
		if (flushTimer != null) {
			flushTimer.cancel();
		}
	}

	class FlushTask extends TimerTask {

		private int tryLimit;
		private int tryCount;
		
		public FlushTask(){
			this(5);
		}
		
		public FlushTask(int tryLimit){
			this.tryLimit = tryLimit;
		}
		@Override
		public void run() {
			if (memoryData.size() >= bufferSize || tryCount >= tryLimit) {
				flush();
				tryCount = 0;
			}else {
				tryCount++;
			}
		}

	}
}
