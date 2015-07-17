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
import org.fastcatsearch.ir.io.IOUtil;
import org.fastcatsearch.ir.query.Filter;
import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.settings.FieldIndexSetting;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.util.GeoDistance;

import java.io.IOException;

/**
 * 경위도 상의 거리를 판별하여, 필터링해주고, 거리 계산값을 _distance 필드로 넣어준다.
 * 
 * @author swsong
 * 
 */
public class GeoRadiusFilter extends FilterFunction {

    private float latPosition;
    private float lonPosition;
    private float radiusInKm;

    private GeoDistance geoDistance;

	public GeoRadiusFilter(Filter filter, FieldIndexSetting fieldIndexSetting, FieldSetting fieldSetting) throws FilterException {
		super(filter, fieldIndexSetting, fieldSetting, false);
        initFunctionParams();
	}

	public GeoRadiusFilter(Filter filter, FieldIndexSetting fieldIndexSetting, FieldSetting fieldSetting, boolean isBoostFunction) throws FilterException {
		super(filter, fieldIndexSetting, fieldSetting, isBoostFunction);
        initFunctionParams();
	}

    private void initFunctionParams() throws FilterException {
        String[] params = (String[]) functionParams;
        if(params == null || params.length == 0) {
            throw new FilterException("Invalid filter method params = " + functionParams);
        }
        String myLat = params[0];
        String myLon = params[1];
        String radius = params[2];
        try {
            latPosition = Float.parseFloat(myLat);
            lonPosition = Float.parseFloat(myLon);
            radiusInKm = Float.parseFloat(radius);
        } catch (NumberFormatException e) {
            throw new FilterException("Invalid Geo_Radius filter param is not integer = " + functionParams);
        }
        geoDistance = new GeoDistance();
    }

	@Override
	public boolean filtering(RankInfo rankInfo, DataRef dataRef) throws IOException {
		while (dataRef.next()) {
            BytesRef[] refs = ((CompoundDataRef) dataRef).bytesRefs();

            BytesRef bytesRef1 = refs[0]; //lat
            BytesRef bytesRef2 = refs[1]; //lon

            float lat = Float.intBitsToFloat(bytesRef1.toIntValue());
            float lon = Float.intBitsToFloat(bytesRef2.toIntValue());

            double distance = geoDistance.calDistance(lat, lon, latPosition, lonPosition);

            if(distance < radiusInKm) {
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
