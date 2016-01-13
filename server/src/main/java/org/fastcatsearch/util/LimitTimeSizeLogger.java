package org.fastcatsearch.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 *
 * */
public class LimitTimeSizeLogger {
	private static final Logger logger = LoggerFactory.getLogger(LimitTimeSizeLogger.class);
    private long flushCheckPeriod = 200;
    private long flushPeriodInNanoseconds;
	private int bufferSize;
	private File dir;
	private String encoding;
	private List<String> memoryData;
    private Timer flushTimer;
    private long lastFlushTime;
    private Queue<File> fileQueue;

	public LimitTimeSizeLogger(File dir, int bufferSize, int flushPeriod) {
		this(dir, bufferSize, "utf-8", flushPeriod);
	}

	public LimitTimeSizeLogger(File dir, int bufferSize, String encoding, int flushPeriod) {
		this.bufferSize = bufferSize;
		this.encoding = encoding;
        this.flushPeriodInNanoseconds = flushPeriod * 1000 * 1000 * 1000;
		this.memoryData = newMemoryData();
        flushTimer = new Timer();
        flushTimer.schedule(new FlushCheckTask(), flushCheckPeriod, flushCheckPeriod);
		this.dir = dir;
		if (!dir.exists()) {
			dir.mkdirs();
		}
        fileQueue = new ArrayDeque<File>();
	}

    public void close() {
        flushTimer.cancel();
        flush();
    }

    public File pollFile() {
        return fileQueue.poll();
    }
    public int fileCount() {
        return fileQueue.size();
    }

	public int size() {
		return memoryData.size();
	}

	private List<String> newMemoryData() {
		return new ArrayList<String>(bufferSize);
	}

    private File newFile() {
        return new File(dir, String.valueOf(System.nanoTime()));
    }
    public void log(String data) {
        if (data != null){
            if(data.length() > 0) {
                memoryData.add(data);
                if (memoryData.size() >= bufferSize) {
                    logger.debug("flush MANUAL");
                    flush();
                }
            }
        }

    }
	private synchronized void flush() {
		if (memoryData.size() == 0) {
			return;
		}
		
		Writer writer = null;
        File file = null;
        Exception ex = null;
		try {
			// append로 연다.
            file = newFile();
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile(), false), encoding));
//			logger.debug("flush data > {} : {}", oldData, file.getAbsolutePath());
			for (String data : memoryData) {
				writer.write(data);
                writer.write("\n");
			}
            this.memoryData = newMemoryData();
		} catch (UnsupportedEncodingException e) {
			logger.error("", e);
            ex = e;
		} catch (IOException e) {
			logger.error("", e);
            ex = e;
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException ignore) {
				}
			}
            lastFlushTime = System.nanoTime();
            if(ex == null && file != null) {
                fileQueue.offer(file);
            }
		}
	}

    class FlushCheckTask extends TimerTask {

        @Override
        public void run() {
            if (memoryData.size() > 0) {
                if(System.nanoTime() - lastFlushTime > flushPeriodInNanoseconds) {
                    logger.debug("flush task");
                    flush();
                }
            }
        }

    }
}
