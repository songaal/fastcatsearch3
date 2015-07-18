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
public class ExcludeFilter extends PatternFilterFunction {

	public ExcludeFilter(Filter filter, FieldIndexSetting fieldIndexSetting, FieldSetting fieldSetting) throws FilterException {
		super(filter, fieldIndexSetting, fieldSetting, false);
	}

	public ExcludeFilter(Filter filter, FieldIndexSetting fieldIndexSetting, FieldSetting fieldSetting, boolean isBoostFunction) throws FilterException {
		super(filter, fieldIndexSetting, fieldSetting, isBoostFunction);
	}

	/**
	 * 하나라도 매칭하면 false를 리턴한다.BOOSTING이라면 매칭시 스코어 더해주고 true를 리턴한다.
	 * */
	@Override
	public boolean filtering(RankInfo rankInfo, DataRef dataRef) throws IOException {
		while (dataRef.next()) {

			BytesRef bytesRef = dataRef.bytesRef();
			for (int j = 0; j < patternCount; j++) {
				BytesRef patternBuf = patternList[j];
				int plen = patternBuf.length;

				// 패턴이 데이터보다 크면 match 확인필요없음.
				if (plen > bytesRef.length()) {
					continue;
				}

				if (plen > 0) {
					boolean isMatch = true;
					if (!patternBuf.bytesEquals(bytesRef, plen)) {
						isMatch = false;
					}

					if (isMatch) {
						// 위의 매치에서는 패턴의 길이만 큼만 비교했으므로,
						// 남아있는 데이터가 더 있는지 확인해본다.
						// 널이 아닌 데이터가 남아있다면 모두 매칭하지 않은것이다.

						for (int bufInx = plen; bufInx < bytesRef.length(); bufInx++) {
							if (bytesRef.bytes[bufInx] != 0) {
								isMatch = false;
								break;
							}
						}

						// 최종적으로 isMatch일때에만 리턴한다.
						if (isMatch) {
							// 매칭했다면 false로 리턴한다. 단, boost시는 true리턴.
							if (isBoostFunction) {
								//매칭하지 않았지만 boosting이므로 다음조건을 위해 true
								return true;
							}else{
								//매칭됬으므로 false
								return false;
							}
						}

						// 여기까지 왔다면 매칭되지 않은것이다.
						// 다음패턴으로 이동.
					}

				}

			} // for (int j = 0; j < patternCount; j++) {

		}

		// 모두 매칭하지 않았으므로 true리턴한다.
		if (isBoostFunction) {
			// boost옵션이 있다면 점수를 올려주고 리턴한다.
			rankInfo.addScore(boostScore);
			if(rankInfo.isExplain()) {
				rankInfo.explain(fieldIndexId, boostScore, "EXCLUDE_BOOST_FILTER");
			}
		}
		return true;

	}

}
