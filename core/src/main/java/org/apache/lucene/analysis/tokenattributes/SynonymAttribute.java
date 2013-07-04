package org.apache.lucene.analysis.tokenattributes;

import org.apache.lucene.util.Attribute;
import org.fastcatsearch.ir.io.CharVector;

/**
 */
public interface SynonymAttribute extends Attribute {

	public void setSynonym(CharVector[] synonym);
	
	public CharVector[] getSynonym();
	
}
