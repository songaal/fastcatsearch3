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
public class PriorityQueueTest {

//	int maxSize = 1000;
//	int maxTest = 10000000;
	
	int maxSize = 30;
	int maxTest = 100;
	
	Random r;
	long seed = System.currentTimeMillis();
	
	@Test
	public void testFastcat() {
		
		r = new Random(seed);
		
		FixedMaxPriorityQueue<Integer> q = new FixedMaxPriorityQueue<Integer>(maxSize) {

			@Override
			protected int compare(Integer x, Integer y) {
				return (x < y) ? -1 : ((x == y) ? 0 : 1);
			}
		};
		long a = timeStart("Fastcat");
		for(int i=0; i < maxSize ; i++) {
			q.push(getRandomValue());
		}
		for(int i=0;i < maxTest; i++) {
			q.pop();
			q.push(getRandomValue());
		}
		timeEnd("Fastcat", a);
		
		validateQueue(q);
	}
	
	
	private Integer getRandomValue(){
		return r.nextInt(10000);
	}
	
	@Test
	public void testJava() {
		r = new Random(seed);
		Comparator<Integer> comparator = new Comparator<Integer>(){

			@Override
			public int compare(Integer x, Integer y) {
				return (x < y) ? 1 : ((x == y) ? 0 : -1);
			}
			
		};
		PriorityQueue<Integer> q = new PriorityQueue<Integer>(maxSize, comparator);
		
		long a = timeStart("Java");
		for(int i=0; i < maxSize ; i++) {
			q.offer(getRandomValue());
		}
		for(int i=0;i < maxTest; i++) {
			q.poll();
			q.offer(getRandomValue());
		}
		timeEnd("Java", a);

//		while(q.size() > 0) {
//			System.out.println(q.poll());
//		}
	}
	
	private void timeEnd(String string, long a) {
		System.out.println(string + " Finish. time=" + (System.currentTimeMillis() - a) +"ms");
		
	}

	private long timeStart(String string) {
		System.out.println(string + " Started.");
		return System.currentTimeMillis();
	}

	
	
	@Test
	public void testFastcatRemove() {
		int maxSize = 20;
		FixedMaxPriorityQueue<Integer> q = new FixedMaxPriorityQueue<Integer>(maxSize) {

			@Override
			protected int compare(Integer x, Integer y) {
				return (x < y) ? -1 : ((x == y) ? 0 : 1);
			}
		};
		long a = timeStart("FastcatRemove");
		for(int i=0; i < maxSize ; i++) {
			q.push(i+1);
		}
		System.out.println("rm "+ q.remove(5));
		q.push(5);
		System.out.println("rm "+ q.remove(7));
//		q.printHeap("remove7");
		q.push(7);
//		q.printHeap("push7");
		System.out.println("rm "+ q.remove(10));
		q.push(10);
		System.out.println("rm "+ q.remove(15));
		q.push(15);
		q.push(19);
		q.remove(0);
		q.push(20);
		timeEnd("FastcatRemove", a);
		
		validateQueue(q);
		
	}
	
	
	private void validateQueue(FixedMaxPriorityQueue<Integer> q) {
		int prev = Integer.MAX_VALUE;
		while(q.size() > 0) {
			int i = q.pop();
			System.out.println(">>"+i);
			assertTrue(i <= prev);
			prev =i;
		}
	}


	@Test
	public void testFastcatReplace() {
		int maxSize = 20;
		FixedMaxPriorityQueue<Integer> q = new FixedMaxPriorityQueue<Integer>(maxSize) {

			@Override
			protected int compare(Integer x, Integer y) {
				return (x < y) ? -1 : ((x == y) ? 0 : 1);
			}
		};
		long a = timeStart("FastcatRemove");
		for(int i=0; i < maxSize ; i++) {
			q.push(i+1);
		}
		System.out.println("rm "+ q.remove(5));
		q.push(5);
		System.out.println("rm "+ q.remove(7));
//		q.printHeap("remove7");
		q.push(7);
//		q.printHeap("push7");
		System.out.println("rm "+ q.remove(10));
		q.push(10);
		System.out.println("rm "+ q.remove(15));
		q.push(15);
		q.push(19);
		for(int i=0; i < maxSize ; i++) {
			q.replace(i+1, i);
		}
		q.push(20);
		timeEnd("FastcatRemove", a);
		
		
		validateQueue(q);
	}
}
