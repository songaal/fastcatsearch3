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

package org.fastcatsearch.query;

import org.fastcatsearch.ir.query.Query;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

import static junit.framework.TestCase.*;

public class QueryParserTest {
	
	private static final Logger logger = LoggerFactory.getLogger(QueryParserTest.class);
	
	@Before
	public void init() {
		assertTrue(true);
		String LOG_LEVEL = System.getProperty("LOG_LEVEL");
		if (LOG_LEVEL == null || "".equals(LOG_LEVEL)) {
			LOG_LEVEL = "DEBUG";
		}

		((ch.qos.logback.classic.Logger) LoggerFactory
				.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME))
				.setLevel(Level.toLevel("DEBUG"));
		((ch.qos.logback.classic.Logger) LoggerFactory
				.getLogger(QueryParser.class)).setLevel(Level
				.toLevel(LOG_LEVEL));
		logger.debug("--------------------------------------------------------------------------------");
	}

	@Test
	public void test1() {
		QueryParser parser = QueryParser.getInstance();
		String queryString = "cn=allgoods&ht=<a>:</a>&sn=0&ln=10" +
				"&so=cache&ud=debug:true,logger:file" +
				"&fl=id,title,img:30,price" +
				"&se={{{gd_nm:AND(마우스 피스):100}OR{brand,goodnm:OR(베트남 신혼 여행):1000}}AND{seller,kindnm:여행사:100}}NOT{seller:adult:100}" +
				"&gr=sellprice:freq:key_desc,goodkind:section_freq:5:freq_asc" +
				"&gc={price:1000:0}" +
				"&ra=_score_,price:asc,point:desc";
		System.out.println(queryString);
		parser.parseQuery(queryString);
	}

	@Test
	public void test3() {
		Query query = new Query();
		//String queryString = "{{{{Title:cc:200:15}AND{Title:aa:200:15}}AND{NOT{{{Title:bb:200:15}OR{Content:cc:100:15}}OR{Content2:cc:100:15}}}}}";
		String queryString = "{test:cc}AND{NOT{test:bb}}";
		//String queryString = "{test:cc}NOT{test:bb}";
		QueryParser parser = QueryParser.getInstance();
		Object obj = parser.makeClause(queryString);
		System.out.println(obj);
	}

	@Test
	public void test2() {
		/*
		 * String queryString = "{" +
				"				{" +
				"					{" +
				"						{Title:AND(aa):200:15}" +
				"						OR" +
				"						{Content,UserName:AND(aa2):100:15}" +
				"					}" +
				"					AND" +
				"					{" +
				"						{Title:AND(bb):200:15}" +
				"						OR" +
				"						{Content,UserName:AND(aa3):100:15}" +
				"					}" +
				"				}" +
				"				NOT" +
				"				{FirstCode:OR(010 012):100:15}" +
				"			}";*/
//		String queryString = "{{{Title,UserName:AND(한화그룹):100:15}AND{Title:(이대준):100:15}}NOT{FirstCode:OR(010 012):100:15}}";
//		String queryString = "{{{{Title:AND(aa):200:15}OR{Content,UserName:AND(aa2):100:15}}AND{{Title:AND(bb):200:15}OR{Content,UserName:AND(aa3):100:15}}}NOT{FirstCode:OR(010 012):100:15}}";
		Query query = new Query();
		String queryString = "{{{{Title:cc:200:15}AND{Title:aa:200:15}}AND{{{Title:bb:200:15}OR{Content:cc:100:15}}OR{Content2:cc:100:15}}}}";
		QueryParser parser = QueryParser.getInstance();
		Object obj = parser.makeClause(queryString);
		System.out.println(obj);
	}
}
