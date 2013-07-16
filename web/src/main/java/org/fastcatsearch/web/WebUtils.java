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

package org.fastcatsearch.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebUtils {
	private static Logger logger = LoggerFactory.getLogger(WebUtils.class);
	
	public static int getInt(String s, int defaultValue){
		if(s == null){
			return defaultValue;
		}
		
		try{
			return Integer.parseInt(s);
		}catch(NumberFormatException e){
			return defaultValue;
		}
	}
	
	public static float getFloat(String s, float defaultValue){
		if(s == null){
			return defaultValue;
		}
		
		try{
			return Float.parseFloat(s);
		}catch(NumberFormatException e){
			return defaultValue;
		}
	}
	
	public static String getString(String s, String defaultValue){
		if(s == null || s.length() == 0){
			return defaultValue;
		}
		
		return s;
	}
	
	public static void callHttpPost(String url, String urlParams){
		try {
			URL nodeURL = new URL (url);
			byte[] paramData = urlParams.getBytes();
			HttpURLConnection conn = (HttpURLConnection)nodeURL.openConnection();
			
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length", Integer.toString(paramData.length));
			conn.setUseCaches(false);
			conn.setDoOutput(true);
			conn.setDoInput(true);
		
			OutputStream os = conn.getOutputStream();
			os.write(paramData);
			os.flush();
			os.close();
//			logger.debug("Call "+url+", "+urlParams);
			
			InputStream is = conn.getInputStream();
			is.close();
			
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			logger.error("Fail : "+url+", "+urlParams);
		}
		
	}
	
	public static boolean isSameServerPage(String servetPath, String pageName){
		return servetPath.endsWith(pageName);
	}
}	
