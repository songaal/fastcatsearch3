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

package org.fastcatsearch.ir.group;

import junit.framework.TestCase;

public class GroupFuctionTest extends TestCase {
	public void testCapitalize(){
		assertEquals("Apple", capitalize("apple"));
		assertEquals("1apple", capitalize("1apple"));
		assertEquals("_pple", capitalize("_pple"));
		assertEquals("Apple", capitalize("Apple"));
		assertEquals("한글", capitalize("한글"));
		assertEquals("@pple", capitalize("@pple"));
	}
	
	private String capitalize(String str){
		char firstChar = str.charAt(0);
		
		if(firstChar >= 'a' && firstChar <= 'z'){
			return ((char) (firstChar - 32)) + str.substring(1);
		}else{
			return str;
		}
		
	}
	
	public void testFunctionClassName(){
		assertEquals("com.websqrd.plugin.group.Apple", convertToClassName("apple_freq"));
		assertEquals("com.websqrd.plugin.group.apple.Mac", convertToClassName("apple.mac_freq"));
		assertEquals("com.websqrd.plugin.group.apple.DesignMac", convertToClassName("apple.design_mac_freq"));
		assertEquals("com.websqrd.plugin.group.apple.jobs.DesignMac", convertToClassName("apple.jobs.design_mac_freq"));
		assertEquals("com.websqrd.plugin.group.apple.jobs.DesignMacMacbookPro", convertToClassName("apple.jobs.design_mac_macbook_pro_freq"));
	}
	
	private String convertToClassName(String groupFunctionName){
		String PLUGIN_PACKAGE_PATH = "com.websqrd.plugin.group";
		
		int strLen = groupFunctionName.length();
		//_freq 제거
		String temp = groupFunctionName.substring(0, strLen - 5);
		String tempClassName = null;
		String packageName = null;
		String className = "";
		
		if(temp.contains(".")){
			int idx = temp.lastIndexOf(".");
			packageName = temp.substring(0, idx);
			tempClassName = temp.substring(idx + 1);
		}else{
			tempClassName = temp;
		}
		
		if(tempClassName.contains("_")){
			String[] parts2 = tempClassName.split("_");
			for (int i = 0; i < parts2.length; i++) {
				className += capitalize(parts2[i]);
			}
		}else{
			className = capitalize(tempClassName);
		}
		
		if(packageName != null){
			return PLUGIN_PACKAGE_PATH + "." + packageName + "." + className;
		}else{
			return PLUGIN_PACKAGE_PATH + "." + className;
		}
		
	}
	
}
