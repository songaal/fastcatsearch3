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
//package org.fastcatsearch.ir.config;
//
//import java.util.Properties;
//
//public class IRConfig {
//	private Properties props;
//	private static final int K = 1024;
//	private static final int M = K * K;
//	private static final int G = K * K * K;
//	
//	public IRConfig(Properties props){
//		this.props = props;
//	}
//	public int getInt(String key){
//		return getInt(key, -1);
//	}
//	public int getInt(String key, int defaultValue){
//		String str = props.getProperty(key);
//		if(str == null)
//			return defaultValue;
//			
//		str = str.trim();
//		int len = str.length();
//		if(len > 0){
//			char suffix = str.charAt(len - 1);
//			if(suffix == 'g' || suffix == 'G'){
//				return Integer.parseInt(str.substring(0, len - 1).trim()) * G;
//			}else if(suffix == 'm' || suffix == 'M'){
//				return Integer.parseInt(str.substring(0, len - 1).trim()) * M;
//			}else if(suffix == 'k' || suffix == 'K'){
//				return Integer.parseInt(str.substring(0, len - 1).trim()) * K;
//			}else if(suffix == 'b' || suffix == 'B'){
//				return Integer.parseInt(str.substring(0, len - 1).trim());
//			}else{
//				return Integer.parseInt(str);
//			}
//		}else{
//			return -1;
//		}
//	}
//	
//	public int getByteSize(String key){
//		return getByteSize(key, 0);
//	}
//	public int getByteSize(String key, int defaultValue){
//		String str = props.getProperty(key);
//		if(str == null)
//			return defaultValue;
//			
//		str = str.trim();
//		int len = str.length();
//		if(len > 0){
//			char suffix = str.charAt(len - 1);
//			if(suffix == 'g' || suffix == 'G'){
//				return Integer.parseInt(str.substring(0, len - 1).trim()) * G;
//			}else if(suffix == 'm' || suffix == 'M'){
//				return Integer.parseInt(str.substring(0, len - 1).trim()) * M;
//			}else if(suffix == 'k' || suffix == 'K'){
//				return Integer.parseInt(str.substring(0, len - 1).trim()) * K;
//			}else if(suffix == 'b' || suffix == 'B'){
//				return Integer.parseInt(str.substring(0, len - 1).trim());
//			}else{
//				return Integer.parseInt(str);
//			}
//		}else{
//			return defaultValue;
//		}
//	}
//	
//	public String getString(String key){
//		String val = props.getProperty(key);
//		if(val != null)
//			return val.trim();
//		else
//			return "";
//	}
//	
//	public boolean getBoolean(String key){
//		return getBoolean(key, false);
//	}
//	public boolean getBoolean(String key, boolean defaultValue){
//		String val = props.getProperty(key);
//		if(val != null)
//			return Boolean.parseBoolean(val);
//		else
//			return defaultValue;
//	}
//	
//	public Properties getProperties(){
//		return props;
//	}
//}
