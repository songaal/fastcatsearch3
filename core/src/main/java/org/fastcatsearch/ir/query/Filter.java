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

package org.fastcatsearch.ir.query;

import org.fastcatsearch.ir.filter.FilterException;
import org.fastcatsearch.ir.filter.FilterFunction;
import org.fastcatsearch.ir.filter.NotSupportedFilterFunctionException;
import org.fastcatsearch.ir.filter.function.*;
import org.fastcatsearch.ir.settings.FieldIndexSetting;
import org.fastcatsearch.ir.settings.FieldSetting;

public class Filter {

	// filter function for bitwise calculation
	public static final int MATCH = 1 << 0;
	public static final int SECTION = 1 << 1;
	public static final int PREFIX = 1 << 2;
	public static final int SUFFIX = 1 << 3;
	public static final int MATCH_BOOST = 1 << 4;
	public static final int SECTION_BOOST = 1 << 5;
	public static final int PREFIX_BOOST = 1 << 6;
	public static final int SUFFIX_BOOST = 1 << 7;
	public static final int EXCLUDE = 1 << 8;
	public static final int EXCLUDE_BOOST = 1 << 9;
    public static final int BOOST = 1 << 10;
    public static final int GEO_RADIUS = 1 << 11;
    public static final int GEO_RADIUS_BOOST = 1 << 12;
	public static final int EMPTY = 1 << 13;
	public static final int SECTION_EXCLUDE = 1 << 14;
	public static final int MATCH_ORDER = 1 << 15;

	private Object fieldIndexId;
	private int function;
	private String[] functionParamList;
	private String[] paramList;
	private String[] endParamList;
	private int boostScore;

    private Object functionParams; //filter function의 파라미터.

	public Filter(Object fieldIndexId, int function) {
		this(fieldIndexId, function, "");
	}

	public Filter(Object fieldIndexId, int function, String param) {
		this(fieldIndexId, function, param, null, 0);
	}

	public Filter(Object fieldIndexId, int function, String param, String endParam) {
		this(fieldIndexId, function, param, endParam, 0);
	}

	public Filter(Object fieldIndexId, int function, String param, int boostScore) {
		this(fieldIndexId, function, param, null, boostScore);
	}

	public Filter(Object fieldIndexId, int function, String param, String endParam, int boostScore) {
        this.fieldIndexId = convertFieldIndexIdToUpperCase(fieldIndexId);
        this.function = function;
		this.paramList = new String[] { param };
		if (endParam != null) {
			this.endParamList = new String[] { endParam };
		}
		this.boostScore = boostScore;
	}

	//
	// LIST
	//
    public Filter(Object fieldIndexId, int function, String[] functionParamList, String[] paramList) {
        this(fieldIndexId, function, functionParamList, paramList, null, 0);
    }

	public Filter(Object fieldIndexId, int function, String[] functionParamList, String[] paramList, String[] endParamList) {
		this(fieldIndexId, function, functionParamList, paramList, endParamList, 0);
	}

	public Filter(Object fieldIndexId, int function, String[] functionParamList, String[] paramList, int boostScore) {
		this(fieldIndexId, function, functionParamList, paramList, null, 0);
		this.boostScore = boostScore;
	}

	public Filter(Object fieldIndexId, int function, String[] functionParamList, String[] paramList, String[] endParamList, int boostScore) {
		this.fieldIndexId = convertFieldIndexIdToUpperCase(fieldIndexId);
        this.function = function;
		this.functionParamList = functionParamList;
        this.paramList = paramList;
        this.endParamList = endParamList;
        this.boostScore = boostScore;
	}

    private Object convertFieldIndexIdToUpperCase(Object fieldIndexId) {
        if(fieldIndexId instanceof String) {
            return ((String) fieldIndexId).toUpperCase();
        } else if(fieldIndexId instanceof String[]) {
            String[] fieldIndexIdList = (String[]) fieldIndexId;
            for(int i = 0; i < fieldIndexIdList.length;i++) {
                fieldIndexIdList[i] = fieldIndexIdList[i].toUpperCase();
            }
            return fieldIndexIdList;
        }
        return fieldIndexId;
    }

	public FilterFunction createFilterFunction(FieldIndexSetting fieldIndexSetting, FieldSetting fieldSetting) throws NotSupportedFilterFunctionException, FilterException {
		switch (function) {
		case MATCH:
			return new MatchFilter(this, fieldIndexSetting, fieldSetting);
		case MATCH_BOOST:
			return new MatchFilter(this, fieldIndexSetting, fieldSetting, true);
		case MATCH_ORDER:
			return new MatchOrderFilter(this, fieldIndexSetting, fieldSetting);
		case SECTION:
			return new SectionFilter(this, fieldIndexSetting, fieldSetting);
		case SECTION_BOOST:
			return new SectionFilter(this, fieldIndexSetting, fieldSetting, true);
		case PREFIX:
			return new PrefixFilter(this, fieldIndexSetting, fieldSetting); // char필드만지원.
		case PREFIX_BOOST:
			return new PrefixFilter(this, fieldIndexSetting, fieldSetting, true);
		case SUFFIX:
			return new SuffixFilter(this, fieldIndexSetting, fieldSetting); // char필드만지원.
		case SUFFIX_BOOST:
			return new SuffixFilter(this, fieldIndexSetting, fieldSetting, true);
		case EXCLUDE:
			return new ExcludeFilter(this, fieldIndexSetting, fieldSetting);
		case EXCLUDE_BOOST:
			return new ExcludeFilter(this, fieldIndexSetting, fieldSetting, true);
		case BOOST:
			return new BoostFilter(this, fieldIndexSetting, fieldSetting);
        case GEO_RADIUS:
            return new GeoRadiusFilter(this, fieldIndexSetting, fieldSetting);
        case GEO_RADIUS_BOOST:
            return new GeoRadiusFilter(this, fieldIndexSetting, fieldSetting, true);
		case EMPTY:
			return new EmptyFilter(this, fieldIndexSetting, fieldSetting);
		case SECTION_EXCLUDE:
			return new SectionExcludeFilter(this, fieldIndexSetting, fieldSetting);

		}
		throw new NotSupportedFilterFunctionException("지원하지 않는 필터기능입니다. function=" + function);
	}

	public String toString() {
        String str = fieldIndexId + ":" + function + ":";
		if (endParamList != null) {
            if(paramList != null) {
                for (int i = 0; i < paramList.length; i++) {
                    str += (paramList[i] + "~" + endParamList[i]);
                    if (i < paramList.length - 1)
                        str += ";";
                }
            }
		} else {
            if(paramList != null) {
                for (int i = 0; i < paramList.length; i++) {
                    str += paramList[i];
                    if (i < paramList.length - 1)
                        str += ";";
                }
            }
		}
		return str + ":" + boostScore;
	}

    public void setFunctionParams(Object functionParams) {
        this.functionParams = functionParams;
    }

    public Object getFunctionParams() {
        return functionParams;
    }

	public Object fieldIndexId() {
		return fieldIndexId;
	}

	public int function() {
		return function;
	}

	public String param() {
		return paramList[0];
	}

	public String param(int n) {
		return paramList[n];
	}

	public int paramLength() {
		return paramList.length;
	}

	public String functionParam() {
		return functionParamList[0];
	}

	public String functionParam(int n) {
		return functionParamList[n];
	}

	public int functionParamLength() {
		return functionParamList.length;
	}

	public boolean isEndParamExist() {
		return endParamList != null;
	}

	public String endParam(int n) {
		return endParamList[n];
	}

	public int endParamLength() {
		return endParamList.length;
	}

	public int boostScore() {
		return boostScore;
	}
}
