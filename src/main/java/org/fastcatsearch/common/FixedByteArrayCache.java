package org.fastcatsearch.common;

import java.lang.ref.SoftReference;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;


public class FixedByteArrayCache {
	private SoftReference<Queue<byte[]>> cache;
	private final AtomicInteger counter = new AtomicInteger();
    private int cacheSizeLimit;
    private int bufferSize;
    
    public FixedByteArrayCache(int bufferSize){
    	this(bufferSize, 100);
    }
	public FixedByteArrayCache(int bufferSize, int cacheSizeLimit){
		this.bufferSize = bufferSize;
		this.cacheSizeLimit = cacheSizeLimit;
		Queue<byte[]> ref = new ConcurrentLinkedQueue<byte[]>();
		cache = new SoftReference<Queue<byte[]>>(ref);
	}
	
	public byte[] pop() {
        Queue<byte[]> ref = cache.get();
        if (ref == null) {
            return new byte[bufferSize];
        }
        byte[] array = ref.poll();
        if (array == null) {
        	return new byte[bufferSize];
        }
        counter.decrementAndGet();
        return array;
    }

    public void push(byte[] array) {
        if (array.length != bufferSize) {
            return;
        }
        
        Queue<byte[]> ref = cache.get();
        if (ref == null) {
        	ref = new ConcurrentLinkedQueue<byte[]>();
            counter.set(0);
            cache = new SoftReference<Queue<byte[]>>(ref);
        }
        if (counter.incrementAndGet() > cacheSizeLimit) {
            counter.decrementAndGet();
        } else {
            ref.add(array);
        }
    }
}
