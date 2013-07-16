package org.apache.lucene.analysis.tokenattributes;

import java.util.Iterator;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Attribute;

public interface AdditionalTermAttribute extends Attribute {
	
	public void init(TokenStream tokenStream);

	public void addAdditionalTerm(String additionalTerm, int start, int end);
	
	public Iterator<String> iterateAdditionalTerms();
}
