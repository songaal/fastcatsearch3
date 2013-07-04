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
//package org.fastcatsearch.ir.filter;
//
//import java.io.IOException;
//
//import org.fastcatsearch.ir.io.DataRef;
//import org.fastcatsearch.ir.io.FastByteBuffer;
//import org.fastcatsearch.ir.io.IOUtil;
//import org.fastcatsearch.ir.query.Filter;
//import org.fastcatsearch.ir.query.RankInfo;
//import org.fastcatsearch.ir.settings.FieldSetting;
//
///*
// * 리턴하는 부분만 제외하고 MatchFilter와 동일함.
// * MatchFilter코드 참고!!
// * */
//public class ExcludeFilter extends FilterFunction {
//	
//	public ExcludeFilter(Filter filter, FieldSetting fieldSetting) throws FilterException {
//		this(filter, fieldSetting, false);
//	}
//
//	public ExcludeFilter(Filter filter, FieldSetting fieldSetting, boolean isBoostFunction) throws FilterException {
//		super(filter, fieldSetting, isBoostFunction);
//	}
//
//	@Override
//	public boolean filtering(RankInfo rankInfo, DataRef dataRef, boolean skip) throws IOException {
//		
//		if(fieldSetting.multiValue){
//			int multiValueCount = IOUtil.readShort(buffer);
//			int endPos = buffer.pos() + fieldByteSize * (multiValueCount == 0 ? 1 : multiValueCount);
//			
//			if(skip){
//				buffer.pos(endPos);
//				return isBoostFunction;
//			}
//			
//			for (int m = 0; m < multiValueCount; m++) {
//				//멀티값의 각 밸류마다 필터링 확인을 한다.
//				int pos = buffer.pos();
//				for (int j = 0; j < patternCount; j++) {
//					FastByteBuffer patternBuf = patternList[j];
//					int plen = patternBuf.limit;
//					
//					//패턴이 데이터보다 크면 match확인필요없음.
//					if(plen > fieldByteSize){
//						continue;
//					}
//					if(plen > 0){
//						int len = (plen <= fieldByteSize)? plen : fieldByteSize;
//						boolean isMatch = true;
//						for (int k = 0; k < len; k++) {
//							if(patternBuf.array[k] != buffer.array[pos + k]){
//								isMatch = false;
//								break;
//							}
//						}
//						if(isMatch){
//							for(int bufInx = plen; bufInx < fieldByteSize; bufInx++) {
//								int value = (int) buffer.array[pos+bufInx] & 0xff;
//								if(value != 0){
//									//널이 아닌 데이터가 남아있다면 모두 매칭하지 않은것이다. 
//									isMatch = false;
//									break;
//								}
//							}
//							
//							if(isMatch){
//								//일치하는것이 하나라도 있으면 미리 끝낸다
//								//buffer를 다 읽지 않았으므로 position을 변경해준다.
//								buffer.pos(endPos);
//								
//								return isBoostFunction;
//								//return false;
//							}
//						}
//					}
//				}//for
//				
//				//다음 value
//				buffer.skip(fieldByteSize);
//			}
//			buffer.pos(endPos);
//			
//			//여기까지오면 매칭된것이 없으므로 제외필터링은 성공이다.
//			if(isBoostFunction){
//				rankInfo.addScore(boostScore);
//			}
//			return true;
//					
//		}else{
//			int endPos = buffer.pos() + fieldByteSize;
//			int pos = buffer.pos();
//			
//			if(skip){
//				buffer.pos(endPos);
//				return isBoostFunction;
//			}
//			
//			for (int j = 0; j < patternCount; j++) {
//				FastByteBuffer patternBuf = patternList[j];
//				int plen = patternBuf.limit;
//				
//				//패턴이 데이터보다 크면 match확인필요없음.
//				if(plen > fieldByteSize){
//					continue;
//				}
//				if(plen > 0){
//					int len = (plen <= fieldByteSize)? plen : fieldByteSize;
//					boolean isMatch = true;
//					for (int k = 0; k < len; k++) {
//						if(patternBuf.array[k] != buffer.array[pos + k]){
//							isMatch = false;
//							break;
//						}
//					}
//					
//					if(isMatch){
//						//위의 매치에서는 패턴의 길이만 큼만 비교했으므로, 
//						//남아있는 데이터가 더 있는지 확인해본다.
//						//널이 아닌 데이터가 남아있다면 모두 매칭하지 않은것이다.
//						for(int bufInx = plen; bufInx < fieldByteSize; bufInx++) {
//							int value = (int) buffer.array[pos+bufInx] & 0xff;
//							if(value != 0){
//								//널이 아닌 데이터가 남아있다면 모두 매칭하지 않은것이다. 
//								isMatch = false;
//								break;
//							}
//						}
//						if(isMatch){
//							//일치하는것이 하나라도 있으면 미리 끝낸다
//							//buffer를 다 읽지 않았으므로 position을 변경해준다.
//							buffer.pos(endPos);
//							return isBoostFunction;
//							//return false;
//						}
//					}
//				}
//			}//for
//			
//			if(isBoostFunction) {
//				rankInfo.addScore(boostScore);
//			}
//			
//			//다음 데이터를 읽을수 있도록 버퍼위치를 변경해준다.
//			buffer.pos(endPos);
//			
//			return true;
//		}
//	}
//}
