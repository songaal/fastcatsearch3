package org.fastcatsearch.ir.io;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.settings.FieldSetting.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CompoundDataRef extends DataRef {
	protected static Logger logger = LoggerFactory.getLogger(CompoundDataRef.class);

    protected DataRef[] dataRefs;
    protected BytesRef[] bytesRefs;

	public CompoundDataRef(){ }

	public CompoundDataRef(DataRef[] dataRef) {
		this.dataRefs = dataRef;
        bytesRefs = new BytesRef[dataRef.length];

        for (int i = 0; i < dataRefs.length; i++) {
            bytesRefs[i] = dataRefs[i].bytesRef();
        }
	}

    @Override
	//다시 읽을때사용.
	public void reset(){

        for (int i = 0; i < dataRefs.length; i++) {
            dataRefs[i].reset();
        }
	}

    @Override
	public void init(int count){
        for (int i = 0; i < dataRefs.length; i++) {
            dataRefs[i].init(count);
        }
	}

    @Override
	public boolean next() throws IOException{
        boolean ret = true;
        for (int i = 0; i < dataRefs.length; i++) {
            ret = ret && dataRefs[i].next();
        }
        return ret;
	}

    @Override
	public BytesRef bytesRef(){
		return bytesRefs[0];
	}

    public BytesRef[] bytesRefs(){
        return bytesRefs;
    }

    @Override
	public void setType(Type type) {
		//ignore
	}

    @Override
	public Object getValue(){

        Object[] values = new Object[dataRefs.length];
        for (int i = 0; i < dataRefs.length; i++) {
            values[i] = dataRefs[i].getValue();
        }

		return values;
	}

    @Override
	public int count(){
        return dataRefs[0].count();
	}
	
	public void skip() throws IOException{
		//do nothing
	}
	
}
