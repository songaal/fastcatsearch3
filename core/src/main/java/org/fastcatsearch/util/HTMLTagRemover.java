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

import java.util.regex.Pattern;

import org.fastcatsearch.ir.common.IRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTMLTagRemover {

	private static Logger logger = LoggerFactory.getLogger(HTMLTagRemover.class);
	private static final Pattern ptnTagScript = Pattern.compile("<[\\s]*script[\\s]*[^>]*>[\\s\\S]*?<[\\s]*[/][\\s]*script[\\s]*>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	private static final Pattern ptnTagStyle = Pattern.compile("<[\\s]*style[\\s]*[^>]*>[\\s\\S]*?<[\\s]*[/][\\s]*style[\\s]*>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	private static final Pattern ptnTagTitle = Pattern.compile("<[\\s]*?title[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?title[\\s]*?>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	private static final Pattern ptnContentHtml = Pattern.compile("<[/]?[a-z0-9]+([^>]*)[/]?>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	private static final Pattern ptnContentComment = Pattern.compile("<![-]*[\\s\\S가-힣ㄱ-ㅎ]+?[-]*>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE); //<!뒤에 -- 가 없을수도.. <!DOCTYPE , >앞에 -- 가 없을수도있다.
	private static final Pattern ptnChrSpecial = Pattern.compile("([&][a-zA-Z]{1,8}[;])", Pattern.CASE_INSENSITIVE);
	private static final Pattern ptnChrSpecial2 = Pattern.compile("([&][#][0-9]{1,4}[;])", Pattern.CASE_INSENSITIVE);

	private static final Pattern ptnSpaces = Pattern.compile("[ \\t]{2,}"); // 빈 공백 긴것 삭제
	private static final Pattern ptnMultiLine1 = Pattern.compile("^\\s*[\\n\\r]"); // 여러 공백삭제.
	private static final Pattern ptnMultiLine2 = Pattern.compile("[\\n\\r\\t]{2,}"); // 여러줄 공백삭제.

	public static String clean(String targetString) throws IRException {

		String htmlStr = targetString.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
		try {
			htmlStr = ptnTagScript.matcher(htmlStr).replaceAll(""); // clean script
			htmlStr = ptnTagStyle.matcher(htmlStr).replaceAll(""); // clean style
			htmlStr = ptnTagTitle.matcher(htmlStr).replaceAll(""); // clean style
			htmlStr = ptnContentHtml.matcher(htmlStr).replaceAll(""); // clean html
			htmlStr = ptnContentComment.matcher(htmlStr).replaceAll(""); // clean add1
			htmlStr = htmlStr.replaceAll("&quot;", "\"");
			htmlStr = htmlStr.replaceAll("&#39;", "'");
			htmlStr = ptnChrSpecial.matcher(htmlStr).replaceAll(" ");
			htmlStr = ptnChrSpecial2.matcher(htmlStr).replaceAll(" ");
			htmlStr = ptnSpaces.matcher(htmlStr).replaceAll(" ");
			htmlStr = ptnMultiLine1.matcher(htmlStr).replaceAll("");
			htmlStr = ptnMultiLine2.matcher(htmlStr).replaceAll("\n");
			return htmlStr;

		} catch (Exception e) {
			logger.error("HTML Tag clean Error:" + e.getMessage(), e);
			throw new IRException(e);
		}
	}
}
