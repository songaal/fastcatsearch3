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
import org.fastcatsearch.ir.group.GroupFunctionType;

public class FirstGroupFunction extends GroupFunction {

	public FirstGroupFunction(int sortOrder, String fieldId) {
		super(GroupFunctionType.FIRST, sortOrder, fieldId);
	}

	@Override
	public void addValue(int groupNo, Object value) {
		if(valueList[groupNo].isEmpty()) {
			valueList[groupNo].set(value);
		}
	}

	@Override
	public void done() {
		
	}

}
