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

package org.fastcatsearch.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fastcatsearch.ir.common.IRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HTMLTagRemover {
	
	private static Logger logger = LoggerFactory.getLogger(HTMLTagRemover.class);
	private static String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>"; //script pattern
	private static String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>"; //style pattern  
	private static String regEx_html = "<[/!]?[a-zA-Z]*(\\s)*(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>"; //html pattern 
	private static String regEx_add1 = "<!--[\\s\\S가-힣ㄱ-ㅎ]+?-->";
	
	private static Pattern p_script = Pattern.compile(regEx_script,Pattern.CASE_INSENSITIVE); 
	private static Pattern p_style = Pattern.compile(regEx_style,Pattern.CASE_INSENSITIVE);   
	private static Pattern p_html = Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE);   
	private static Pattern p_add1 = Pattern.compile(regEx_add1,Pattern.CASE_INSENSITIVE);
	
	public static String clean(String targetString) throws IRException{
			
		String htmlStr = targetString.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
		try {
			Matcher m_script = p_script.matcher(htmlStr);
			htmlStr = m_script.replaceAll(""); // clean script

			Matcher m_style = p_style.matcher(htmlStr);
			htmlStr = m_style.replaceAll(""); // clean style

			Matcher m_html = p_html.matcher(htmlStr);
			htmlStr = m_html.replaceAll(""); // clean html
			
			Matcher m_add1 = p_add1.matcher(htmlStr);
			htmlStr = m_add1.replaceAll(""); // clean add1
			
			return htmlStr;

		} catch (Exception e) {
			logger.error("HTML Tag clean Error:" + e.getMessage(),e);
			throw new IRException(e);
		}
	         
	}
}
