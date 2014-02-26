package org.fastcatsearch.ir.dic;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fastcatsearch.ir.io.CharVector;

public abstract class Dictionary<E, P> {
	
	public abstract List<E> find(CharVector token);
	
	public abstract P findPreResult(CharVector token);
	
	public abstract void setPreDictionary(Map<CharVector, P> map);
	
	public abstract int size();

	public abstract void appendAdditionalNounEntry(Set<CharVector> set, String tokenType);
}
