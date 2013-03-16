package org.fastcatsearch.transport.common;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ResultFuture {
	private BlockingQueue<Object> queue = new LinkedBlockingQueue<Object>();
	private boolean isSuccess;
	private long requestId;
	private Map<Long, ResultFuture> resultFutureMap;
	private long sentTime;
	
	public ResultFuture(long requestId, Map<Long, ResultFuture> resultFutureMap, long sentTime) {
		this.requestId = requestId;
		this.resultFutureMap = resultFutureMap;
		this.sentTime = sentTime;
	}

	public void put(Object result, boolean isSuccess) {
		this.isSuccess = isSuccess;
		try {
			queue.put(result);
		} catch (InterruptedException e) {
			//ignore
		}
	}
	
	public boolean isSuccess(){
		return isSuccess;
	}
	
	public Object take() {
		try {
			return queue.take();
		} catch (InterruptedException e) {
			//결과를 받지 못할경우, map에서 제거해준다.
			resultFutureMap.remove(requestId);
			return null;
		}
	}
	
	public Object poll(int timeInSecond) {
		long remainTime = timeInSecond * 1000 - (System.currentTimeMillis() - sentTime);
		try {
			if(remainTime > 0){
				Object result = queue.poll(remainTime, TimeUnit.SECONDS);
				if(result == null){
					//결과가 아직도착하지 않아서 받지못하거나, 네트워크 문제로 인해 전달이 안될수도 있으므로 불필요한 객체를 map에서 제거한다.
					resultFutureMap.remove(requestId);
				}
				return result;
			}else{
				Object result = queue.poll();
				if(result == null){
					resultFutureMap.remove(requestId);
				}
				return result;
			}
		} catch (InterruptedException e) {
			resultFutureMap.remove(requestId);
			return null;
		}
	}
}
