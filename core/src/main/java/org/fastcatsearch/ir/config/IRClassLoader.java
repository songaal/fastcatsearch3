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
//import java.io.File;
//import java.lang.reflect.Constructor;
//import java.lang.reflect.InvocationTargetException;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.URLClassLoader;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class IRClassLoader {
//	private static Logger logger = LoggerFactory.getLogger(IRClassLoader.class);
//	private Map<String, URLClassLoader> classLoaderList = new HashMap<String, URLClassLoader>();
//	private Map<String, Class<?>> classCache = new HashMap<String, Class<?>>();
//	public static IRClassLoader instance;
//	protected boolean isRunning;
//	
//	public static IRClassLoader getInstance(){
//		return instance;
//	}
//	
//	public IRClassLoader(){ 
//		instance = this;
//	}
//	
//	public boolean isRunning(){
//		return isRunning;
//	}
//	public boolean start() {
//		IRConfig config = IRSettings.getConfig(true);
//		String jarPath = config.getString("dynamic.classpath");
//		if(jarPath != null){
//			jarPath = jarPath.trim();
//		}
//		
//		//초기화
//		classLoaderList.clear();
//		classCache.clear();
//		
//		if(jarPath != null && jarPath.length() > 0){
//			// ',' makes different classloader
//			String[] pathList = jarPath.split(",");
//			for (int i = 0; i < pathList.length; i++) {
//				String tmp = pathList[i];
//				String[] tl = tmp.split("[:;]");
//				for (int k = 0; k < tl.length; k++) {
//					tl[k] = tl[k].trim();
//					if(tl[k].length() == 0)
//						continue;
//					
//					if(IRSettings.OS_NAME.startsWith("Windows")){
//						if(!tl[k].matches("^[a-zA-Z]:\\\\.*")){
//							tl[k] = IRSettings.HOME + tl[k];
//						}
//					}else{
//						if(!tl[k].startsWith(IRSettings.FILE_SEPARATOR)){
//							tl[k] = IRSettings.HOME + tl[k];
//						}
//					}
//				}
//				addClassLoader(tmp, tl);
//			}
//			
//		}
//		isRunning = true;
//		logger.info("IRClassLoader started!");
//		return true;
//	}
//	
//	public boolean restart() {
//		shutdown();
//		start();
//		return true;
//	}
//
//	public boolean shutdown() {
//		classLoaderList.clear();
//		classCache.clear();
//		isRunning = false;
//		logger.info("shutdown IRClassLoader");
//		return true;
//	}
//	
//	public boolean addClassLoader(String tag, String[] jarFilePath) {
//		URL[] jarUrls = new URL[jarFilePath.length];
//		for (int i = 0; i < jarFilePath.length; i++) {
//			File f = new File(jarFilePath[i]);
//			try {
//				jarUrls[i] = f.toURI().toURL();
//				logger.debug("Add jar = {}", jarUrls[i]);
//			} catch (MalformedURLException e) {
//				logger.error("Dynamic jar filepath is strange. path="+jarFilePath[i]+"("+f.getAbsolutePath()+")",e);
//			}
//		}
//		
//		URLClassLoader l = new URLClassLoader(jarUrls);
//		classLoaderList.put(tag, l);
//		logger.info("Class Loader {} = {}", tag, l);
//		return true;
//	}
//	
//	public Object loadObject(String className){
//		//1. cache?
//		Class<?> clazz = classCache.get(className);
//		if(clazz != null){
//			try {
//				return clazz.newInstance();
//			} catch (InstantiationException e) {
//				logger.warn("",e);
//			} catch (IllegalAccessException e) {
//				logger.warn("",e);
//			} catch (Exception e){
//				logger.error("",e);
//			}
//			return null;
//		}
//		//2. default CL?
//		try{
//			clazz = Class.forName(className);
//			if(clazz != null){
//				try {
//					return clazz.newInstance();
//				} catch (InstantiationException e) {
//					logger.warn("",e);
//				} catch (IllegalAccessException e) {
//					logger.warn("",e);
//				} catch (Exception e){
//					logger.error("",e);
//				}
//				return null;
//			}
//		}catch(ClassNotFoundException e){
//			logger.debug("Not found default class {}", className);
//		}
//		
//		//3. custom CL?
//		Iterator<URLClassLoader> iter = classLoaderList.values().iterator();
//		while(iter.hasNext()){
//			URLClassLoader l = (URLClassLoader)iter.next();
//			try {
//				clazz = Class.forName(className, true, l);
//			} catch (ClassNotFoundException e) {
//				
//				continue;
//			}
//			
//			if(clazz != null){
//				try {
//					logger.info("Found dynamic class {}", className);
//					return clazz.newInstance();
//				} catch (InstantiationException e) {
//					logger.warn("",e);
//				} catch (IllegalAccessException e) {
//					logger.warn("",e);
//				} catch (Exception e){
//					logger.error("",e);
//				}
//				return null;
//			}else{
//				logger.error("Not found dynamic class {}", className);
//			}
//		}
//		return null;
//	}
//	
//	public Object loadObject(String className, Class<?>[] paramTypes ,Object[] initargs){
//		//1. cache?
//		Class<?> clazz = classCache.get(className);
//		if(clazz != null){
//			try {
//				Constructor<?> constructor = clazz.getConstructor(paramTypes);
//				return constructor.newInstance(initargs);
//			} catch (InstantiationException e) {
//				logger.warn("",e);
//			} catch (IllegalAccessException e) {
//				logger.warn("",e);
//			} catch (SecurityException e) {
//				logger.warn("",e);
//			} catch (NoSuchMethodException e) {
//				logger.warn("",e);
//			} catch (IllegalArgumentException e) {
//				logger.warn("",e);
//			} catch (InvocationTargetException e) {
//				logger.warn("",e);
//			} catch (Exception e){
//				logger.error("",e);
//			}
//			return null;
//		}
//		
//		//2. default CL?
//		try{
//			clazz = Class.forName(className);
//			if(clazz != null){
//				try {
//					Constructor<?> constructor = clazz.getConstructor(paramTypes);
//					return constructor.newInstance(initargs);
//				} catch (InstantiationException e) {
//					logger.warn("",e);
//				} catch (IllegalAccessException e) {
//					logger.warn("",e);
//				} catch (SecurityException e) {
//					logger.warn("",e);
//				} catch (NoSuchMethodException e) {
//					logger.warn("",e);
//				} catch (IllegalArgumentException e) {
//					logger.warn("",e);
//				} catch (InvocationTargetException e) {
//					logger.warn("",e);
//				} catch (Exception e){
//					logger.error("",e);
//				}
//				return null;
//			}
//		}catch(ClassNotFoundException e){
//			logger.debug("Not found default class {}", className);
//		}
//		
//		//3. custom CL?
//		Iterator<URLClassLoader> iter = classLoaderList.values().iterator();
//		while(iter.hasNext()){
//			URLClassLoader l = (URLClassLoader)iter.next();
//			try {
//				
//				clazz = Class.forName(className, true, l);
//				logger.info("classname = {}", clazz);
//			} catch (ClassNotFoundException e) {
//				
//				continue;
//			}
//			
//			if(clazz != null){
//				try {
//					Constructor<?> constructor = clazz.getConstructor(paramTypes);
//					logger.info("Found dynamic class {}", className);
//					return constructor.newInstance(initargs);
//				} catch (InstantiationException e) {
//					logger.error("",e);
//				} catch (IllegalAccessException e) {
//					logger.error("",e);
//				} catch (SecurityException e) {
//					logger.error("",e);
//				} catch (NoSuchMethodException e) {
//					logger.error("",e);
//				} catch (IllegalArgumentException e) {
//					logger.error("",e);
//				} catch (InvocationTargetException e) {
//					logger.error("",e);
//				} catch (Exception e){
//					logger.error("",e);
//				}
//				return null;
//			}else{
//				logger.error("Not found dynamic class "+className);
//			}
//		}
//		return null;
//	}
//	
//	
//	public Class<?> loadClass(String className){
//		//1. cache?
//		Class<?> clazz = classCache.get(className);
//		if(clazz != null){
//			try {
//				return clazz;
//			} catch (Exception e){
//				logger.error("",e);
//			}
//			return null;
//		}
//		//2. default CL?
//		try{
//			clazz = Class.forName(className);
//			if(clazz != null){
//				try {
//					return clazz;
//				} catch (Exception e){
//					logger.error("",e);
//				}
//				return null;
//			}
//		}catch(ClassNotFoundException e){
//			logger.debug("Not found default class {}", className);
//		}
//		
//		//3. custom CL?
//		Iterator<URLClassLoader> iter = classLoaderList.values().iterator();
//		while(iter.hasNext()){
//			URLClassLoader l = (URLClassLoader)iter.next();
//			try {
//				clazz = Class.forName(className, true, l);
//			} catch (ClassNotFoundException e) {
//				
//				continue;
//			}
//			
//			if(clazz != null){
//				try {
//					logger.info("Found dynamic class {}", className);
//					return clazz;
//				} catch (Exception e){
//					logger.error("",e);
//				}
//				return null;
//			}else{
//				logger.error("Not found dynamic class {}", className);
//			}
//		}
//		return null;
//	}
//	
//}
