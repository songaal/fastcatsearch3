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
import org.fastcatsearch.ir.io.DataRef;
import org.fastcatsearch.ir.query.Filter;
import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.settings.FieldIndexSetting;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 범위조건을 지정할수 있다.
 * ~를 구분자로 시작과 끝범위를 지정하며, ;를 사용하여 여러범위를 포함할수있다.
 * 시작패턴과 끝패턴이 함께 입력되어야 하며, 1000~ 와 같은 표현을 지원하지 않는다.
 * @author swsong
 *
 */
public class SectionFilter extends FilterFunction {

	private static final Logger logger = LoggerFactory.getLogger(SectionFilter.class);
	
	public SectionFilter(Filter filter, FieldIndexSetting fieldIndexSetting, FieldSetting fieldSetting) throws FilterException {
		super(filter, fieldIndexSetting, fieldSetting, false);
	}
	public SectionFilter(Filter filter, FieldIndexSetting fieldIndexSetting, FieldSetting fieldSetting, boolean isBoostFunction) throws FilterException {
		super(filter, fieldIndexSetting, fieldSetting, isBoostFunction);
	}

	@Override
	public boolean filtering(RankInfo rankInfo, DataRef dataRef)
			throws IOException {
		fieldSetting.isNumericField();
		while(dataRef.next()) {
			BytesRef bytesRef = dataRef.bytesRef();
			for (int j = 0; j < patternCount; j++) {
				BytesRef patternBuf1 = patternList[j];
				BytesRef patternBuf2 = endPatternList[j];
				int plen1 = patternBuf1.length;
				int plen2 = patternBuf2.length;
				
				//크기비교에서 문자열과 숫자형은 비교방식이 다르므로 다른 루틴을 사용하도록 함.
				//숫자 : 
				// 숫자는 MSB 비교가 우선이 되어야 함. 패턴크기는 항상 같음 (캐스팅되어 사용하기 때문에 항상 같은 바이트 수가 나옴)
				// 1. 패턴크기가 같은경우 : 앞에서부터 순차비교
				//문자 :
				// 1. 패턴크기가 같은경우 : 앞에서부터 순차비교
				// 2. 패턴크기가 다른경우 : 앞에서부터 순차비교, 짧은 패턴에 맞춤. 남는패턴이 있는쪽이 큼
				if(fieldSetting.isNumericField()) {
					
					logger.debug("value : {} / pattern1:{} / pattern2:{}",
							new Object[] {
						bytesRef.toIntValue(),
						patternBuf1.toIntValue(),
						patternBuf2.toIntValue()
					});
					
					if(
						compareNumeric(
						bytesRef,bytesRef.length(), 
						patternBuf1, patternBuf1.length()) >=0 &&
						
						compareNumeric(
						bytesRef,bytesRef.length(), 
						patternBuf2, patternBuf2.length()) <=0 
						
					) {
						return true;
					} else {
						return false;
					}
				} else {
					
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * @param lval
	 * @param lsize
	 * @param rval
	 * @param rsize
	 * @return 0:lval and rval is same / 1:lval is bigger / -1:rval is bigger
	 */
	private static int compareNumeric(BytesRef lval, int lsize, BytesRef rval, int rsize) {
		//check msb;
		int direction = 1;
		
		int lbyte = lval.get(0)&0xff;
		int rbyte = rval.get(0)&0xff;
		
		if((lbyte&0x80)==0) {
			if((rbyte&0x80)==0) {
				//lbyte positive / rbyte positive
				//do nothing
			} else {
				//lbyte positive / rbyte negative
				return 1;
			}
		} else {
			if((rbyte&0x80)==0) {
				//lbyte negative / rbyte positive
				return -1;
			} else {
				//lbyte negative / rbyte negative
				direction = -1;
			}
		}
		
		for(int inx=1;inx<lsize;inx++) {
			if((lval.get(inx)&0xff) > (rval.get(inx)&0xff)) {
				return 1 * direction;
			} else if((lval.get(inx)&0xff) < (rval.get(inx)&0xff)) {
				return -1 * direction;
			} else {
				continue;
			}
		}
		return 0;
	}
}

//
//	public SectionFilter(Filter filter, FieldSetting fieldSetting) throws FilterException {
//		this(filter, fieldSetting, false);
//	}
//	public SectionFilter(Filter filter, FieldSetting fieldSetting, boolean isBoostFunction) throws FilterException {
//		super(filter, fieldSetting, isBoostFunction);
//		if(filter.isEndPatternExist()){
//			f = Field.createSingleValueField(fieldSetting, filter.endPattern(j));
//			patternByteSize = fieldSetting.getByteSize();
//			arrayOutput = new ByteArrayOutput(patternByteSize);
//			f.writeFixedDataTo(arrayOutput);
//			endPatternList[j] = arrayOutput.bytesRef();
////				f = fieldSetting.createField(filter.endPattern(j));
////				patternByteSize = fieldSetting.getByteSize();
////				endPatternList[j] = new FastByteBuffer(patternByteSize);
////				f.getFixedBytes(endPatternList[j]);
////				endPatternList[j].flip();
//		}
//	}
//
//	@Override
//	public boolean filtering(RankInfo rankInfo, DataRef dataRef, boolean skip) throws IOException {
//		
//		if(fieldSetting.multiValue) {
//			int multiValueCount = IOUtil.readShort(buffer);
//			int endPos = buffer.pos() + fieldByteSize * (multiValueCount == 0 ? 1 : multiValueCount);
//			
//			if(skip){
//				buffer.pos(endPos);
//				return isBoostFunction;
//			}
//			
//			for (int m = 0; m < multiValueCount; m++) {
//				int pos = buffer.pos();
//				//if( doSingleSectionFilter(bbuf,2 + m * fieldByteSize) ) {
//				if( doSingleSectionFilter(buffer.array, pos) ) {
//					//buffer를 다 읽지 않았으므로 position을 변경해준다.
//					buffer.pos(endPos);
//					if(isBoostFunction) {
//						rankInfo.addScore(boostScore);
//					}
//					return true;
//				}
//				buffer.skip(fieldByteSize);
//			}
//			buffer.pos(endPos);
//			return isBoostFunction;
//		} else {
//			int endPos = buffer.pos() + fieldByteSize;
//			int pos = buffer.pos();
//
//			if(skip){
//				buffer.pos(endPos);
//				return isBoostFunction;
//			}
//			
//			if( doSingleSectionFilter(buffer.array, pos) ) {
//				//buffer를 다 읽지 않았으므로 position을 변경해준다.
//				buffer.pos(endPos);
//				if(isBoostFunction) {
//					rankInfo.addScore(boostScore);
//				}
//				return true;
//			}
//			//다음 데이터를 읽을수 있도록 버퍼위치를 변경해준다.
//			buffer.pos(endPos);
//			return isBoostFunction;
//		}
//	}
//	
//	
//	private boolean doSingleSectionFilter(byte[] bbuf, int pos) {
//		//data stored in MSB method
//		//logic case if data type is (1)string or (2)numeric positive or (3)numeric negative
//		//(1) string : compare sequentially from first byte to end byte
//		//(2) numeric positive :
//		//(3) numeric negative : 
//		int s1, s2, s3, v1, v2;
//		//logger.debug("fieldSetting.isNumeric : "+fieldSetting.isNumeric());
//		/*
//		 * 
//		 * 
//		 * FIXME 숫자형 필드의 경우 재확인필요. 2012-10-22
//		 * 문자형 필드는 버그수정을 했으나 숫자형은 아직 확인 안함.
//		 * 
//		 * */
//		if(fieldSetting.isNumeric()) { // numeric compare
//			if(pos >= bbuf.length) { return false; }
//			s1 = bbuf[pos] & 0x80;
//			for (int patternInx = 0; patternInx < patternCount; patternInx++) PATTERN_LOOP: { //pattern loop head.
//				
//				byte[] startPattern = patternList[patternInx].array;
//				s2 = startPattern[0] & 0x80;
//				byte[] endPattern = null;
//				
//				if(endPatternList[patternInx] !=null) {
//					endPattern = endPatternList[patternInx].array;
//					s3 = endPattern[0] & 0x80;
//				} else {
//					s3 = 0x0;
//				}
//				//logger.debug("section filter sign bit : "+s1+":"+s2+":"+s3+"");
//				if(s2 >= s1 && s1 >= s3) { //negative = 1, positive = 0;
//					if(s2 == s1 && s1 == s3) { //compare all
//						for(int bufInx = 0; bufInx < fieldByteSize; bufInx++) {
//							v1 = (int)bbuf[pos+bufInx] &0xff;
//							v2 = (int)startPattern[bufInx] &0xff;
//							if(v1 < v2) {
//								//logger.debug("value is not greater than startpattern");
//								break PATTERN_LOOP;
//							} else if( v1 > v2){
//								//logger.debug("value is greater than startpattern");
//								break;
//							}
//						}
//						
//						if(endPattern == null) { return true; }
//						
//						for(int bufInx = 0; bufInx < fieldByteSize; bufInx++) {
//							v1 = (int)bbuf[pos+bufInx] &0xff;
//							v2 = (int)endPattern[bufInx] &0xff;
//							if(v1 > v2) {
//								//logger.debug("value is not lesser than endpattern");
//								break PATTERN_LOOP;
//							} else if( v1 < v2 ){
//								//logger.debug("value is lesser than endpattern");
//								return true;
//							}
//						}
//						
//						return true;
//						
//					} else if(s1 == 0 && s3 == 0) { //compare  bbuf & endPattern
//						
//						if(endPattern == null) { 
//							return true; 
//						}
//						
//						for(int bufInx = 0; bufInx < fieldByteSize; bufInx++) {
//							v1 = (int)bbuf[pos+bufInx] &0xff;
//							v2 = (int)endPattern[bufInx] &0xff;
//							if(v1 > v2) {
//								break PATTERN_LOOP;
//							} else if( v1 < v2 ) {
//								return true;
//							}
//						}
//						
//					} else if(s2 != 0 && s1 != 0) { //compare startPattern & bbuf
//						for(int bufInx = 0; bufInx < fieldByteSize; bufInx++) {
//							v1 = (int)bbuf[pos+bufInx] &0xff;
//							v2 = (int)endPattern[bufInx] &0xff;
//							if(v1 < v2) {
//								break PATTERN_LOOP;
//							} else if(v1 > v2) {
//								return true;
//							}
//						}
//					}
//					//} else { //sign bit unmatched or out of scope
//				}
//			}
//		} else { //string compare
//			
//			//여러패턴입력시 관계는 OR이므로, 패턴하나만 일치해도 성공이다. 
//			//FIXME uchar필드(특히 한글)의 경우 byte비교가 작동하리라는 보장이없다.
//			//수정필요.
//			for (int patternInx = 0; patternInx < patternCount; patternInx++) {
//				
//				byte[] startPattern = patternList[patternInx].array;
//				byte[] endPattern = null;
//				if(endPatternList[patternInx] !=null) {
//					endPattern = endPatternList[patternInx].array;
//				}
////				logger.debug("Pat>> {} ~ {}", patternList[patternInx].toAlphaString(), endPatternList[patternInx]!= null?endPatternList[patternInx].toAlphaString():"NULL");
//				
//				int clen = patternList[patternInx].limit;
//				int len = 0;
//				boolean isOK = false;
//				
////				logger.debug("clen ={}, fieldByteSize={}", clen, fieldByteSize);
//				
//				if(clen > 0) {
//					//데이터와 패턴의 최소공통길이를 찾아낸다.
//					len = (clen <= fieldByteSize)? clen : fieldByteSize;
//					
////					logger.debug("startPattern.length ={}, bbuf.length={}", startPattern.length, bbuf.length);
//					for(int bufInx = 0; bufInx < len; bufInx++) {
//						
//						//입력패턴이 bytesize보다 작게 들어올수 있다.
//						//이런경우 루프를 종료시켜주지 않으면 ArrayIndexOutOfBoundException이 발생한다.
//						//array bound 체크.
//						if(bufInx >= startPattern.length || (pos+bufInx) >= bbuf.length) {
//							//(pos+bufInx) >= bbuf.length 는 발생할수 없지만 double check 해준다.
//							break;
//						}
//						v1 = (int)bbuf[pos+bufInx] &0xff;
//						v2 = (int)startPattern[bufInx] &0xff;
////						logger.debug("v1:v2 = {}:{}", (char)v1, (char)v2);
//						//예) 2009 : 2012 의 비교시.
//						if(v1 < v2) {
//							//데이터값이 시작패턴보다 작아지면 out!
//							isOK = false;
//							break;
//						} else if(v1 > v2) {
//							isOK = true;
//							//최소공통길이의 값이 모두 v1 >= v2 임이 확인되어야 하므로, break하지 않고 다음 char비교로 계속진행한다.
//							break;
//						} else {
//							isOK = true;
//						}
//					}
//					
//					//시작패턴에서 통과못하면 바로 다음 패턴으로 넘어간다.
//					if(!isOK) {
//						continue;
////						if(clen > fieldByteSize) {
////							continue;
////						}
//					}
//				}
//				
//				if(endPatternList[patternInx] == null) { 
//					return true;
//				}
//				
//				clen = endPatternList[patternInx].limit;
////				logger.debug("check endPattern = {},clen={}", endPatternList[patternInx].toAlphaString(), clen);
//				
//				if(clen > 0) {
//					isOK = true;
//					
//					len = (clen <= fieldByteSize)? clen : fieldByteSize;
//					
//					for(int bufInx = 0; bufInx < len; bufInx++) {
//						
//						if(bufInx >= endPattern.length || (pos+bufInx) >= bbuf.length) {
//							break;
//						}
//						v1 = (int)bbuf[pos+bufInx] &0xff;
//						v2 = (int)endPattern[bufInx] &0xff;
////						logger.debug("v1:v2 = {}:{}", (char)v1, (char)v2);
//						//예) 2009 : 2012 의 비교시.
//						if(v1 > v2) {
//							isOK = false;
//							break;
//						}else if(v1 < v2) {
//							isOK = true;
//							break;
//						} else {
//							isOK = true;
//						}
//						//v1 == v2의 경우는 다음글자를 더 확인해야 한다.
//					}
//					
//					if(isOK) {
//						//종료패턴길이가 데이터길이와 같거나 더 크면 허용한다.
//						if(clen >= fieldByteSize) {
//							return true;
//						}else {
//							//패턴길이가 작아도 나머지 데이터가 0 이면 비교가 필요없으므로 허용되지만,
//							// 0이 아닌 값이 들어있으면, 종료패턴보다 데이터가 큰 것이므로 허용되지 않는다.
//							for(int bufInx = clen; bufInx < fieldByteSize; bufInx++) {
//								v1 = (int)bbuf[pos+bufInx] & 0xff;
//								if(v1 != 0){
////									logger.debug("0이 아닌 필터데이터가 있어서 범위에 부합하지 않음. v1 = {}, clen={}, fieldByteSize={}", new Object[]{v1, clen, fieldByteSize});
//									isOK = false;
//									break;
//								}
//							}
//							
//							//최종적으로 OK일때에만 리턴한다.
//							if(isOK) {
//								return true;
//							}
//						}
//						
//					}
//				}
//			}
//		}
//		return false;
//	}
