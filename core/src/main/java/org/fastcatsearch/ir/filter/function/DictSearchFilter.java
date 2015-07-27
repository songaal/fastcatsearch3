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

import java.io.IOException;

/**
 * 사전을 검색해서 해당 값으로 특정필드를 필터링하는
 * Created by swsong on 2015. 7. 27..
 */
public class DictSearchFilter extends FilterFunction {

    public DictSearchFilter(Filter filter, FieldIndexSetting fieldIndexSetting, FieldSetting fieldSetting) throws FilterException {
        this(filter, fieldIndexSetting, fieldSetting, false);
    }
    public DictSearchFilter(Filter filter, FieldIndexSetting fieldIndexSetting, FieldSetting fieldSetting, boolean isBoostFunction) throws FilterException {
        super(filter, fieldIndexSetting, fieldSetting, isBoostFunction);
        initFunctionParams(filter);
        initParams(filter);
    }

    private void initFunctionParams(Filter filter) throws FilterException {
        if(filter.functionParamLength() != 2) {
            throw new FilterException("DICT_SEARCH filter needs PluginId and DictionaryId.");
        }

    }

    private void initParams(Filter filter) throws FilterException {
        filter.paramLength();
    }
    @Override
    public boolean filtering(RankInfo rankInfo, DataRef dataRef) throws FilterException, IOException {
        while (dataRef.next()) {

            if(isMultiField) {
                BytesRef[] refs = ((CompoundDataRef) dataRef).bytesRefs();
            } else {
                BytesRef ref = dataRef.bytesRef();
            }
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
