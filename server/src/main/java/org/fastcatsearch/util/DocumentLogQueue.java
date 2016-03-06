package org.fastcatsearch.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 *
 * */
public class DocumentLogQueue {
	private static final Logger logger = LoggerFactory.getLogger(DocumentLogQueue.class);
    private long flushPeriodInNanoseconds;
	private int bufferSize;
	private File dir;
	private String encoding;
	private List<String> memoryData;
    private Timer flushTimer;
    private long lastFlushTime;
    private Queue<File> fileQueue;
    private Object lock = new Object();

	public DocumentLogQueue(File dir, int flushPeriodInSeconds) {
		this(dir, "utf-8", flushPeriodInSeconds);
	}

	public DocumentLogQueue(File dir, String encoding, int flushPeriodInSeconds) {
		this.bufferSize = 10000;
		this.encoding = encoding;
        this.flushPeriodInNanoseconds = ((long) flushPeriodInSeconds) * 1000 * 1000 * 1000;
		this.memoryData = newMemoryData();
        flushTimer = new Timer();
        long flushCheckPeriod = 500;
        flushTimer.schedule(new FlushCheckTask(), flushCheckPeriod, flushCheckPeriod);
		this.dir = dir;
		if (!dir.exists()) {
			dir.mkdirs();
		}
        fileQueue = new ArrayDeque<File>();
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
//            logger.info("index file = " + f.getName());
            fileQueue.add(f);
        }
    }

    public int getQueueSize() {
        return fileQueue.size();
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
		Writer writer = null;
        File file = null;
        Exception ex = null;
		try {
			// append로 연다.
            file = new File(dir, String.valueOf(System.nanoTime()));
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), encoding));
			logger.debug("## flush docs > {}", oldData.size());
			for (String data : oldData) {
				writer.write(data);
                writer.write("\n");
			}
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
                if (System.nanoTime() - lastFlushTime > flushPeriodInNanoseconds) {
                    logger.debug("flush task");
                    flush();
                }
            }
        }

    }
}
