package org.fastcatsearch.ir.io;

import static org.junit.Assert.*;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

import org.junit.Test;

/**
 * Java 기본 우선순위큐와 Fastcat의 우선순위큐 속도비교.
 * 거의 동일함.
 * */
public class PriorityQueueSpeedTest {

	int maxSize = 5000;
	int maxTest = 1000 * 10000;
	
	Random r;
	long seed = System.currentTimeMillis();
	
	@Test
	public void testFastcatSpeed() {
		
		r = new Random(seed);
		
		FixedMaxPriorityQueue<Integer> q = new FixedMaxPriorityQueue<Integer>(maxSize) {

			@Override
			protected int compare(Integer x, Integer y) {
				return (x < y) ? -1 : ((x == y) ? 0 : 1);
			}
		};
		long a = timeStart("Fastcat");
		for(int i=0; i < maxSize -1; i++) {
			q.push(getRandomValue());
		}
		for(int i=0;i < maxTest; i++) {
			q.push(getRandomValue());
			q.pop();
		}
		timeEnd("Fastcat", a);
	}
	
	

	private Integer getRandomValue(){
		return r.nextInt();
	}
	
	@Test
	public void testJavaSpeed() {
		r = new Random(seed);
		Comparator<Integer> comparator = new Comparator<Integer>(){

			@Override
			public int compare(Integer x, Integer y) {
				return (x < y) ? -1 : ((x == y) ? 0 : 1);
			}
			
		};
		PriorityQueue<Integer> q = new PriorityQueue<Integer>(maxSize, comparator);
		
		long a = timeStart("Java");
		for(int i=0; i < maxSize -1; i++) {
			q.offer(getRandomValue());
		}
		for(int i=0;i < maxTest; i++) {
			q.offer(getRandomValue());
			q.poll();
		}
		timeEnd("Java", a);
		
	}
	
	private void timeEnd(String string, long a) {
		System.out.println(string + " Finish. time=" + (System.currentTimeMillis() - a) +"ms");
		
	}

	private long timeStart(String string) {
		System.out.println(string + " Started.");
		return System.currentTimeMillis();
	}

}
