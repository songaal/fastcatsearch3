package org.apache.lucene.analysis.tokenattributes;

import org.apache.lucene.util.Attribute;

import java.util.List;

/**
 */
public interface CompoundTermAttribute extends Attribute {

	public void setCompounds(List synonym);
	
	public List getCompounds();
	
}
