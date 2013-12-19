package org.apache.lucene.analysis.tokenattributes;

import org.apache.lucene.util.Attribute;

/**
 * 추출된 단어(형태소)가 의미를 가진 중요한 단어인지, 아니면 어미,조사와 같이 앞의 단어에 종속적인지 판단하는 속성.
 * 문장검색시 부가단어는 발견이 안되어도 검색되도록 하여 품질을 높일수 있다. 
 */
public interface FeatureAttribute extends Attribute {

	public static enum FeatureType {
		NULL, MAIN, APPEND, ADDITION
	}

	public static final FeatureType DEFAULT_TYPE = FeatureType.NULL;

	/**
	 * 
	 * @see #setType(String)
	 */
	public FeatureType type();

	/**
	 * Set the lexical type.
	 * 
	 * @see #type()
	 */
	public void setType(FeatureType type);
}
