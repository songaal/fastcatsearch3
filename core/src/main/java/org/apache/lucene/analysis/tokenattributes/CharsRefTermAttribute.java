package org.apache.lucene.analysis.tokenattributes;

import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.CharsRef;

/**
 * char
 */
public interface CharsRefTermAttribute extends Attribute {

	public void setBuffer(char[] buffer, int offset, int length);
	public void setOffset(int offset, int length);
	
	public CharsRef charsRef();

	// //시작위치를 하나증가시킨다.
	// public void incrementOffset();
	//
	// //전체 길이를 다시 셋팅한다.
	// public void setLength();
}
