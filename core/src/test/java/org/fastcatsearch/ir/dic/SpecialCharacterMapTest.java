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
//import org.fastcatsearch.ir.config.IRSettings;
//import org.fastcatsearch.ir.dic.SpecialCharacterMap;
//
//import junit.framework.TestCase;
//
//
//public class SpecialCharacterMapTest extends TestCase {
//	
//	public void testPrint(){
//		IRSettings.setHome(".");
//		SpecialCharacterMap specialCharacterMap = SpecialCharacterMap.getMap();
//		char[] map = specialCharacterMap.map;
//		for (int i = 0; i < map.length; i++) {
//			if(i % 16 == 0) System.out.println();
//			
//			System.out.print(map[i]+",");
//			
//			if(i > 128)
//				break;
//		}
//	}
//	public void test1(){
//		IRSettings.setHome(".");
//		SpecialCharacterMap specialCharacterMap = SpecialCharacterMap.getMap();
//		System.out.println("specialCharacterMap = "+specialCharacterMap);
//		String str = "【廓】13)  보건복지부, 󰡒노인보건복지시설현황󰡓(2002)  14) 1994통계청, \"세계인구전망1995\" ";
//		char[] chars = specialCharacterMap.getNormarlizedString(str);
//		System.out.println(str);
//		System.out.println(new String(chars));
//		
//	}
//}
