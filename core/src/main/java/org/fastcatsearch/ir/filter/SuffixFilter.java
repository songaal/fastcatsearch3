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
//
///**
// * @author swsong
// *
// */
//public class SuffixFilter extends FilterFunction {
//
//	public SuffixFilter(Filter filter, FieldSetting fieldSetting) throws FilterException {
//		this(filter, fieldSetting, false);
//	}
//	public SuffixFilter(Filter filter, FieldSetting fieldSetting, boolean isBoostFunction) throws FilterException {
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
//				int pos = buffer.pos() + fieldByteSize - 1;
//				for (int j = 0; j < patternCount; j++) {
//					FastByteBuffer buf = patternList[j];
//					boolean isMatch = true;
//					//패턴의 길이만큼만 확인한다.
//					//TODO buffer의 0이아닌 유효한 데이터 위치를 확인한다.
//					
//					//뒤에서 부터 매칭테스트.
//					for (int k = 0; k < buf.limit; k++) {
////					for (int k = 0; k < fieldByteSize; k++) {
//						if(buf.array[buf.limit - 1 - k] != buffer.array[pos - k]){
//							isMatch = false;
//							break;
//						}
//					}
//					
//					if(isMatch){
//						//일치하는것이 하나라도 있으면 미리 끝낸다
//						//buffer를 다 읽지 않았으므로 position을 변경해준다.
//						buffer.pos(endPos);
//						if(isBoostFunction){
//							rankInfo.addScore(boostScore);
//						}
//						
//						return true;
//					}
//				}//for
//				
//				//다음 value
//				buffer.skip(fieldByteSize);
//			}
//			buffer.pos(endPos);
//			
//			return isBoostFunction;
//					
//		}else{
//			int endPos = buffer.pos() + fieldByteSize;
//			int pos = buffer.pos() + fieldByteSize - 1;
//			
//			if(skip){
//				buffer.pos(endPos);
//				return isBoostFunction;
//			}
//			
//			for (int i = 0; i < fieldByteSize; i++) {
//				if(buffer.array[pos] != 0)
//					break;
//				pos--;
//			}
//			for (int j = 0; j < patternCount; j++) {
//				FastByteBuffer buf = patternList[j];
//				boolean isMatch = true;
//				//패턴의 길이만큼만 확인한다.
//				if(pos >= buffer.pos() && buf.limit > 0){
//					for (int k = 0; k < buf.limit && pos >= buffer.pos(); k++) {
//						if(buf.array[buf.limit - 1 - k] != buffer.array[pos - k]){
//							isMatch = false;
//							break;
//						}
//					}
//				}else{
//					isMatch = false;
//				}
//				
//				if(isMatch){
//					//일치하는것이 하나라도 있으면 미리 끝낸다
//					//buffer를 다 읽지 않았으므로 position을 변경해준다.
//					buffer.pos(endPos);
//					if(isBoostFunction){
//						rankInfo.addScore(boostScore);
//					}
//					return true;
//				}
//			}//for
//			
//			//다음 데이터를 읽을수 있도록 버퍼위치를 변경해준다.
//			buffer.pos(endPos);
//			
//			return isBoostFunction;
//		}
//	}
//}
