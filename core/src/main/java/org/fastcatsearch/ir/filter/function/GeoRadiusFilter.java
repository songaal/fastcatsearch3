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

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.filter.FilterException;
import org.fastcatsearch.ir.filter.FilterFunction;
import org.fastcatsearch.ir.io.CompoundDataRef;
import org.fastcatsearch.ir.io.DataRef;
import org.fastcatsearch.ir.query.Filter;
import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.settings.FieldIndexSetting;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.util.GeoDistance;

import java.io.IOException;

/**
 * 경위도 상의 거리를 판별하여, 필터링해주고, 거리 계산값을 _distance 필드로 넣어주게 된다.
 * 
 * @author swsong
 * 
 */
public class GeoRadiusFilter extends FilterFunction {

    private float latPosition;
    private float lonPosition;
    private float radiusInMeter;

    private GeoDistance geoDistance;

	public GeoRadiusFilter(Filter filter, FieldIndexSetting fieldIndexSetting, FieldSetting fieldSetting) throws FilterException {
		this(filter, fieldIndexSetting, fieldSetting, false);
	}

	public GeoRadiusFilter(Filter filter, FieldIndexSetting fieldIndexSetting, FieldSetting fieldSetting, boolean isBoostFunction) throws FilterException {
		super(filter, fieldIndexSetting, fieldSetting, isBoostFunction);
        initParams(filter);
	}

    private void initParams(Filter filter) throws FilterException {
        int paramLength = filter.paramLength();
        if(paramLength != 3) {
            throw new FilterException("GEO_RADIUS filter needs 3 params : lat, lon, radius. But only " + paramLength +" params specified.");
        }

        try {
            latPosition = Float.parseFloat(filter.param(0));
            lonPosition = Float.parseFloat(filter.param(1));
            radiusInMeter = Float.parseFloat(filter.param(2)) * 1000.0f;
        } catch (NumberFormatException e) {
            throw new FilterException("Invalid GEO_RADIUS filter param. This is not float type : " + filter.param(0) + ", " + filter.param(1) + ", " + filter.param(2));
        }
        geoDistance = new GeoDistance();
    }

	@Override
	public boolean filtering(RankInfo rankInfo, DataRef dataRef) throws FilterException, IOException {
		while (dataRef.next()) {
            if(!(dataRef instanceof CompoundDataRef)) {
                throw new FilterException("GEO_RADIUS need 2 field : latitude, longitude.");
            }
            BytesRef[] refs = ((CompoundDataRef) dataRef).bytesRefs();

            BytesRef bytesRef1 = refs[0]; //lat
            BytesRef bytesRef2 = refs[1]; //lon

            float lat = Float.intBitsToFloat(bytesRef1.toIntValue());
            float lon = Float.intBitsToFloat(bytesRef2.toIntValue());

            float distance = (float) geoDistance.calDistance(lat, lon, latPosition, lonPosition);
//            logger.debug("calDistance {},{} -> {},{} = {} < {}", lat, lon, latPosition, lonPosition, distance, radiusInMeter);
            rankInfo.distance(distance);
            if(distance <= radiusInMeter) {
                if (isBoostFunction) {
                    // boost옵션이 있다면 점수를 올려주고 리턴한다.
                    rankInfo.addScore(boostScore);
                    if(rankInfo.isExplain()) {
                        rankInfo.explain(fieldIndexId, boostScore, "GEO_RADIUS_FILTER");
                    }
                }
                return true;
            }
		}

		return isBoostFunction;

	}

}
