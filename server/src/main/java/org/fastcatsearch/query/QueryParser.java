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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.ir.group.GroupFunction;
import org.fastcatsearch.ir.group.function.CountGroupFunction;
import org.fastcatsearch.ir.query.Clause;
import org.fastcatsearch.ir.query.Filter;
import org.fastcatsearch.ir.query.Filters;
import org.fastcatsearch.ir.query.Group;
import org.fastcatsearch.ir.query.Groups;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.Sort;
import org.fastcatsearch.ir.query.Sorts;
import org.fastcatsearch.ir.query.Term;
import org.fastcatsearch.ir.query.Term.Option;
import org.fastcatsearch.ir.query.View;
import org.fastcatsearch.util.DynamicClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * 
 * @author sangwook.song
 *
 */

public class QueryParser {
	
	private static Logger logger = LoggerFactory.getLogger(QueryParser.class);
	
	private static final String PLUGIN_PACKAGE_PATH = "org.fastcatsearch.ir.group.function"; 
	private static final String PLUGIN_CLASSNAME_SUFFIX = "GroupFunction";
	
	private static QueryParser instance = new QueryParser();
	
	private String COLLECTION_NAME = "cn";
	private String HIGHLIGHT_TAG = "ht";
	private String START_NUMBER = "sn";
	private String LENGTH = "ln";
	private String SEARCH_OPTION = "so";
	private String USER_DATA = "ud";
	private String FIELD_LIST = "fl";
	private String SEARCH = "se";
	private String FILTER = "ft";
	private String GROUP = "gr";
	private String GROUP_CLAUSE = "gc";
	private String GROUP_FILTER = "gf";
	private String RANK = "ra";
	
	private String GROUP_SEPARATOR = "(?<!\\\\)&";
	private String VALUE_SEPARATOR = "(?<!\\\\)=";
	private String FIELD_SEPARATOR1 = "(?<!\\\\),";
	private String FIELD_SEPARATOR2 = "(?<!\\\\):";
	private String FIELD_SEPARATOR3 = "(?<!\\\\);";
	private String FIELD_SEPARATOR4 = "(?<!\\\\)~";
	
	private Map<String, Integer> map;
	
	private QueryParser(){
		map = new HashMap<String, Integer>();
		int i=0;
		map.put(COLLECTION_NAME, i++);
		map.put(HIGHLIGHT_TAG, i++);
		map.put(START_NUMBER, i++);
		map.put(LENGTH, i++);
		map.put(SEARCH_OPTION, i++);
		map.put(USER_DATA, i++);
		map.put(FIELD_LIST, i++);
		map.put(SEARCH, i++);
		map.put(FILTER, i++);
		map.put(GROUP, i++);
		map.put(GROUP_CLAUSE, i++);
		map.put(GROUP_FILTER, i++);
		map.put(RANK, i++);
	}
	
	public static QueryParser getInstance(){
		return instance;
	}
	//검색 term 에서 사용 
	private String removeEscape(String str){
		return str.replaceAll("\\\\:", ":").replaceAll("\\\\&", "&").replaceAll("\\\\=", "=").replaceAll("\\\\,", ",");
	}
	//필터 값에서 사용.
	//필터는 여러 값으로 필터링이 가능해야 하므로 ; 구분자를 사용한다.
	//그리고 범위지정에 ~를 사용한다.
	private void removeEscape(String[] strList){
		for(int i=0;i<strList.length;i++)
			strList[i] = strList[i].replaceAll("\\\\:", ":").replaceAll("\\\\&", "&").replaceAll("\\\\=", "=").replaceAll("\\\\,", ",").replaceAll("\\\\;", ";").replaceAll("\\\\~", "~");
	}
	
	public Query parseQuery(String queryString) throws QueryParseException{
		try{
			String[] groups = queryString.split(GROUP_SEPARATOR);
			Query query = new Query();
			query.setMeta(new Metadata());
			for (int i = 0; i < groups.length; i++) {
				String[] tmp = groups[i].split(VALUE_SEPARATOR);
				if(tmp.length < 2){
//					logger.debug("Skip parsing = "+groups[i]);
					continue;
				}
				String type = tmp[0];
				String value= tmp[1];
				Integer typeNum = map.get(type);
				if(typeNum == null || typeNum == -1){
					logger.debug("Unknown query component = "+type);
					continue;
				}
				
	//			logger.debug(">>"+type +" = "+value);
				
				switch(typeNum){
				case 0: //COLLECTION_NAME
				{
					Metadata m = query.getMeta();
					m.setCollectionName(value);
					break;
				}
				case 1: //HIGHLIGHT_TAG
				{
					Metadata m1 = query.getMeta();
					String tags[] = value.split(FIELD_SEPARATOR2);
					removeEscape(tags);
					m1.setTags(tags);
					break;
				}
				case 2: //START_NUMBER
				{
					Metadata m2 = query.getMeta();
					int sn = Integer.parseInt(value);
					if(sn < 1)
						throw new QueryParseException("Start number has to be greater than 0.");
					m2.setStart(sn);
					break;
				}
				case 3: //LENGTH
				{
					Metadata m3 = query.getMeta();
					int len = Integer.parseInt(value);
					if(len < 1)
						throw new QueryParseException("Length has to be greater than 0.");
					m3.setRows(len);
					break;
				}
				case 4: //SEARCH_OPTION
				{
					Metadata m4 = query.getMeta();
					int so = getSearchOption(value);
					m4.setOptions(so);
					break;
				}
				case 5: //USER_DATA
				{
					Metadata m5 = query.getMeta();
					String[] list = value.split(FIELD_SEPARATOR1);
					HashMap<String, String> map = new HashMap<String, String>();
					for (int k = 0; k < list.length; k++) {
						String[] keyValue = list[k].split(FIELD_SEPARATOR2);
						//key:value 매핑.
						if(keyValue.length == 2){
							removeEscape(keyValue);
							map.put(keyValue[0], keyValue[1]);
						}
					}
					m5.setUserData(map);
					break;
				}
				case 6: //FIELD_LIST
				{
					String[] list = value.split(FIELD_SEPARATOR1);
					List<View> views = new ArrayList<View>(list.length);
					for (int k = 0; k < list.length; k++) {
						String[] str = list[k].split(FIELD_SEPARATOR2);
						if(str.length > 2){
							views.add(new View(str[0], Integer.parseInt(str[1]), Integer.parseInt(str[2]), false));
						} else if(str.length > 1){
							views.add(new View(str[0], Integer.parseInt(str[1])));
						}else{
							views.add(new View(list[k]));
						}
					}
					query.setViews(views);
					break;
				}
				case 7: //SEARCH
					//{{gd_nm:AND(마우스 피스):100}OR{brand,goodnm:AND(베트남 신혼 여행):1000}}AND{seller,kindnm:여행사:100}
				{
					Clause clause = (Clause)makeClause(value);
					query.setClause(clause);
					break;
				}
				case 8: //FILTER
					//ft=sellprice:section:500:2000
					//ft=username:match:james,category:prefix:1030
				{
					Filters f = makeFilters(value);
					query.setFilters(f);
					break;
				}
				case 9: //GROUP
					//&gr=sellprice:COUNT:key_desc,goodkind:section_COUNT:5:COUNT_asc
					//category:section_COUNT;2:key_asc:10  => section은 2개로 하고, 키로 내림차순 정렬, 상위10개만 가져옴. 
				{
					String[] list = value.split(FIELD_SEPARATOR1);
					Groups g = new Groups();
					for (int k = 0; k < list.length; k++) {
						String[] items = list[k].split(FIELD_SEPARATOR2);
						if(items.length < 2){
							//정보부족 에러.
							logger.error("그룹핑 조건 정보부족 => "+list[k]);
							continue;
						}
						
						String field = items[0];
//						String shortFunctionName = items[1];
						int limit = -1;
						int sortOrder = 0;

						//function은 ; 구분으로 파라미터 나눈다.
						// 기능이름;파라미터1;파라미터2 등둥.. 
						
						String[] functionList = items[1].split(FIELD_SEPARATOR3);
						if(items.length > 2){
							if(items.length > 3){
								//마지막은 limit이다.
								limit = Integer.parseInt(items[3]);
							}
							//items[2]는 정렬옵션
							sortOrder = getGroupSortOrder(items[2]);
						}
						
						GroupFunction[] groupFunctions = new GroupFunction[functionList.length];
						int idx = 0;
						for (int j = 0; j < functionList.length; j++) {
							String functionExpr = functionList[j];
							String functionName = null; 
							String param = null;
							if(functionExpr.contains("(")){
								
								String[] funcTmp = functionExpr.split("(");
								functionName = funcTmp[0];
								param = funcTmp[1].substring(0, funcTmp[1].length() - 1);
								
							}else{
								functionName = functionExpr;
							}
							
							GroupFunction groupFunction = null;
							if(functionName.equalsIgnoreCase(Group.DEFAULT_GROUP_FUNCTION_NAME)){
								//기본 클래스.
								groupFunction = new CountGroupFunction(sortOrder, param);
							}else{
								//사용자가 만든 XXXX_COUNT
								String className = convertToClassName(functionName);
								groupFunction = DynamicClassLoader.loadObject(className, GroupFunction.class
										, new Class<?>[] {int.class, String.class}, new Object[]{sortOrder, param});
							}
							groupFunctions[j] = groupFunction;
						}
						
						g.add(new Group(field, groupFunctions, sortOrder, limit));
						
					}
					query.setGroups(g);
					break;
				}
				case 10://GROUP_CLAUSE
					//&gc={price:1000:0}
				{
					Clause clause = (Clause)makeClause(value);
					query.setGroupClause(clause);
					break;
				}
				case 11://GROUP_FILTER
					//&gf=upd_date:section:2010-10-10:2011-01-01
				{
					Filters f = makeFilters(value);
					query.setGroupFilters(f);
					break;
				}
				case 12: //RANK
					//&ra=_score_,price:asc,point:desc
				{	Sorts s = new Sorts();
					String[] list = value.split(FIELD_SEPARATOR1);
					for (int k = 0; k < list.length; k++) {
						String[] str = list[k].split(FIELD_SEPARATOR2);
						if(str.length > 1){
							boolean isAsc = str[1].equalsIgnoreCase("asc");
							s.add(new Sort(str[0], isAsc));
						}else{
							s.add(new Sort(list[k]));
						}
						
					}
					query.setSorts(s);
					break;
				}
				
				}//switch
				
				
			}
			
			//if highlight option is on
			if((query.getMeta().option() & Query.SEARCH_OPT_HIGHLIGHT) > 0){
				if(query.getClause() != null)
					query.getClause().forceHighlight();
			}
			
			logger.debug("query = {}", query);
			return query;
		}catch(Exception e){
			logger.error("Error while parse query => ",e);
			throw new QueryParseException(e);
		}
		
	}

	
	private static String convertToClassName(String groupFunctionName){
		String tempClassName = null;
		String packageName = null;
		String className = "";
		
		if(groupFunctionName.contains(".")){
			int idx = groupFunctionName.lastIndexOf(".");
			packageName = groupFunctionName.substring(0, idx);
			tempClassName = groupFunctionName.substring(idx + 1);
		}else{
			tempClassName = groupFunctionName;
		}
		
		if(tempClassName.contains("_")){
			String[] parts2 = tempClassName.split("_");
			for (int i = 0; i < parts2.length; i++) {
				className += capitalize(parts2[i]);
			}
		}else{
			className = capitalize(groupFunctionName);
		}
		
		if(packageName != null){
			return PLUGIN_PACKAGE_PATH + "." + packageName + "." + className + PLUGIN_CLASSNAME_SUFFIX;
		}else{
			return PLUGIN_PACKAGE_PATH + "." + className + PLUGIN_CLASSNAME_SUFFIX;
		}
		
	}
	
	private static String capitalize(String str){
		char firstChar = str.charAt(0);
		
		if(firstChar >= 'a' && firstChar <= 'z'){
			return ((char) (firstChar - 32)) + str.substring(1);
		}else{
			return str;
		}
		
	}
	
	private int getGroupSortOrder(String string) {
		if(string.equalsIgnoreCase("KEY_ASC")){
			return Group.SORT_KEY_ASC;
		}else if(string.equalsIgnoreCase("KEY_DESC")){
			return Group.SORT_KEY_DESC;
		}else if(string.equalsIgnoreCase("COUNT_ASC")){
			return Group.SORT_VALUE_ASC;
		}else if(string.equalsIgnoreCase("COUNT_DESC")){
			return Group.SORT_VALUE_DESC;
		}
		return 0;
	}
	
	//
	//TODO 괄호없이 A or B and C 형태도 가능토록, escapse 문자 '\' 적용필요.
	//
	protected Object makeClause(String value) throws QueryParseException {
		logger.debug("makeClause = {}", value);
		if(value.charAt(0) == '{'){
			int pos = findMatchBrace(value, 1);
			
			if(pos < 0)
				logger.error("Cannot find match brace '}'.");
			
//			logger.debug("pos={}, value={}",pos, value);
			Object operand1 = makeClause(value.substring(1, pos));
			if(value.regionMatches(true, pos + 1, "OR", 0, 2)){
				int end = findMatchBrace(value, pos + 4);//value.indexOf('}', pos + 4);
				Object operand2 = makeClause(value.substring(pos+4, end));
//				logger.debug("OR!");
				return new Clause(operand1,Clause.Operator.OR, operand2);
			}else if(value.regionMatches(true, pos + 1, "AND", 0, 3)){
				int end = findMatchBrace(value, pos + 5);//value.indexOf('}', pos + 5);
				Object operand2 = makeClause(value.substring(pos+5, end));
//				logger.debug("AND!");
				return new Clause(operand1,Clause.Operator.AND, operand2);
			}else if(value.regionMatches(true, pos + 1, "NOT", 0, 3)){
				int end = findMatchBrace(value, pos + 5);//value.indexOf('}', pos + 5);
				Object operand2 = makeClause(value.substring(pos+5, end));
//				logger.debug("NOT!");
				return new Clause(operand1,Clause.Operator.NOT, operand2);
			}else{
				//operator가 없거나 잘못되었으면 뒤는 무시하고 operand1 이 term으로 간주된다.
				if(operand1 instanceof Term)
					return new Clause(operand1);
				else
					return operand1;
			}
			
			
		}else{
			/*
			 * Unary NOT을 지원하기위함.
			 * 예) NOT{title,body:AND(방송):100:32}
			 * */
			if(value.startsWith("NOT{")){
				int end = findMatchBrace(value, 4);
				Object operand2 = makeClause(value.substring(4, end));
				return new Clause(null,Clause.Operator.NOT, operand2);
			}else{
				return makeTerm(value);
			}
		}
		
	}

	private int findMatchBrace(String value, int m) {
		int depth = 0;
		for (; m < value.length(); m++) {
//			logger.debug(m+"="+value.charAt(m));
			if(value.charAt(m) == '{'){
				depth++;
//				logger.debug("depth="+depth);
			}else if(value.charAt(m) == '}'){
//				logger.debug("depth="+depth);
				if(depth == 0)
					return m;
				
				depth--;
//				logger.debug("depth="+depth);
			}
		}
		return -1;
	}
	
	private Term makeTerm(String value) throws QueryParseException {
		try{
			logger.debug("Term => {}", value);
			String[] list = value.split(FIELD_SEPARATOR2);
			if(list.length == 1){
				throw new QueryParseException("Term field syntax error. No Search keyword => "+value);
			}else if(list.length == 2){
				//field:term
				String[] fieldList = list[0].split(FIELD_SEPARATOR1);
				String[] term = new String[1];
				Term.Type type = getType(list[1], term);
				return new Term(fieldList, removeEscape(term[0]), type);
			}else if(list.length == 3){
				//field:term:score
				String[] fieldList = list[0].split(FIELD_SEPARATOR1);
				String[] term = new String[1];
				Term.Type type = getType(list[1], term);
				return new Term(fieldList, removeEscape(term[0]), Integer.parseInt(list[2]), type);
			}else if(list.length == 4){
				//field:term:score:option
				String[] fieldList = list[0].split(FIELD_SEPARATOR1);
				String[] term = new String[1];
				Term.Type type = getType(list[1], term);
				return new Term(fieldList, removeEscape(term[0]), Integer.parseInt(list[2]), type, new Option(Integer.parseInt(list[3])));
			}else{
				throw new QueryParseException("Term field syntax error. Too many options => "+value);
			}
		}catch(Exception e){
			throw new QueryParseException(e);
		}
		
	}

	private Term.Type getType(String str, String[] term) throws QueryParseException{
		if(str.startsWith("ALL")){
			term[0] = str.substring(4, str.length() - 1);
			return Term.Type.ALL;
		}else if(str.startsWith("ANY")){
			term[0] = str.substring(4, str.length() - 1);
			return Term.Type.ANY;
		}else if(str.startsWith("EXT")){
			term[0] = str.substring(4, str.length() - 1);
			logger.debug("str = "+str);
			return Term.Type.EXT;
		}
		//통째로 반환. default = AND
		term[0] = str;
		return Term.Type.ALL;
	}
	
	private int getSearchOption(String value) {
		int num = 0;
		
		if(value.contains("nocache"))
			num |= Query.SEARCH_OPT_NOCACHE;
		if(value.contains("highlight"))
			num |= Query.SEARCH_OPT_HIGHLIGHT;
		return num;
	}

	private Filters makeFilters(String value){
		String[] list = value.split(FIELD_SEPARATOR1);
		Filters f = new Filters();
		for (int k = 0; k < list.length; k++) {
			String[] str = list[k].split(FIELD_SEPARATOR2);
			if(str.length <= 1){
				logger.error("Filter grammar error.");
				return f;
			}else if(str.length <= 2){
				logger.error("Filter pattern string is empty.");
				return f;
			}
			String field = str[0];
			String method = str[1];
			String[] patternList = str[2].split(FIELD_SEPARATOR3);
			removeEscape(patternList);
			if(method.equalsIgnoreCase("match")){
				f.add(new Filter(field, Filter.MATCH, patternList));
			}else if(method.equalsIgnoreCase("section")){
				String[] patList = new String[patternList.length];
				String[] endPatList = new String[patternList.length];
				
				for (int i = 0; i < patternList.length; i++) {
					String[] range = patternList[i].split(FIELD_SEPARATOR4);
					patList[i] = range[0];
					if(range.length >= 2) {
						endPatList[i] = range[1];
						logger.debug("["+patList[i]+"~"+endPatList[i]+"]");
					} else {
						endPatList[i] = "";
						logger.debug("["+patList[i]+"~]");
					}
				}
				f.add(new Filter(field, Filter.SECTION, patList, endPatList));
			}else if(method.equalsIgnoreCase("prefix")){
				f.add(new Filter(field, Filter.PREFIX, patternList));
			}else if(method.equalsIgnoreCase("suffix")){
				f.add(new Filter(field, Filter.SUFFIX, patternList));
			}else if(method.equalsIgnoreCase("match_boost")){
				if(str.length >= 4){
					int boostScore = Integer.parseInt(str[3]);
					f.add(new Filter(field, Filter.MATCH_BOOST, patternList, boostScore));
				}else{
					logger.warn("MATCH_BOOST Pattern string is empty.");
				}
			}else if(method.equalsIgnoreCase("section_boost")){
				if(str.length >= 4){
					String[] patList = new String[patternList.length];
					String[] endPatList = new String[patternList.length];
					for (int i = 0; i < patternList.length; i++) {
						String[] range = patternList[i].split(FIELD_SEPARATOR4);
						patList[i] = range[0];
						if(range.length >= 2)
							endPatList[i] = range[1];
						else
							endPatList[i] = "";
					}
					int boostScore = Integer.parseInt(str[3]);
					f.add(new Filter(field, Filter.SECTION_BOOST, patList, endPatList, boostScore));
				}else{
					logger.warn("SECTION_BOOST Pattern string or boost score is empty.");
				}
			}else if(method.equalsIgnoreCase("prefix_boost")){
				int boostScore = Integer.parseInt(str[3]);
				f.add(new Filter(field, Filter.PREFIX_BOOST, patternList, boostScore));
			}else if(method.equalsIgnoreCase("suffix_boost")){
				int boostScore = Integer.parseInt(str[3]);
				f.add(new Filter(field, Filter.SUFFIX_BOOST, patternList, boostScore));
			}else if(method.equalsIgnoreCase("exclude")){
				f.add(new Filter(field, Filter.EXCLUDE, patternList));
			}else{
				logger.error("Unknown Filter method = "+ method);
			}
			
		}
		return f;
		
	}
}
