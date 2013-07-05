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

package org.fastcatsearch.ir.filter;

import java.io.IOException;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.io.ByteArrayOutput;
import org.fastcatsearch.ir.io.DataRef;
import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.io.IOUtil;
import org.fastcatsearch.ir.query.Filter;
import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.settings.FieldSetting;


/**
 * 패턴과 데이터비교시.
 * case 1) 패턴길이 > 셋팅필드길이 : not match
 * case 2) 패턴길이 <= 셋팅필드길이 : 데이터비교 진행.
 * @author swsong
 *
 */
public class MatchFilter extends FilterFunction {

	public MatchFilter(Filter filter, FieldSetting fieldSetting) throws FilterException {
		this(filter, fieldSetting, false);
	}
	public MatchFilter(Filter filter, FieldSetting fieldSetting, boolean isBoostFunction) throws FilterException {
		super(filter, fieldSetting, isBoostFunction);
	}
	
	@Override
	public boolean filtering(RankInfo rankInfo, DataRef dataRef) throws IOException {
		while(dataRef.next()){
			
			BytesRef bytesRef = dataRef.bytesRef();
			for (int j = 0; j < patternCount; j++) {
				BytesRef patternBuf = patternList[j];
				int plen = patternBuf.length;
				
				//패턴이 데이터보다 크면 match확인필요없음.
				if(plen > fieldByteSize){
					continue;
				}
				
				if(plen > 0){
					boolean isMatch = true;
					if(!patternBuf.bytesEquals(bytesRef, plen)){
						isMatch = false;
						break;
					}
					
//					for (int k = 0; k < len; k++) {
//						logger.debug("patternBuf.array[k] = {} : {} = buffer.array[pos + k]", patternBuf.array[k], buffer.array[pos + k]);
//						if(patternBuf.array[k] != buffer.array[pos + k]){
//							isMatch = false;
//							break;
//						}
//					}

					if(isMatch){
						//위의 매치에서는 패턴의 길이만 큼만 비교했으므로, 
						//남아있는 데이터가 더 있는지 확인해본다.
						//널이 아닌 데이터가 남아있다면 모두 매칭하지 않은것이다.
						
						for(int bufInx = plen; bufInx < fieldByteSize; bufInx++) {
							if(bytesRef.bytes[bufInx] != 0){
								isMatch = false;
								break;
							}
//							int value = (int) buffer.array[pos+bufInx] & 0xff;
//							if(value != 0){
//								//널이 아닌 데이터가 남아있다면 모두 매칭하지 않은것이다. 
//								isMatch = false;
//								break;
//							}
						}
						
						//최종적으로 isMatch일때에만 리턴한다.
						if(isMatch) {
							//일치하는것이 하나라도 있으면 미리 끝낸다
							//buffer를 다 읽지 않았으므로 position을 변경해준다.
							//logger.debug("Matched >> {}", new String(buffer.array, pos, fieldByteSize));
//							buffer.pos(endPos);
							if(isBoostFunction){
								//boost옵션이 있다면 점수를 올려주고 리턴한다.
								rankInfo.addScore(boostScore);
							}
							return true;
						}
						
						//여기까지 왔다면 매칭되지 않은것이다.
						//다음패턴으로 이동.
					}
					
				}
					
			} // for (int j = 0; j < patternCount; j++) {
			
		}
		
		return isBoostFunction;
		
	}
	
	
//	@Override
//	public boolean filtering2(RankInfo rankInfo, FastByteBuffer buffer, boolean skip) throws IOException {
//		if(fieldSetting.multiValue){
////			logger.debug("rankInfo = {}, buffer.pos={}", rankInfo.docNo(), buffer.pos());
//			int multiValueCount = IOUtil.readShort(buffer);
//			//2012-10-17 swsong
//			//multiValueCount 가 0개 이더라도 fixed size이므로 1개일 경우와 길이는 같다 
//			int endPos = buffer.pos() + fieldByteSize * (multiValueCount == 0 ? 1 : multiValueCount);
////			logger.debug("multiValueCount={}, endPos={}", multiValueCount, endPos);
//			
//			if(skip){
//				buffer.pos(endPos);
//				return isBoostFunction;
//			}
////			logger.debug("multiValueCount>> {}", multiValueCount);
//			for (int m = 0; m < multiValueCount; m++) {
//				//멀티값의 각 밸류마다 필터링 확인을 한다.
//				int pos = buffer.pos();
//				for (int j = 0; j < patternCount; j++) {
//					FastByteBuffer patternBuf = patternList[j];
//					int plen = patternBuf.limit; //remining을 사용해야하나 패턴은 항상 pos=0이므로 limit사용가능.
//					
//					//패턴이 데이터보다 크면 match확인필요없음.
//					if(plen > fieldByteSize){
//						continue;
//					}
//					
////					logger.debug("{}:{} Check match filter {}:{}, fieldByteSize={}", new Object[]{m,j, patternBuf.toAlphaString(), new String(buffer.array, pos, fieldByteSize), fieldByteSize});
//					
//					if(plen > 0){
//						int len = (plen <= fieldByteSize)? plen : fieldByteSize;
////						logger.debug("Check plen = {}", plen);
//						boolean isMatch = true;
//						for (int k = 0; k < len; k++) {
////							logger.debug("patternBuf.array[k] = {} : {} = buffer.array[pos + k]", patternBuf.array[k], buffer.array[pos + k]);
//							//TODO 차후에 대소문자 구별 색인을 한다면 FilterFunction.isTheCaseInsensitiveSame 를 사용한다.
//							//단, uchar에는 적용하면 안됨. 두 바이트글자의 하위바이트가 어떤 값이나올지 모름. 
//							if(patternBuf.array[k] != buffer.array[pos + k]){
//								isMatch = false;
//								break;
//							}
//						}
//	
//						if(isMatch){
//							//위의 매치에서는 패턴의 길이만 큼만 비교했으므로, 
//							//남아있는 데이터가 더 있는지 확인해본다.
//							//널이 아닌 데이터가 남아있다면 모두 매칭하지 않은것이다.
//							for(int bufInx = plen; bufInx < fieldByteSize; bufInx++) {
//								int value = (int) buffer.array[pos+bufInx] & 0xff;
//								if(value != 0){
//									//널이 아닌 데이터가 남아있다면 모두 매칭하지 않은것이다. 
//									isMatch = false;
//									break;
//								}
//							}
//							
//							//최종적으로 isMatch일때에만 리턴한다.
//							if(isMatch) {
//								//일치하는것이 하나라도 있으면 미리 끝낸다
//								//buffer를 다 읽지 않았으므로 position을 변경해준다.
//								//logger.debug("Matched >> {}", new String(buffer.array, pos, fieldByteSize));
//								buffer.pos(endPos);
//								if(isBoostFunction){
//									//boost옵션이 있다면 점수를 올려주고 리턴한다.
//									rankInfo.addScore(boostScore);
//								}
//								return true;
//							}
//							
//							//여기까지 왔다면 매칭되지 않은것이다.
//							//다음패턴으로 이동.
//						}
//						
//					}
//						
//				}//for
//				
//				//다음 value
//				buffer.skip(fieldByteSize);
//			}
//			buffer.pos(endPos);
//			
//			//여기까지 오면 false를 리턴해야 하지만 isBoostFunction의 경우는 무조건 true를 리턴한다.
//			return isBoostFunction;
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
//				
//				if(plen > 0){
//					int len = (plen <= fieldByteSize)? plen : fieldByteSize;
//	//				logger.debug(">> pattern-{} >> {}", j, patternBuf.toAlphaString());
//					boolean isMatch = true;
//					for (int k = 0; k < len; k++) {
//						//logger.debug(">> "+buf.array[k]+"("+(char)buf.array[k]+")"+" : "+buffer.array[pos + k]+"("+(char)buffer.array[pos + k]+")");
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
//						
//						//최종적으로 isMatch일때에만 리턴한다.
//						if(isMatch) {
//							//일치하는것이 하나라도 있으면 미리 끝낸다
//							//buffer를 다 읽지 않았으므로 position을 변경해준다.
//							buffer.pos(endPos);
//							if(isBoostFunction){
//								rankInfo.addScore(boostScore);
//							}
//							return true;
//						}
//						
//						//여기까지 왔다면 매칭되지 않은것이다.
//					}
//				}
//			}//for
//			
//			//일치하는것이 없었다.
//			//다음 데이터를 읽을수 있도록 버퍼위치를 변경해준다.
//			//buffer.skip(fieldByteSize);
//			buffer.pos(endPos);
//			
//			return isBoostFunction;
//		}
//	}
}
