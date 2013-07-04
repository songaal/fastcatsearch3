package org.apache.lucene.analysis.tokenattributes;

import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.CharsRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharsRefTermAttributeImpl extends AttributeImpl implements CharsRefTermAttribute, Cloneable {
	
	private static Logger logger = LoggerFactory.getLogger(CharsRefTermAttributeImpl.class);
	
	private CharsRef charsRef = null;

	@Override
	public void setBuffer(char[] buffer, int offset, int length) {
		charsRef = new CharsRef(buffer, offset, length);
	}

	@Override
	public void setOffset(int offset, int length) {
		charsRef.offset = offset;
		charsRef.length = length;
	}
	
	@Override
	public CharsRef charsRef() {
		return charsRef;
	}
	@Override
	public String toString(){
//		logger.debug("off={}, len={}", charsRef.offset, charsRef.length);
		try{
		return charsRef.toString();
		}catch(StringIndexOutOfBoundsException e){
			logger.debug("{} charsRef={}, off={}, len={}", charsRef.chars, charsRef.offset, charsRef.length);
			throw e;
		}
	}
	
	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public void copyTo(AttributeImpl target) {
		// TODO Auto-generated method stub

	}

	

}
