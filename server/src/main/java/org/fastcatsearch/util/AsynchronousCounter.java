package org.fastcatsearch.util;

import java.util.concurrent.atomic.AtomicInteger;

import org.fastcatsearch.ir.util.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 호출 갯수를 카운트하기 위한 클래스.
 *  incrementCount로 카운트를 증가시키고, resetCount를 호출하여 갯수를 저장하고 0부터 다시 카운트하기 시작한다.
 *  비동기적으로 일정주기동안의 합산 갯수를 가져갈때 유용하게 사용될수 있다.
 * */
public class AsynchronousCounter implements Counter {
	protected static Logger logger = LoggerFactory.getLogger(AsynchronousCounter.class);
			
	private AtomicInteger runningCount;
	private AtomicInteger storedCount;
	
	public AsynchronousCounter(){
		runningCount = new AtomicInteger();
		storedCount = new AtomicInteger();
	}
	
	@Override
	public void incrementCount(){
		int c = runningCount.incrementAndGet();
	}
	
	public void addCount(int count){
		runningCount.addAndGet(count);
	}
	
	public int getLastCount(){
		return storedCount.intValue();
	}
	
	//통계를 0으로 만든다.
	//[0]에는 수치를 계속 더해가고, 주기가 되면 완료된 수치를 [1]로 옮기고 [0]을 0으로 만든다. 
	public int resetCount(){
		int count = runningCount.get();
		storedCount.lazySet(count);
		runningCount.set(0);
		return count;
	}
	
	
}
