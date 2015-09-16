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

package org.fastcatsearch.ir.filter;

import java.io.IOException;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.field.FieldDataParseException;
import org.fastcatsearch.ir.io.BytesDataOutput;
import org.fastcatsearch.ir.io.DataRef;
import org.fastcatsearch.ir.query.Filter;
import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.settings.FieldIndexSetting;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 필터기능을 제공한다.
 * */
public abstract class FilterFunction {
	protected static Logger logger = LoggerFactory.getLogger(FilterFunction.class);
	protected FieldSetting fieldSetting;
	protected int boostScore;
	protected boolean isBoostFunction;
    protected String fieldIndexId;
	protected String[] fieldIndexIdList;
	protected Object functionParams;

	protected boolean isMultiField;

	public FilterFunction(Filter filter, FieldIndexSetting fieldIndexSetting, FieldSetting fieldSetting, boolean isBoostFunction) throws FilterException{
        Object fieldIdObject = filter.fieldIndexId();
        if(fieldIdObject instanceof String) {
            fieldIndexIdList = new String[] { (String) fieldIdObject };
            fieldIndexId = (String) fieldIdObject;
			isMultiField = false;
        } else if(fieldIdObject instanceof String[]) {
            fieldIndexIdList = (String[]) fieldIdObject;
            fieldIndexId = fieldIndexIdList[0];
			isMultiField = true;
        }
		this.fieldSetting = fieldSetting;
		this.isBoostFunction = isBoostFunction;
		boostScore = filter.boostScore();
        functionParams = filter.getFunctionParams();
	}

	/**
	 * 필터링에서 buffer의 데이터는 셋팅필드의 바이트길이로 계산되어 입력된다.
	 * 즉, 길이가 10인 셋팅필드에 실제데이터가 1바이트만 들어있든 5바이트만 들어있든 받는 쪽에서는 알수없으므로(나머지는 0으로 채워져있다.), 무조건 패턴길이만큼 비교를 해봐야 한다.   
	 * @param rankInfo
	 * @return true : 포함됨, false : 포함되지 않음.
	 */
	public abstract boolean filtering(RankInfo rankInfo, DataRef dataRef) throws FilterException, IOException;
	
}
