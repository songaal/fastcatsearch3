package org.fastcatsearch.ir.dic;

import java.util.List;
import java.util.Set;

import org.fastcatsearch.ir.io.CharVector;

public abstract class Dictionary<E> {
	
	public abstract List<E> find(CharVector token);
	
	public abstract int size();

	public abstract void appendAdditionalNounEntry(Set<CharVector> set, String tokenType);
}
