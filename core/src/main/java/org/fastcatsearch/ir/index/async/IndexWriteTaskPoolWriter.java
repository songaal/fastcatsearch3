package org.fastcatsearch.ir.index.async;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.SingleIndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexWriteTaskPoolWriter {
	private static Logger logger = LoggerFactory.getLogger(IndexWriteTaskPoolWriter.class);
	
	private BlockingQueue<IndexWriteTask> taskQueue;
	private IndexWriteTask[] writeTaskList;
	
	public IndexWriteTaskPoolWriter(BlockingQueue<IndexWriteTask> taskQueue, SingleIndexWriter[] singleIndexWriterList){
		this.taskQueue = taskQueue;
		this.writeTaskList = new IndexWriteTask[singleIndexWriterList.length];
		
		for (int i = 0; i < singleIndexWriterList.length; i++) {
			writeTaskList[i] = new IndexWriteTask(singleIndexWriterList[i]);
		}
	}
	
	public void write(Document doc, int docNo) throws IRException, IOException {
		for (int i = 0; i < writeTaskList.length; i++) {
			writeTaskList[i].setDocument(doc, docNo);
			try {
				taskQueue.put(writeTaskList[i]);
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
		for (int i = 0; i < writeTaskList.length; i++) {
			writeTaskList[i].waitUntilDone();
		}
	}
	
//	public void flush() {
//		if (count <= 0) {
//			return;
//		}
//		
//		for (int i = 0; i < indexSize; i++) {
//			searchIndexWriterList[i].flush();
//		}
//	}
}
