/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.settings;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * 동적인 사용자 설정을 추가하기 위한 컨테이너.
 * @author lupfeliz
 *
 */
//public class CatServerSettingContainer implements UserSettings {
//	
//	private static final Logger logger = LoggerFactory.getLogger(CatServerSettingContainer.class);
//	
//	private List<UserSettings> settings;
//	
//	public CatServerSettingContainer() {
//		this.settings = new ArrayList<UserSettings>();
//	}
//	
//	public void add(UserSettings setting) {
//		settings.add(setting);
//	}
//
//	@SuppressWarnings("rawtypes")
//	public void config(Properties props, Map configContext) {
//		for(UserSettings setting : settings) {
//			setting.config(props, configContext);
//		}
//	}
//	
//	private void classifySettings(String ename, String pkg) {
//		if(ename.endsWith(".class")) {
//			ename = ename.substring(0,ename.length()-6);
//			ename = ename.replaceAll("/", ".");
//			if(ename.startsWith(pkg)) {
//				try {
//					Class<?> pcls = Class.forName(ename);
//					if(pcls != CatServerSettingContainer.class) {
//						Class<?>[] clss = pcls.getInterfaces();
//						for( Class<?> cls : clss) {
//							if(cls == UserSettings.class) {
//								this.add((UserSettings)pcls.newInstance());
//								break;
//							}
//						}
//					}
//				} catch (ClassNotFoundException e) { 
//					logger.error("",e);
//				} catch (InstantiationException e) {
//					logger.error("",e);
//				} catch (IllegalAccessException e) {
//					logger.error("",e);
//				}
//			}
//		}
//	}
//	
//	public void detectUserSettings() {
//		String pkg = "org.fastcatsearch.settings.";
//		ClassLoader clsldr = getClass().getClassLoader();
//		String path = pkg.replace(".", "/");
//		try {
//			Enumeration<URL> em = clsldr.getResources(path);
//			while(em.hasMoreElements()) {
//				String urlstr = em.nextElement().toString();
//				if(urlstr.startsWith("jar:file:")) {
//					String jpath = urlstr.substring(9);
//					int st = jpath.indexOf("!/");
//					jpath = jpath.substring(0,st);
//					JarFile jf = new JarFile(jpath);
//					Enumeration<JarEntry>jee = jf.entries();
//					while(jee.hasMoreElements()) {
//						JarEntry je = jee.nextElement();
//						String ename = je.getName();
//						classifySettings(ename,pkg);
//						
//					}
//					jf.close();
//				} else  if(urlstr.startsWith("file:")) {
//					File file = new File(urlstr.substring(5));
//					File[] dir = file.listFiles();
//					for(int i=0;i<dir.length;i++) {
//						classifySettings(pkg+dir[i].getName(),pkg);
//					}
//				}
//			}
//		} catch (IOException e) { }
//	}
//}
