package org.fastcatsearch.common.io;

import static org.junit.Assert.*;

import org.fastcatsearch.common.io.BlockingCachedStreamOutput.Entry;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockingCachedStreamOutputTest {
	protected static Logger logger = LoggerFactory.getLogger(BlockingCachedStreamOutputTest.class);
	
	@Test
	public void test() {
		final BlockingCachedStreamOutput cache = new BlockingCachedStreamOutput(15, 1024);
		int TEST_COUNT = 100;
		Thread[] threadList = new Thread[TEST_COUNT];
		for (int i = 0; i < TEST_COUNT; i++) {
			threadList[i] = new Thread("thread-"+i){
				@Override
				public void run(){
//					logger.debug("{} pop buffer", getName());
					long st = System.currentTimeMillis();
					logger.debug("### cache >> {}", cache);
					Entry entry = cache.popEntry();
					logger.debug("{} pop buffer {} - {}ms", new Object[]{getName(), entry, System.currentTimeMillis() - st});
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					logger.debug("{} push buffer >> {}", getName(), entry);
					cache.pushEntry(entry);
					logger.debug("### cache >> {}", cache);
				}
			};
		}
		
		for (int i = 0; i < TEST_COUNT; i++) {
			threadList[i].start();
		}
		
		for (int i = 0; i < TEST_COUNT; i++) {
			try {
				threadList[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		logger.debug("### Final cache >> {}", cache);
	}

}
