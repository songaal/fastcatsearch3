package org.fastcatsearch.ir.index.async;

import java.io.IOException;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.SingleIndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexWriteTask {
	
	private static Logger logger = LoggerFactory.getLogger(IndexWriteTask.class);
			
	private SingleIndexWriter w;
	private Document doc;
	private int docNo;
	
	private int resultCode;
	private IRException exception;
	
	public IndexWriteTask(SingleIndexWriter w) {
		this.w = w;
	}
	
	public void setDocument(Document doc, int docNo) {
		this.doc = doc;
		this.docNo = docNo;
		this.resultCode = 0;
		this.exception = null;
	}
	
	@Override
	public String toString() {
		return w.toString() + " #" + docNo;
	}
	
	public void run() {
		try {
//			logger.debug("write {} > {}", w.toString(), docNo);
			w.write(doc, docNo);
		} catch (IRException e) {
			exception = e;
			logger.error("", e);
		} catch (IOException e) {
			exception = new IRException(e);
			logger.error("", e);
		} finally {
			synchronized(this) {
				notifyAll();
				resultCode = (exception != null) ? 1 : -1;
			}
		}
	}
	
	public void waitUntilDone() throws IRException {
//		long l = System.nanoTime();
		synchronized(this) {
			while(resultCode == 0) {
				try {
					wait(100);
					
//					if(resultCode == 0) {
//						logger.debug("resultCode = {}", resultCode);
//					}
				} catch (InterruptedException e) {
					throw new IRException(e);
				}
			}
		}
//		logger.debug("[{}] {} > {}", System.nanoTime() - l, w, docNo);
		if(exception != null) {
			throw exception;
		}
	}
}
