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
//import java.io.IOException;
//
//import org.fastcatsearch.ir.common.IRException;
//import org.fastcatsearch.ir.config.IRConfig;
//import org.fastcatsearch.ir.config.IRSettings;
//import org.fastcatsearch.ir.io.BufferedFileInput;
//import org.fastcatsearch.ir.io.Input;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//
//public class SpecialCharacterMap {
//	private static Logger logger = LoggerFactory.getLogger(SpecialCharacterMap.class);
//	
//	protected static char[] map = new char[0x10000];
//	private static File mapPath;
//	private static SpecialCharacterMap id;
//	
//	static{
//		IRConfig irconfig = IRSettings.getConfig();
//		if(irconfig != null){
//			mapPath = new File(IRSettings.path(irconfig.getString("specialCharacter.map.path")));
//		}else{
//			logger.error("Cannot find IR config file. ");
//		}
//	}
//	public char[] map(){
//		return map;
//	}
//	
//	public synchronized static SpecialCharacterMap getMap(){
//		if(id == null){
//			id = new SpecialCharacterMap(mapPath);
//		}
//		
//		return id;
//	}
//
//	public synchronized static SpecialCharacterMap getMap(File file) throws IRException{
//		mapPath = file;
//		
//		if(id == null){
//			id = new SpecialCharacterMap(mapPath);
//		}
//		
//		return id;
//	}
//	
//	protected SpecialCharacterMap(File file){
//		try{
//			Input input = new BufferedFileInput(file);
//			for (int i = 0; i < map.length; i++) {
//				map[i] = input.readUChar();
//			}
//			input.close();
//		}catch(IOException e){
//			throw new RuntimeException("Error loading special character map." + e);
//		}
//		
//	}
//	public char getNormarlizedChar(char c){
//		return map[c];
//	}
//	
//	public char[] getNormarlizedChars(char[] chars){
//		for (int i = 0; i < chars.length; i++) {
//			chars[i] = map[chars[i]];
//		}
//		return chars;
//	}
//	
//	public char[] getNormarlizedString(String str){	
//		return getNormarlizedChars(str.toCharArray());
//	}
//	
//}
