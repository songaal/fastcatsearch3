/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RealTimeCollectionStatistics {
	private static Logger logger = LoggerFactory.getLogger(RealTimeCollectionStatistics.class);
	private String collection;
	private int accumulatedHit;//검색수

	private long accumulatedResponseTime;
	private long tempMaxResponseTime;
	private int timeCount;
	private long prevTimeMillis;

	//lock
	Object timeLock = new Object();
	Object hitLock = new Object();
	Object failHitLock = new Object();
	
	//보여주기필드
	private int hit;
	private int failHit;
	private int meanResponseTime;
	private int maxResponseTime;
	
	public RealTimeCollectionStatistics(String collection){
		this.collection = collection;
		prevTimeMillis = System.currentTimeMillis();
	}
	
	public String getCollectionName(){
		return collection;
	}
	
	//이전 시간부터 지금시간까지의 평균쿼리수를 반환한다. 1초에 한번씩 호출하면 초당평균쿼리수가 된다.
	public int getHitPerUnitTime(){
		return hit;
	}
	//평균실패쿼리수
	public int getFailHitPerUnitTime(){
		return failHit;
	}
	//평균응답시간
	public int getMeanResponseTime(){
		return meanResponseTime;
	}
	
	//최대응답시간
	public int getMaxResponseTime(){
		return maxResponseTime;
	}
	
	public int getAccumulatedHit() {
		return accumulatedHit;
	}

	public void setAccumulatedHit(int accumulatedHit) {
		this.accumulatedHit = accumulatedHit;
	}

	private int accumulatedFailHit;//실패검색수
	public int getAccumulatedFailHit() {
		return accumulatedFailHit;
	}

	public void setAccumulatedFailHit(int accumulatedFailHit) {
		this.accumulatedFailHit = accumulatedFailHit;
	}
	
	public void addKeyword(String keyword) {
//		keywordList[keywordCount] = keyword;
//		keywordCount++;
	}
	public void addTime(long responseTime) {
		synchronized(timeLock){
			if(responseTime > tempMaxResponseTime){
				tempMaxResponseTime = responseTime;
			}
			accumulatedResponseTime += responseTime;
			timeCount++;
		}
	}
	
	public void addHit() {
		synchronized(hitLock){
			accumulatedHit++;
		}
	}
	
	public void addFailHit() {
		synchronized(failHitLock){
			accumulatedFailHit++;
		}
	}
	
	public void checkHitStatictics(){
		double timeGap = (System.currentTimeMillis() - prevTimeMillis) / 1000.0;
		prevTimeMillis = System.currentTimeMillis();
		if(timeGap == 0) timeGap = 1; //0으로 나누기 에러 피하기. 
		synchronized(hitLock){
			hit = (int) Math.round(accumulatedHit / timeGap);
//			logger.debug("collection={}, accumulatedFailHit={}, gap={}, fh={}", new Object[]{collection, accumulatedFailHit, timeGap, failHit});
			accumulatedHit = 0;
		}
		synchronized(failHitLock){
			failHit = (int) Math.round(accumulatedFailHit / timeGap);
//			logger.debug("collection={}, accumulatedFailHit={}, gap={}, fh={}", new Object[]{collection, accumulatedFailHit, timeGap, failHit});
			accumulatedFailHit = 0;
		}
	}
	
	public void checkResponseTime(){
		synchronized(timeLock){
			if(timeCount > 0){
//				logger.debug("accumulatedResponseTime={}, timeCount={}",accumulatedResponseTime,timeCount);
				meanResponseTime = (int) (accumulatedResponseTime / timeCount);
				maxResponseTime = (int) tempMaxResponseTime;
				accumulatedResponseTime = 0;
				tempMaxResponseTime = 0;
				timeCount = 0;
			}else{
				//검색요청이 안들어오면 초기화만 해준다.
				meanResponseTime = 0;
				maxResponseTime = 0;
				accumulatedResponseTime = 0;
				tempMaxResponseTime = 0;
			}
		}
	}
	
	public void print(){
		logger.trace("[{}]hit = {}, failHit = {}, achit = {}, acfailHit = {}, meanResponseTime = {}, maxResponseTime = {}", new Object[]{collection, hit, failHit, accumulatedHit, accumulatedFailHit, meanResponseTime, maxResponseTime});
	}
	
	public void add(RealTimeCollectionStatistics stat){
		hit += stat.hit;
		failHit += stat.failHit;
		meanResponseTime += stat.meanResponseTime;
		if(stat.maxResponseTime > maxResponseTime)
			maxResponseTime = stat.maxResponseTime;
	}
	
	public RealTimeCollectionStatistics getAverage(int count){
		RealTimeCollectionStatistics stat = new RealTimeCollectionStatistics(collection);
		stat.hit = this.hit / count;
		stat.failHit = this.failHit / count;
		stat.meanResponseTime = this.meanResponseTime / count;
		stat.maxResponseTime = this.maxResponseTime;
		hit = 0;
		failHit = 0;
		meanResponseTime = 0;
		maxResponseTime = 0;
		return stat;
	}
	
}
