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

package org.fastcatsearch.ir.group.function;

import org.fastcatsearch.ir.group.GroupFunction;

public class MaxGroupFunction extends GroupFunction {

	private final static String FUNCTION_NAME = "MAX";

	public MaxGroupFunction(int sortOrder, String fieldId) {
		super(FUNCTION_NAME, sortOrder, fieldId);
	}

	@Override
	public void addValue(int groupNo, Number value) {
		valueList[groupNo].setIfMax(value);
	}

	@Override
	public String getHeaderName() {
		return FUNCTION_NAME + "_" + fieldId;
	}

}
