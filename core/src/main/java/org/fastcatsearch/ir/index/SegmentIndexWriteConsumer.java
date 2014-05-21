package org.fastcatsearch.ir.index;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SegmentIndexWriteConsumer extends Thread {
	protected static Logger logger = LoggerFactory.getLogger(SegmentIndexWriteConsumer.class);
	
	String segmentId;
	IndexWritable w;
	BlockingQueue<Document> q;
	CountDownLatch latch;
	boolean requestDone;
	
	public SegmentIndexWriteConsumer(String segmentId, IndexWritable w, BlockingQueue<Document> q, CountDownLatch latch) {
		
		this.segmentId = segmentId;
		this.w = w;
		this.q = q;
		this.latch = latch;
		
		setName("segment-writer-" + segmentId);
		setDaemon(true);
	}

	public void requestDone(){
		this.requestDone = true;
	}
	
	public IndexWritable getWriter(){
		return w;
	}
	@Override
	public void run() {
		try {
			while (true) {
				Document document = q.poll(500, TimeUnit.MILLISECONDS);
				if(document != null) {
//					logger.debug("############### {} / q={}", document.get(0).toString(), q.size());
					w.addDocument(document);
				}
				if(requestDone && q.size() == 0) {
					break;
				}
			}
		} catch (Throwable e) {
			logger.error("", e);
		}
		latch.countDown();
	}
}
