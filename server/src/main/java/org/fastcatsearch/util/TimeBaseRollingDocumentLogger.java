package org.fastcatsearch.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * */
public class TimeBaseRollingDocumentLogger {
	private static final Logger logger = LoggerFactory.getLogger(TimeBaseRollingDocumentLogger.class);
    private long flushPeriodInNanoseconds;
    private long rollingPeriodInNanoseconds;
	private int bufferSize;
	private File dir;
	private String encoding;
	private List<String> memoryData;
    private Timer flushTimer;
    private long lastFlushTime;
    private long fileOpenTime;
    private BlockingQueue<LogFileStatus> fileQueue;
    private Object lock = new Object();
    private Writer logWriter;
    private LogFileStatus currentLogFileStatus;
	public TimeBaseRollingDocumentLogger(File dir, int flushPeriodInSeconds, int rollingPeriodInSeconds) {
		this(dir, "utf-8", flushPeriodInSeconds, rollingPeriodInSeconds);
	}

	public TimeBaseRollingDocumentLogger(File dir, String encoding, int flushPeriodInSeconds, int rollingPeriodInSeconds) {
		this.bufferSize = 10000;
		this.encoding = encoding;
        this.flushPeriodInNanoseconds = ((long) flushPeriodInSeconds) * 1000 * 1000 * 1000;
        this.rollingPeriodInNanoseconds = ((long) rollingPeriodInSeconds) * 1000 * 1000 * 1000;
		this.memoryData = newMemoryData();
        flushTimer = new Timer();
        long flushCheckPeriod = 500;
        flushTimer.schedule(new FlushCheckTask(), flushCheckPeriod, flushCheckPeriod);
		this.dir = dir;
		if (!dir.exists()) {
			dir.mkdirs();
		}
        fileQueue = new LinkedBlockingQueue<LogFileStatus>();
        TreeSet<File> sorter = new TreeSet<File>(new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                long c = Long.parseLong(o1.getName()) - Long.parseLong(o2.getName());
                return c > 0 ? 1 : c < 0 ? -1 : 0;
            }
        });
        FilenameFilter filenameFilter =  new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                try {
                    Long.parseLong(name);
                    return true;
                } catch(NumberFormatException e) {
                    return false;
                }
            }
        };

        for(File f : dir.listFiles(filenameFilter)) {
            sorter.add(f);
        }

        Iterator<File> iter = sorter.iterator();
        while(iter.hasNext()) {
            File f = iter.next();
            logger.info("[{}] indexlog file = {}", dir.getParentFile().getName(), f.getName());
            fileQueue.add(new LogFileStatus(f, true)); //문서가 닫힘으로 읽어들인다.
        }

        lastFlushTime = System.nanoTime();
        fileOpenTime = System.nanoTime();
    }

    public int getQueueSize() {
        return fileQueue.size();
    }
    public void close() {
        flushTimer.cancel();
        flush();
        closeFile();
    }

    public BlockingQueue<LogFileStatus> getFileQueue() {
        return fileQueue;
    }

	public int size() {
		return memoryData.size();
	}

	private List<String> newMemoryData() {
		return new ArrayList<String>(bufferSize);
	}

    public void log(String data) {
        if (data != null){
            if(data.length() > 0) {
                synchronized (lock) {
                    memoryData.add(data);
                }
            }
        }
    }

	private void flush() {
        if (memoryData.size() == 0) {
			return;
		}
        List<String> oldData = null;
        synchronized (lock) {
            oldData = memoryData;
            this.memoryData = newMemoryData();
        }

        File file = null;
        Exception ex = null;
        boolean isCreated = false;
        try {
            if(logWriter == null) {
                fileOpenTime = System.nanoTime();
                file = new File(dir, String.valueOf(fileOpenTime));
                logWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), encoding));
                isCreated = true;
            }
			logger.debug("## flush docs > {}", oldData.size());
			for (String data : oldData) {
                logWriter.write(data);
                logWriter.write("\n");
			}
            logWriter.flush();
		} catch (IOException e) {
			logger.error("", e);
            ex = e;
		} finally {
            lastFlushTime = System.nanoTime();
            if(isCreated && ex == null && file != null) {
                currentLogFileStatus = new LogFileStatus(file);
                fileQueue.add(currentLogFileStatus);
            }
        }
	}

    //롤링시간이 지나면 파일을 닫아준다.
    private void closeFile() {
        if(logWriter != null) {
            try {
                logWriter.close();
                if(currentLogFileStatus != null) {
                    currentLogFileStatus.setIsClosed(true);
                }
            } catch (IOException e) {
                logger.error("", e);
            }
        }
        logWriter = null;
    }

    class FlushCheckTask extends TimerTask {
        @Override
        public void run() {
            if (memoryData.size() > 0) {
                if (System.nanoTime() - lastFlushTime > flushPeriodInNanoseconds) {
                    logger.debug("flush task");
                    flush();
                }
            }

            //flush 가 한번이라도 되어야 파일을 rolling한다.
            if(logWriter != null) {
                if (System.nanoTime() - fileOpenTime > rollingPeriodInNanoseconds) {
                    logger.debug("rolling task");
                    closeFile();
                }
            }
        }
    }

    public static class LogFileStatus {
        private File f;
        private boolean isClosed;

        public LogFileStatus(File f, boolean isClosed) {
            this.f = f;
            this.isClosed = isClosed;
        }

        public LogFileStatus(File f) {
            this.f = f;
        }

        public File getFile() {
            return f;
        }

        public boolean isClosed() {
            return isClosed;
        }

        public void setIsClosed(boolean isClosed) {
            this.isClosed = isClosed;
        }
    }
}
