///*
// * Copyright 2013 Websquared, Inc.
// * 
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * 
// *   http://www.apache.org/licenses/LICENSE-2.0
// * 
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.fastcatsearch.ir.analysis;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.fastcatsearch.ir.config.IRSettings;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//
//
//public class TokenizerPool {
//	private static Logger logger = LoggerFactory.getLogger(TokenizerPool.class);
//	public static Map<String, TokenizerPool> poolMap = new HashMap<String, TokenizerPool>();
//	
//	private static final int MAX_SIZE = 100;
//	private static final int CORE_SIZE = 5;
//	
//	public List<Tokenizer> pool = new ArrayList<Tokenizer>();
//	private String tokenizerName;
//	
//	public synchronized static TokenizerPool getPool(String tokenizerName){
//		TokenizerPool pool = poolMap.get(tokenizerName);
//		if(pool != null){
//			return pool;
//		}else{
//			pool = new TokenizerPool(tokenizerName);
//			poolMap.put(tokenizerName, pool);
//			return pool;
//		}
//	}
//	
//	private TokenizerPool(String tokenizerName){
//		this.tokenizerName = tokenizerName; 
//		for (int i = 0; i < CORE_SIZE; i++) {
//			pool.add((Tokenizer)IRSettings.classLoader.loadObject(tokenizerName));
//		}
//		logger.info("Init TokenizerPool "+tokenizerName+", size = "+pool.size());
//	}
//	
//	//synchronized
//	public synchronized Tokenizer getFromPool(){
//		Tokenizer e = null;
//		if(pool.size() == 0){
//			e = (Tokenizer)IRSettings.classLoader.loadObject(tokenizerName);
////			logger.debug("Create tokenizer "+tokenizerName);
//		}
//		else
//			e = pool.remove(pool.size() - 1);
//		
//		return e;
//	}
//	
//	//synchronized
//	public synchronized void releaseToPool(Tokenizer e){
//		if(e == null) return;
//		if(pool.size() < MAX_SIZE){
//			pool.add(e);
////			logger.debug("Release tokenizer "+tokenizerName+", size = "+pool.size());
//		}
//		//현재 사이즈가 MAX_SIZE를 초과하면 버린다.
//		
//	}
//	
//	public synchronized int size(){
//		return pool.size();
//	}
//	
//	public String toString(){
//		return "Tokenizer "+tokenizerName+", size = "+pool.size();
//	}
//	
//}
