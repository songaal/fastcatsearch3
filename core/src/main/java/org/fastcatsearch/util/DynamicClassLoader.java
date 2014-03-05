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

package org.fastcatsearch.util;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicClassLoader {
	private static Logger logger = LoggerFactory.getLogger(DynamicClassLoader.class);
	private static final ReadWriteLock lock = new ReentrantReadWriteLock();
	
	private static Map<String, URLClassLoader> classLoaderList = new HashMap<String, URLClassLoader>();
	
	public static boolean remove(String tag) {
		try{
			lock.writeLock().lock();
			classLoaderList.remove(tag);
		}finally{
			lock.writeLock().unlock();
		}
		return true;
	}
	public static boolean removeAll() {
		try{
			lock.writeLock().lock();
			classLoaderList.clear();
		}finally{
			lock.writeLock().unlock();
		}
		return true;
	}
	
	public static boolean add(String tag, List<File> jarFiles) {
		URL[] jarUrls = new URL[jarFiles.size()];
		return add(tag, jarFiles.toArray(new File[0]));
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
		try{
			lock.writeLock().lock();
			logger.debug("Add Classpath {}:{}", tag, sb.toString());
			classLoaderList.put(tag, l);
		}finally{
			lock.writeLock().unlock();
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
		try{
			lock.writeLock().lock();
			classLoaderList.put(tag, l);
		}finally{
			lock.writeLock().unlock();
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
				try{
					Constructor<?> constructor = clazz.getConstructor(paramTypes);
					return (T) constructor.newInstance(initargs);
				}catch(NoSuchMethodException e){
					logger.trace("해당 생성자가 없습니다. {} >> {} {} {} {} {} {} {}", className, paramTypes);
				}
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
			
		}
//		logger.debug("basic cl {}, {}", clazz, className);
		try{
			lock.readLock().lock();
			Iterator<URLClassLoader> iter = classLoaderList.values().iterator();
			while(iter.hasNext()){
				URLClassLoader l = (URLClassLoader)iter.next();
				try {
					clazz = Class.forName(className, true, l);
//					logger.debug("{} cl {} : {}", l, clazz, className);
				} catch (ClassNotFoundException e) {
//					logger.debug("{} cl {} : {}", l, clazz, className);
					continue;
				}
				
				if(clazz != null){
					return clazz;
				}
			}
		}finally{
			lock.readLock().unlock();
		}
		
		logger.warn("Classloader cannot find {}", className);
		return null;
	}
	
	public static Enumeration<URL> getResources(String name){
		CompoundEnumeration<URL> compoundEnumeration = new CompoundEnumeration<URL>();
		try{
			
			Enumeration<URL> e = ClassLoader.getSystemResources(name);
			if(e != null && e.hasMoreElements()){
				compoundEnumeration.add(e);
			}
			lock.readLock().lock();
			Iterator<URLClassLoader> iter = classLoaderList.values().iterator();
			while(iter.hasNext()){
				URLClassLoader l = (URLClassLoader)iter.next();
				e = l.getResources(name);
				
				logger.debug("getResources {} >> {}, {}", l, e, e.hasMoreElements());
				if(e != null && e.hasMoreElements()){
					compoundEnumeration.add(e);
				}
			}
			
		} catch (IOException e) {
			logger.error("", e);
		}finally{
			lock.readLock().unlock();
		}
		return compoundEnumeration;
	}
	
	public static abstract class ClassScanner<E> {
		public List<E> scanClass(String pkg, ClassLoader classLoader) {
			List<E> classes = new ArrayList<E>();
			String path = pkg.replace(".", "/");
			if (!path.endsWith("/")) {
				path = path + "/";
			}
			try {
				Enumeration<URL> em = null;
				if(classLoader!=null) {
					em = classLoader.getResources(path);
				} else {
					em = DynamicClassLoader.getResources(path);
				}
				while(em.hasMoreElements()) {
					String urlstr = em.nextElement().toString();
					if(urlstr.startsWith("jar:file:")) {
						String jpath = urlstr.substring(9);
						int st = jpath.indexOf("!/");
						String jarPath = jpath.substring(0, st);
						String entryPath = jpath.substring(st + 2);
						JarFile jf = new JarFile(jarPath);
						try {
							Enumeration<JarEntry>jee = jf.entries();
							while(jee.hasMoreElements()) {
								JarEntry je = jee.nextElement();
								String ename = je.getName();
								if (ename.startsWith(entryPath)) {
									E ar = done(ename,pkg);
									if(ar!=null) { classes.add(ar); }
								}
							}
						} finally{
							jf.close();
						}
					} else  if(urlstr.startsWith("file:")) {
						String rootPath = urlstr.substring(5);
						int prefixLength = rootPath.indexOf(path);
						File file = new File(rootPath);
						
						Set<File> fileSet = new HashSet<File>();
						if(file.isDirectory()){
							addDirectory(fileSet, file);
						}else{
							fileSet.add(file);
						}
						for(File f : fileSet){
							String classPath = f.toURI().toURL().toString()
								.substring(5).substring(prefixLength);
							E ar = done(classPath,pkg);
							if(ar!=null) { classes.add(ar); }
						}
					}
				}
				return classes;
			} catch (IOException e) { }
			return null;
		}
		
		private void addDirectory(Set<File> set, File d){
			File[] files = d.listFiles();
			for (int i = 0; i < files.length; i++) {
				if(files[i].isDirectory()){
					addDirectory(set, files[i]);
				}else{
					set.add(files[i]);
				}
			}
		}
		public abstract E done(String ename, String pkg);
	}
}
