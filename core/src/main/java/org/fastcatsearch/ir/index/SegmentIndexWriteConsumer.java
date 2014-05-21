package org.fastcatsearch.ir.index;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.document.Document;

public class SegmentIndexWriteConsumer extends Thread {
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
		q.notifyAll();
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
					w.addDocument(document);
				}
				if(requestDone) {
					break;
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IRException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		latch.countDown();
	}
}
