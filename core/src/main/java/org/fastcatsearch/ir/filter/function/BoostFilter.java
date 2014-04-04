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
import org.fastcatsearch.ir.settings.FieldSetting.Type;

/**
 * 패턴비교없이 숫자형 데이터값을 score에 더해준다.
 * 
 * @author swsong
 * 
 */
public class BoostFilter extends FilterFunction {

	public BoostFilter(Filter filter, FieldIndexSetting fieldIndexSetting, FieldSetting fieldSetting) throws FilterException {
		super(filter, fieldIndexSetting, fieldSetting, true);
		
		if(fieldSetting.getType() == Type.INT){
			
		}else if(fieldSetting.getType() == Type.LONG){
			
		}else if(fieldSetting.getType() == Type.FLOAT){
			
		}else if(fieldSetting.getType() == Type.DOUBLE){
			
		}
	}

	@Override
	public boolean filtering(RankInfo rankInfo, DataRef dataRef) throws IOException {
		while (dataRef.next()) {
			
			BytesRef bytesRef = dataRef.bytesRef();
			
			if(fieldSetting.getType() == Type.INT){
				rankInfo.addScore(bytesRef.toIntValue());
			}else if(fieldSetting.getType() == Type.LONG){
				rankInfo.addScore(bytesRef.toLongValue());
			}else if(fieldSetting.getType() == Type.FLOAT){
				rankInfo.addScore(Float.intBitsToFloat(bytesRef.toIntValue()));
			}else if(fieldSetting.getType() == Type.DOUBLE){
				rankInfo.addScore((float) Double.longBitsToDouble(bytesRef.toLongValue()));
			}
			return true;
		}
		
		return true;

	}

}
