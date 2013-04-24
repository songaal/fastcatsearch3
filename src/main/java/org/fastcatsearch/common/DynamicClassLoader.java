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

import org.fastcatsearch.env.Environment;
import org.fastcatsearch.module.AbstractModule;
import org.fastcatsearch.module.AbstractSingletoneModule;
import org.fastcatsearch.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicClassLoader extends AbstractSingletoneModule {
	private static Logger logger = LoggerFactory.getLogger(DynamicClassLoader.class);
	private Map<String, URLClassLoader> classLoaderList = new HashMap<String, URLClassLoader>();
	private Map<String, Class<?>> classCache = new HashMap<String, Class<?>>();
	protected boolean isLoad;
	private static DynamicClassLoader instance;
	
	public DynamicClassLoader(Environment environment, Settings settings){
		super(environment, settings);
	}
	
	public static DynamicClassLoader getInstance(){
		return instance;
	}
	@Override
	public void asSingleton() {
		instance = this;
	}
	@Override
	public boolean doLoad() {
		String jarPath = settings.getString("classpath");
		
		//초기화
		classLoaderList.clear();
		classCache.clear();
		
		if(jarPath != null && jarPath.length() > 0){
			// ',' makes different classloader
			String[] pathList = jarPath.split(",");
			for (int i = 0; i < pathList.length; i++) {
				String tmp = pathList[i];
				String[] tl = tmp.split("[:;]");
				for (int k = 0; k < tl.length; k++) {
					tl[k] = tl[k].trim();
					if(tl[k].length() == 0)
						continue;
					
					tl[k] = environment.filePaths().getPath(tl[k]);
				}
				addClassLoader(tmp, tl);
			}
			
		}
		isLoad = true;
		return true;
	}
	@Override
	public boolean doUnload() {
		classLoaderList.clear();
		classCache.clear();
		isLoad = false;
		return true;
	}
	
	public boolean addClassLoader(String tag, String[] jarFilePath) {
		URL[] jarUrls = new URL[jarFilePath.length];
		for (int i = 0; i < jarFilePath.length; i++) {
			File f = new File(jarFilePath[i]);
			try {
				jarUrls[i] = f.toURI().toURL();
				logger.debug("Add jar = {}", jarUrls[i]);
			} catch (MalformedURLException e) {
				logger.error("Dynamic jar filepath is strange. path="+jarFilePath[i]+"("+f.getAbsolutePath()+")",e);
			}
		}
		
		URLClassLoader l = new URLClassLoader(jarUrls);
		classLoaderList.put(tag, l);
		logger.info("Class Loader {} = {}", tag, l);
		return true;
	}
	
	public Object loadObject(String className){
		Class<?> clazz = loadClass(className);
		if(clazz != null){
			try {
				return clazz.newInstance();
			} catch (Exception e){
				logger.error("",e);
			}
			return null;
		}
		
		return null;
	}
	
	public Object loadObject(String className, Class<?>[] paramTypes ,Object[] initargs){
		Class<?> clazz = loadClass(className);
		if(clazz != null){
			try {
				Constructor<?> constructor = clazz.getConstructor(paramTypes);
				return constructor.newInstance(initargs);
			} catch (Exception e){
				logger.error("",e);
			}
			return null;
		}
		
		return null;
	}
	
	
	public Class<?> loadClass(String className){
		//1. cache?
		Class<?> clazz = classCache.get(className);
		if(clazz != null){
			try {
				return clazz;
			} catch (Exception e){
				logger.error("",e);
			}
			return null;
		}
		//2. default CL?
		try{
			clazz = Class.forName(className);
			if(clazz != null){
				try {
					classCache.put(className, clazz);
					return clazz;
				} catch (Exception e){
					logger.error("",e);
				}
				return null;
			}
		}catch(ClassNotFoundException e){
			logger.debug("Not found default class {}", className);
		}
		
		//3. custom CL?
		Iterator<URLClassLoader> iter = classLoaderList.values().iterator();
		while(iter.hasNext()){
			URLClassLoader l = (URLClassLoader)iter.next();
			try {
				clazz = Class.forName(className, true, l);
			} catch (ClassNotFoundException e) {
				
				continue;
			}
			
			if(clazz != null){
				try {
					logger.info("Found dynamic class {}", className);
					classCache.put(className, clazz);
					return clazz;
				} catch (Exception e){
					logger.error("",e);
				}
				return null;
			}else{
				logger.error("Not found dynamic class {}", className);
			}
		}
		return null;
	}

	
	
}
