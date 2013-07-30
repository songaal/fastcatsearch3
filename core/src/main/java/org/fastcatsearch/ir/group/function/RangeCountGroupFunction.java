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

/**
 * 그룹결과를 다시한번 정제하여 정해진 range 수 만큼 구간통계로 바꾸어 주는 function
 * */
public class RangeCountGroupFunction extends GroupFunction {

	private final static String FUNCTION_NAME = "RANGE";

	public RangeCountGroupFunction(int sortOrder, String param) {
		super(FUNCTION_NAME, sortOrder, param);
	}

	@Override
	public String getHeaderName() {
		return FUNCTION_NAME + "_" + fieldId;
	}
	
	@Override
	public void done() {
		if (fieldId != null) {
			int sectionCount = Integer.parseInt(fieldId);

			// make section
//			convertToSection(sectionCount);
		}

	}
//	TODO 구현필요..
	
	
//	protected void convertToSection(int sectionCount) {
//		
//		GroupEntry[] result = new GroupEntry[sectionCount];
//		int unitSize = totalFrequencySum / sectionCount;
//		if (totalFrequencySum > (unitSize * sectionCount)) {
//			unitSize += 1;
//		}
//		logger.debug("## totalFrequencySum = {}, sectionCount = {}, unitSize = {}, size = {}", totalFrequencySum, sectionCount, unitSize, size);
//		
//		int sum = 0;
//		int section = 1;
//		int startIndex = 0;
//		int unitSum = 0;
//
//		String lastEndKey = null;
//		String newKey = null;
//		String startKey = null;
//		
//		for (int i = 0; i < valueList.length; i++) {
//			
//			GroupingValue groupingValue = valueList[i];
//			
//			logger.debug("## groupEntryList[i].count + sum = {}, unitSize * section={}", groupEntryList[i].count() + sum, (unitSize * section));
//			
//			if ((groupEntryList[i].count() + sum) >= unitSize * section) {
//				startKey = groupEntryList[startIndex].key.getKeyString();
//				String endKey = groupEntryList[i].key.getKeyString();
//
//				newKey = startKey + " ~ " + endKey;
//
//				logger.debug("SECTION-" + (section - 1) + " : " + newKey + " = " + unitSum);
//				result[section - 1] = new GroupEntry(new GroupKey.StringKey(newKey.toCharArray()), unitSum);
//				startIndex = i + 1;
//				unitSum = 0;
//				section++;
//
//			}
//
//			sum += groupEntryList[i].count();
//			unitSum += groupEntryList[i].count();
//
//			// end 값을 알아낸다.
//			lastEndKey = groupEntryList[i].key.getKeyString();
//
//		}
//
//		logger.debug("rest unitSum = " + unitSum);
//
//		// logger.debug("endKey = "+ new String(groupEntryList[size - 1].charKey()));
//
//		// 남은 값들을 더해준다.
//		if (unitSum > 0) {
//			newKey = startKey + " ~ " + lastEndKey;
//			int count = result[section - 2].count();
//			logger.debug("L SECTION-" + (section - 2) + " : " + newKey + " = " + (count + unitSum));
//			result[section - 2] = new GroupEntry(new GroupKey.StringKey(newKey.toCharArray()), count + unitSum);
//		}
//
//		// 새 값으로 변경해준다.
//		groupEntryList = result;
//		size = section - 1;
//		// totalFrequencySum 는 불변.
//
//		// return new GroupFrequency(result, section - 1, totalFrequencySum, FieldSetting.Type.UChar);
//	}

	@Override
	public void addValue(int groupNo, Number value) {
		valueList[groupNo].increment();
	}

}
