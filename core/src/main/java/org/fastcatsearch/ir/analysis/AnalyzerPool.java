/*
 * Copyright 2013 Websquared, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fastcatsearch.ir.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 각 analyzer마다 하나의 pool을 가지고 있다.
 * 각 pool은 설정된 factory를 이용해 생성된다.
 * analyzer에 필요한 파라미터또는 로직등의 factory에 담겨있다. 
 * */
public class AnalyzerPool {
	private static Logger logger = LoggerFactory.getLogger(AnalyzerPool.class);
	
	private final String analyzerName;
	private final AnalyzerFactory factory;
	private final int corePoolSize;
	private final int maximumPoolSize;
	private List<Analyzer> pool = new ArrayList<Analyzer>();
	
	public AnalyzerPool(String analyzerName, AnalyzerFactory factory, int corePoolSize, int maximumPoolSize) {
		this.analyzerName = analyzerName;
		this.factory = factory;
		this.corePoolSize = corePoolSize;
		this.maximumPoolSize = maximumPoolSize;
		
		for (int i = 0; i < corePoolSize; i++) {
			pool.add(factory.create());
		}
	}
	
	//synchronized
	public synchronized Analyzer getFromPool(){
		Analyzer e = null;
		if(pool.size() == 0){
			e = (Analyzer) factory.create();
		}else{
			e = pool.remove(pool.size() - 1);
		}
		
		return e;
	}
	
	//synchronized
	public synchronized void releaseToPool(Analyzer e){
		if(e == null) return;
		if(pool.size() < maximumPoolSize){
			pool.add(e);
		}
		//현재 사이즈가 CORE_SIZE를 초과하면 버린다.
	}
	
	public synchronized int size(){
		return pool.size();
	}
	
	public String toString(){
		return "Analyzer "+analyzerName+", current size = "+pool.size();
	}
	
}
