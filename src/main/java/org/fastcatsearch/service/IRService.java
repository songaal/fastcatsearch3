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

package org.fastcatsearch.service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.fastcatsearch.ir.analysis.TokenizerAttributes;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.IRConfig;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.ir.config.SettingException;
import org.fastcatsearch.ir.dic.Dic;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.util.ClassDetector;


public class IRService extends CatServiceComponent{
	private static IRService instance;
	
	private Map<String, CollectionHandler> collectionHandlerMap = new HashMap<String, CollectionHandler>();
	private String[] collectionNameList;
	private String[][] tokenizerList;
	
	public static IRService getInstance() {
		if(instance == null)
			instance = new IRService();
		return instance;
	}
	
	private IRService() { }
	
	protected boolean start0() throws ServiceException {
		IRConfig irconfig = IRSettings.getConfig(true);
logger.debug("--------------------------------------------------------------------------------");
		//load dictionary
		try {
			Dic.init();
		} catch (IRException e1) {
			throw new ServiceException(e1);
		}
logger.debug("--------------------------------------------------------------------------------");
		
		
		String collectionList = irconfig.getString("collection.list");
		collectionNameList = collectionList.split(",");
		
		for (int i = 0; i < collectionNameList.length; i++) {
			String collection = collectionNameList[i];
			try {
				collectionHandlerMap.put(collection, new CollectionHandler(collection));
			} catch (IRException e) {
				logger.error("[ERROR] "+e.getMessage(),e);
			} catch (SettingException e) {
				logger.error("[ERROR] "+e.getMessage(),e);
			} catch (Exception e) {
				logger.error("[ERROR] "+e.getMessage(),e);
			}
		}
logger.debug("--------------------------------------------------------------------------------");
		
		detectTokenizers();
		
		return true;
	}
	
	public String[] getCollectionNames(){
		return collectionNameList;
	}
	
	public String[][] getTokenizers() {
		return tokenizerList;
	}
	
	public CollectionHandler removeCollectionHandler(String collection){
		return collectionHandlerMap.remove(collection);
	}
	
	public CollectionHandler getCollectionHandler(String collection){
		return collectionHandlerMap.get(collection);
	}
	
	public CollectionHandler putCollectionHandler(String collection, CollectionHandler collectionHandler){
		return collectionHandlerMap.put(collection, collectionHandler);
	}

	protected boolean shutdown0() throws ServiceException {
		Iterator<Entry<String, CollectionHandler>> iter = collectionHandlerMap.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String, CollectionHandler> entry = iter.next();
			try {
				entry.getValue().close();
				logger.info("Collection " + entry.getKey()+ " Shutdown!");
			} catch (IOException e) {
				logger.error("[ERROR] "+e.getMessage(),e);
				throw new ServiceException("IRService 종료중 에러발생.", e);
			}
		}
		return true;
	}	
	
	public void detectTokenizers() {
		ClassDetector<String[]> detector = new ClassDetector<String[]>() {
			@Override
			public String[] classify(String ename, String pkg) {
				if(ename.endsWith(".class")) {
					ename = ename.substring(0,ename.length()-6);
					ename = ename.replaceAll("/", ".");
					if(ename.startsWith(pkg)) {
						try {
							Class<?> cls = Class.forName(ename);
							TokenizerAttributes tokenizerAttributes = cls.getAnnotation(TokenizerAttributes.class);
							if(tokenizerAttributes!=null) {
								return new String[] { tokenizerAttributes.name(), cls.getName() };
							}
						} catch (ClassNotFoundException e) { }
					}
				}
				return null;
			}
		};
		
		List<String[]>tokenizers = detector.detectClass("org.fastcatsearch.ir.analysis.");
		if(tokenizers!=null && tokenizers.size() > 0) {
			tokenizerList = new String[tokenizers.size()][];
			tokenizerList = tokenizers.toArray(tokenizerList);
		}
	}

	public void detectFieldTypes() {
		ClassDetector<String[]> detector = new ClassDetector<String[]>() {
			@Override
			public String[] classify(String ename, String pkg) {
				if(ename.endsWith(".class")) {
					ename = ename.substring(0,ename.length()-6);
					ename = ename.replaceAll("/", ".");
					if(ename.startsWith(pkg)) {
						try {
							Class<?> cls = Class.forName(ename);
							if(org.fastcatsearch.ir.config.Field.class.equals(cls)) {
							} else if(org.fastcatsearch.ir.field.SingleValueField.class.equals(cls)) {
							} else if(org.fastcatsearch.ir.field.MultiValueField.class.equals(cls)) {
							} else if(org.fastcatsearch.ir.field.MultiValueField.class.isAssignableFrom(cls)) {
							} else if(org.fastcatsearch.ir.config.Field.class.isAssignableFrom(cls)) {
								String fieldType = ename.substring(pkg.length());
								if(fieldType.endsWith("Field")) { fieldType = fieldType.substring(0,fieldType.length()-5); }
								fieldType = fieldType.toLowerCase();
								return new String[] { fieldType, cls.getName() };
							}
						} catch (ClassNotFoundException e) { }
					}
				}
				return null;
			}
		};
		detector.detectClass("org.fastcatsearch.ir.config.");
	}
}
