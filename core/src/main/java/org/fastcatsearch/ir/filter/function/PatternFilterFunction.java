package org.fastcatsearch.ir.filter.function;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.field.FieldDataParseException;
import org.fastcatsearch.ir.filter.FilterException;
import org.fastcatsearch.ir.filter.FilterFunction;
import org.fastcatsearch.ir.io.BytesDataOutput;
import org.fastcatsearch.ir.query.Filter;
import org.fastcatsearch.ir.settings.FieldIndexSetting;
import org.fastcatsearch.ir.settings.FieldSetting;

import java.io.IOException;

/**
 * 패턴기반의 필터를 처리하는 필터함수 클래스.
 * @author swsong
 * 2015. 7. 18
 */
public abstract class PatternFilterFunction extends FilterFunction {

    protected int patternCount;
    protected BytesRef[] patternList;
    protected BytesRef[] endPatternList;

    public PatternFilterFunction(Filter filter, FieldIndexSetting fieldIndexSetting, FieldSetting fieldSetting, boolean isBoostFunction) throws FilterException {
        super(filter, fieldIndexSetting, fieldSetting, isBoostFunction);
        patternCount = filter.paramLength();
        patternList = new BytesRef[patternCount];
        endPatternList = new BytesRef[patternCount];
        boolean isIgnoreCase = fieldIndexSetting != null ? fieldIndexSetting.isIgnoreCase() : false;

        try {
            for (int j = 0; j < patternCount; j++) {
                //패턴의 byte 데이터를 얻기위해 필드객체를 생성한다.
                //패턴과 필드데이터를 같은 길이의 byte[]로 만들어놓고 비교를 한다.
                String pattern = filter.param(j);
//				logger.debug("Filter Pattern {} : {} isIgnoreCase={}", fieldIndexSetting.getId(), param, isIgnoreCase);

                //ignoreCase로 색인되어있다면 패턴도 대문자로 변환한다.
                if(isIgnoreCase){
                    pattern = pattern.toUpperCase();
                }

                Field f;
                int patternByteSize = 0;
                BytesDataOutput arrayOutput = null;

                if(pattern != null && !"".equals(pattern)) {
                    f = fieldSetting.createPatternField(pattern);
                    patternByteSize = fieldSetting.getByteSize();
                    arrayOutput = new BytesDataOutput(patternByteSize);
                    f.writeFixedDataTo(arrayOutput);
                    patternList[j] = arrayOutput.bytesRef();
                }
//				logger.debug("Filter Pattern>>> {} > {}", param, patternList[j]);

                if(filter.isEndParamExist()){
                    pattern = filter.endParam(j);
                    if(isIgnoreCase){
                        pattern = pattern.toUpperCase();
                    }

                    if(pattern != null && !"".equals(pattern)) {
                        f = fieldSetting.createPatternField(pattern);
                        patternByteSize = fieldSetting.getByteSize();
                        arrayOutput = new BytesDataOutput(patternByteSize);
                        f.writeFixedDataTo(arrayOutput);
                        endPatternList[j] = arrayOutput.bytesRef();
                    }
//					logger.debug("End Filter Pattern>>> {} > {}", param, endPatternList[j]);
                }
            }
        } catch (IOException e) {
            throw new FilterException("필터패턴생성중 에러", e);
        } catch (FieldDataParseException e) {
            throw new FilterException("필터패턴을 파싱할 수 없습니다.", e);
        }


    }

    public BytesRef[] getPatternList(){
        return patternList;
    }
}
