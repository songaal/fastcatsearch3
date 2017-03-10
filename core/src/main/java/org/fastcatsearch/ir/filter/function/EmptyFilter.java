package org.fastcatsearch.ir.filter.function;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.filter.FilterException;
import org.fastcatsearch.ir.filter.FilterFunction;
import org.fastcatsearch.ir.io.DataRef;
import org.fastcatsearch.ir.query.Filter;
import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.settings.FieldIndexSetting;
import org.fastcatsearch.ir.settings.FieldSetting;

import java.io.IOException;

/**
 * Created by 전제현 on 2017. 3. 9..
 */
public class EmptyFilter extends PatternFilterFunction {

    private static byte YES1 = 0x59;
    private static byte YES2 = 0x79;
    private static byte NO1 = 0x4e;
    private static byte NO2 = 0x6e;

    public EmptyFilter(Filter filter, FieldIndexSetting fieldIndexSetting, FieldSetting fieldSetting) throws FilterException {
        super(filter, fieldIndexSetting, fieldSetting, false);
    }

    public EmptyFilter(Filter filter, FieldIndexSetting fieldIndexSetting, FieldSetting fieldSetting, boolean isBoostFunction) throws FilterException {
        super(filter, fieldIndexSetting, fieldSetting, isBoostFunction);
    }

    @Override
    public boolean filtering(RankInfo rankInfo, DataRef dataRef) throws FilterException, IOException {

        boolean empty = false;
        boolean noEmpty = true;
        BytesRef pattern = patternList[0];
        byte check = pattern.bytes[1];

        while (dataRef.next()) {

            if (check == YES1 || check == YES2) {
                empty = true;
                noEmpty = false;
            }
            
            BytesRef bytesRef = dataRef.bytesRef();

            for (int bufInx = 0; bufInx < bytesRef.length(); bufInx++) {
                if (bytesRef.bytes[bufInx] != 0) {
                    return noEmpty;
                }
            }
        }

        return empty;
    }
}
