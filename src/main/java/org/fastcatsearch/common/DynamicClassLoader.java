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

package org.fastcatsearch.common;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicClassLoader {
	private static Logger logger = LoggerFactory.getLogger(DynamicClassLoader.class);
	
	private static Map<String, URLClassLoader> classLoaderList = new HashMap<String, URLClassLoader>();
	
	public static boolean remove(String tag) {
		synchronized(classLoaderList){
			classLoaderList.remove(tag);
		}
		return true;
	}
	public static boolean removeAll() {
		synchronized(classLoaderList){
			classLoaderList.clear();
		}
		return true;
	}
	
	public static boolean add(String tag, File[] jarFiles) {
		URL[] jarUrls = new URL[jarFiles.length];
		StringBuilder sb = new StringBuilder(); 
		for (int i = 0; i < jarFiles.length; i++) {
			try {
				jarUrls[i] = jarFiles[i].toURI().toURL();
				sb.append(jarUrls[i]).append(", ");
			} catch (MalformedURLException e) {
			}
		}
		
		URLClassLoader l = new URLClassLoader(jarUrls, Thread.currentThread().getContextClassLoader());
		synchronized(classLoaderList){
			logger.debug("Add Classpath {}:{}", tag, sb.toString());
			classLoaderList.put(tag, l);
		}
		return true;
	}
	
	public static boolean add(String tag, String[] jarFilePath) {
		URL[] jarUrls = new URL[jarFilePath.length];
		for (int i = 0; i < jarFilePath.length; i++) {
			File f = new File(jarFilePath[i]);
			try {
				jarUrls[i] = f.toURI().toURL();
			} catch (MalformedURLException e) {
			}
		}
		
		URLClassLoader l = new URLClassLoader(jarUrls);
		synchronized(classLoaderList){
			classLoaderList.put(tag, l);
		}
		return true;
	}
	public static Object loadObject(String className){
		try {
			Class<?> clazz = loadClass(className);
			if(clazz != null){
				return clazz.newInstance();
			}
		} catch (Exception ignore){
			ignore.printStackTrace();
		}
		
		return null;
	}
	public static <T> T loadObject(String className, Class<T> type){
		try {
			Class<?> clazz = loadClass(className);
			if(clazz != null){
				return (T) clazz.newInstance();
			}
		} catch (Exception ignore){
			ignore.printStackTrace();
		}
		
		return null;
	}
	
	public static <T> T loadObject(String className, Class<T> type, Class<?>[] paramTypes ,Object[] initargs) {
		try {
			Class<?> clazz = loadClass(className);
			if(clazz != null){
				Constructor<?> constructor = clazz.getConstructor(paramTypes);
				return (T) constructor.newInstance(initargs);
			}
		} catch (Exception ignore){
			ignore.printStackTrace();
		}
		return null;
	}
	
	public static Class<?> loadClass(String className) {
		
		Class<?> clazz = null;
		try {
			clazz = Class.forName(className);
			if(clazz != null){
				return clazz;
			}
		}catch(ClassNotFoundException ignore){
			logger.warn("Default classloader cannot find {}", className);
		}
		
		synchronized(classLoaderList){
			Iterator<URLClassLoader> iter = classLoaderList.values().iterator();
			while(iter.hasNext()){
				URLClassLoader l = (URLClassLoader)iter.next();
				try {
					clazz = Class.forName(className, true, l);
				} catch (ClassNotFoundException e) {
					continue;
				}
				
				if(clazz != null){
					return clazz;
				}
			}
		}
		return null;
	}
	
}
