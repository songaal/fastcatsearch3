package org.apache.lucene.analysis.tokenattributes;

import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Attribute;

public interface AdditionalTermAttribute extends Attribute {
	
	public void init(TokenStream tokenStream);
	
	void addAdditionalTerm(String additionalTerm, String type,
			@SuppressWarnings("rawtypes") List synonyms, int start, int end);
	
	int size();

	Iterator<String> iterateAdditionalTerms();

}
