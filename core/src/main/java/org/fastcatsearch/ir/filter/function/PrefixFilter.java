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

package org.fastcatsearch.ir.filter.function;

import java.io.IOException;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.filter.FilterException;
import org.fastcatsearch.ir.filter.FilterFunction;
import org.fastcatsearch.ir.io.DataRef;
import org.fastcatsearch.ir.query.Filter;
import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.settings.FieldIndexSetting;
import org.fastcatsearch.ir.settings.FieldSetting;

/**
 * 패턴과 데이터비교시. 패턴길이 > 데이터길이 이면 비교하지 않고 불일치처리.
 * 
 * @author swsong
 * 
 */
public class PrefixFilter extends PatternFilterFunction {

	public PrefixFilter(Filter filter, FieldIndexSetting fieldIndexSetting, FieldSetting fieldSetting) throws FilterException {
		super(filter, fieldIndexSetting, fieldSetting, false);
	}

	public PrefixFilter(Filter filter, FieldIndexSetting fieldIndexSetting, FieldSetting fieldSetting, boolean isBoostFunction) throws FilterException {
		super(filter, fieldIndexSetting, fieldSetting, isBoostFunction);
	}

	@Override
	public boolean filtering(RankInfo rankInfo, DataRef dataRef) throws IOException {
		while (dataRef.next()) {

			BytesRef bytesRef = dataRef.bytesRef();
			for (int j = 0; j < patternCount; j++) {
				BytesRef patternBuf = patternList[j];
				int plen = patternBuf.length;

				// Prefix에서도 패턴이 데이터보다 크면 match확인필요없음. 다음으로 진행.
				if (plen > bytesRef.length()) {
					continue;
				}

				if (plen > 0) {
					if (patternBuf.bytesEquals(bytesRef, plen)) {
						if (isBoostFunction) {
							// boost옵션이 있다면 점수를 올려주고 리턴한다.
							rankInfo.addScore(boostScore);
							if(rankInfo.isExplain()) {
								rankInfo.explain(fieldIndexId, boostScore, "PREFIX_BOOST_FILTER");
							}
						}
						return true;
					}

					// 여기까지 왔다면 매칭되지 않은것이다.
					// 다음패턴으로 이동.
				}

			} // for (int j = 0; j < patternCount; j++) {

		}

		return isBoostFunction;

	}

	private boolean byteEqualsReverse() {
		// 뒤에서 부터 매칭테스트.
		// for (int k = 0; k < buf.limit; k++) {
		// // for (int k = 0; k < fieldByteSize; k++) {
		// if(buf.array[buf.limit - 1 - k] != buffer.array[pos - k]){
		// isMatch = false;
		// break;
		// }
		// }

		return false;
	}
}
