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

import junit.framework.TestCase;

public class QueryParserTest extends TestCase{
	public void test1() throws QueryParseException{
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
	
	public void test2() throws QueryParseException{
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
		Object obj = parser.makeClause(queryString,query);
		System.out.println(obj);
	}
	
}
