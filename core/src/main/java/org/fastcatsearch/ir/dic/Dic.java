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
//package org.fastcatsearch.ir.dic;
//
//import java.io.File;
//
//import org.fastcatsearch.ir.common.IRException;
//import org.fastcatsearch.ir.config.IRConfig;
//import org.fastcatsearch.ir.config.IRSettings;
//import org.fastcatsearch.ir.io.CharVector;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//
//public class Dic {
//	
//	private static Logger logger = LoggerFactory.getLogger(Dic.class);
//
//	public static HashMapDictionary synonym;
//	public static HashSetDictionary stopword;
//	public static HashSetDictionary korean;
//	public static HashSetDictionary userword;
//	public static HashSetDictionary apStop;
//	public static HashSetDictionary stop;
//	
//	public static void init() throws IRException{
//		
//		IRConfig irconfig = IRSettings.getConfig();
//		if(irconfig != null){
//			File synonymFile = null;
//			File stopwordFile = null;
//			File koreanFile = null;
//			File userFile = null;
//			
//			if(irconfig.getString("synonym.dic.path") != null)
//				synonymFile = new File(IRSettings.path(irconfig.getString("synonym.dic.path")));
//			if(irconfig.getString("stopword.dic.path") != null)
//				stopwordFile = new File(IRSettings.path(irconfig.getString("stopword.dic.path")));
//			if(irconfig.getString("korean.dic.path") != null)
//				koreanFile = new File(IRSettings.path(irconfig.getString("korean.dic.path")));
//			if(irconfig.getString("user.dic.path") != null)
//				userFile = new File(IRSettings.path(irconfig.getString("user.dic.path")));
//			
//			logger.info("Init dictionary.");
//			if(synonymFile != null && synonymFile.exists()){
//				synonym = new HashMapDictionary(synonymFile);
//			}else{
//				synonym = new HashMapDictionary(16);
//				logger.info("Synonym Dictionary not exist!!");
//			}
//			if(stopwordFile != null && stopwordFile.exists()){
//				stopword = new HashSetDictionary(stopwordFile);
//			}else{
//				stopword = new HashSetDictionary(16);
//				logger.info("Stopword Dictionary not exist!!");
//			}
//			if(koreanFile != null && koreanFile.exists()){
//				korean = new HashSetDictionary(koreanFile);
//			}else{
//				korean = new HashSetDictionary(16);
//				logger.info("Korean Dictionary not exist!!");
//			}
//			if(userFile != null && userFile.exists()){
//				userword = new HashSetDictionary(userFile);
//			}else{
//				userword = new HashSetDictionary(16);
//				logger.info("User Dictionary not exist!!");
//			}
////			if(preFile != null && preFile.exists()){
////				predic = new HashMapDictionary(preFile);
////			}else{
////				predic = new HashMapDictionary(16);
////				logger.info("Pre Dictionary not exist!!");
////			}
//		}
//		makeApdbStopDic();
//		makeStopDic();
//		
//	}
//	
//	public static void makeApdbStopDic(){
//		String[] appendableStop = {"가","같이","거나","게","게서","고","과","구","그래","그랴","그려","까지","깨나","꺼정"
//				,"께","께서","께옵서","꼬","ㄴ들","ㄴ커녕","나","나마","는","는커녕","니","다","다가","대로","더러"
//				,"덜","도","두","든","든가","든지","들","따라","ㄹ","ㄹ랑","ㄹ랑은","라","라고","라도","라든가"
//				,"라든지","라야","라야만","란","랑","래","로","로부터","로서","로써","루","를","마냥","마는","마다"
//				,"마동","마따나","마저","만","만큼","맨치로","며","밖에","보고","보구","보다","보담","부덤","부터"
//				,"부텀","뿐","사","서","서껀","서부터","손","시여","아","야","야말로","얼","었","에","에게","에게로"
//				,"에게서","에다","에다가","에로","에서","에서부터","에야","에의","엔","엔들","여","와","요","우","유"
//				,"으로","으로부터","으로서","으로써","으루","은","은커녕","을","의","이","이고","이구","이나","이나마"
//				,"이니","이다","이든","이든지","이라","이라고","이라도","이라두","이라든가","이라든지","이라야","이란"
//				,"이랑","이래","이며","이사","이시여","이야","이야말로","이여","인들","인즉","인즉슨","일랑","입쇼","입죠"
//				,"잉","조차","처럼","치고","치고는","커녕","하고","하구","하며","한테","한테로","한테서","한티","허고"
//				,"헌테","헌티"
//				,"입니다","립니다","습니다","니다","있","있다","수","에까지","인","한","것","면서","지만","에서도","등","으니"
//				,"라는","면","적","이기","이라기","혔다","스럽게","스럽","(",")","[","]","{","}"};
//		Dic.apStop = new HashSetDictionary(256);
//		for (int i = 0; i < appendableStop.length; i++) {
//			apStop.put(new CharVector(appendableStop[i]));
//			
//		}
//	}
//	public static void makeStopDic(){
//		String[] stopList = {"있다","있으니","있","했","없","수","할","갈","하","및","(","[","\"","\'","#","“","‘","’"
//				,"것","보다는","됐","되","즉","한","이라는","된","겼","해","을","를"};
//		Dic.stop = new HashSetDictionary(256);
//		for (int i = 0; i < stopList.length; i++) {
//			stop.put(new CharVector(stopList[i]));
//			
//		}
//	}
//	public static boolean reload(String dicName) throws IRException{
//		IRConfig irconfig = IRSettings.getConfig();
//		if(irconfig != null){
//			
//			if(dicName.equalsIgnoreCase("synonym")){
//				logger.info("Load synonym dictionary...");
//				File synonymFile = new File(IRSettings.path(irconfig.getString("synonym.dic.path")));
//				if(synonymFile.exists()){
//					Dic.synonym = new HashMapDictionary(synonymFile);
//				}else{
//					throw new IRException("Cannot find dictionary file = "+synonymFile.getAbsolutePath());
//				}
//				logger.info("Done.");
//			}else if(dicName.equalsIgnoreCase("stopword")){
//				logger.info("Load stopword dictionary...");
//				File stopwordFile = new File(IRSettings.path(irconfig.getString("stopword.dic.path")));
//				if(stopwordFile.exists()){
//					Dic.stopword = new HashSetDictionary(stopwordFile);
//				}else{
//					throw new IRException("Cannot find dictionary file = "+stopwordFile.getAbsolutePath());
//				}
//				logger.info("Done.");
//			}else if(dicName.equalsIgnoreCase("korean")){
//				logger.info("Load korean dictionary...");
//				File koreanFile = new File(IRSettings.path(irconfig.getString("korean.dic.path")));
//				if(koreanFile.exists()){
//					Dic.korean = new HashSetDictionary(koreanFile);
//				}else{
//					throw new IRException("Cannot find dictionary file = "+koreanFile.getAbsolutePath());
//				}
//				logger.info("Done.");
//			}else if(dicName.equalsIgnoreCase("userword")){
//				logger.info("Load User dictionary...");
//				File userwordFile = new File(IRSettings.path(irconfig.getString("user.dic.path")));
//				if(userwordFile.exists()){
//					Dic.userword = new HashSetDictionary(userwordFile);
//				}else{
//					throw new IRException("Cannot find dictionary file = "+userwordFile.getAbsolutePath());
//				}
//				logger.info("Done.");
//			}
////			else if(dicName.equalsIgnoreCase("predic")){
////				logger.info("Load Pre dictionary...");
////				File predicFile = new File(IRSettings.path(irconfig.getString("pre.dic.path")));
////				if(predicFile.exists()){
////					Dic.predic = new HashMapDictionary(predicFile);
////				}else{
////					throw new IRException("Cannot find dictionary file = "+predicFile.getAbsolutePath());
////				}
////				logger.info("Done.");
////			}
//			else{
//				logger.error("Unknown dictionary = "+dicName);
//				return false;
//			}
//			
//			return true;
//			
//		}
//		logger.error("Cannot load ir config file");
//		return false;
//	}
//	
//	//for test
//	public static void set(String dicName, File file) throws IRException{
//			
//		if(dicName.equalsIgnoreCase("synonym")){
//			Dic.synonym = new HashMapDictionary(file);
//		}else if(dicName.equalsIgnoreCase("stopword")){
//			Dic.stopword = new HashSetDictionary(file);
//		}else if(dicName.equalsIgnoreCase("korean")){
//			Dic.korean = new HashSetDictionary(file);
//		}else if(dicName.equalsIgnoreCase("userword")){
//			Dic.userword = new HashSetDictionary(file);
//		}
////		else if(dicName.equalsIgnoreCase("predic")){
////			Dic.predic = new HashMapDictionary(file);
////		}
//		else{
//			logger.error("Unknown dictionary = "+dicName);
//		}
//	}
//	//for test
//	public static void set(String dicName, Object dic) throws IRException{
//			
//		if(dicName.equalsIgnoreCase("synonym")){
//			Dic.synonym = (HashMapDictionary)dic;
//		}else if(dicName.equalsIgnoreCase("stopword")){
//			Dic.stopword = (HashSetDictionary)dic;
//		}else if(dicName.equalsIgnoreCase("korean")){
//			Dic.korean = (HashSetDictionary)dic;
//		}else if(dicName.equalsIgnoreCase("userword")){
//			Dic.userword = (HashSetDictionary)dic;
//		}
////		else if(dicName.equalsIgnoreCase("predic")){
////			Dic.predic = (HashMapDictionary)dic;
////		}
//		else{
//			logger.error("Unknown dictionary = "+dicName);
//		}
//	}
//}
