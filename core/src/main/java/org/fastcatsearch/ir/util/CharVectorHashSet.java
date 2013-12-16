package org.fastcatsearch.ir.util;

import java.util.HashSet;

import org.fastcatsearch.ir.io.CharVector;

public class CharVectorHashSet extends HashSet<CharVector> {

	private boolean isIgnoreCase;

	public CharVectorHashSet() {
		this(false);
	}

	public CharVectorHashSet(boolean isIgnoreCase) {
		super();
		this.isIgnoreCase = isIgnoreCase;
	}

	public boolean isIgnoreCase() {
		return isIgnoreCase;
	}

	@Override
	public boolean add(CharVector e) {
		if (isIgnoreCase) {
			e.setIgnoreCase();
		}
		return super.add(e);
	}

	@Override
	public boolean contains(Object key) {
		CharVector charKey = (CharVector) key;
		if (isIgnoreCase) {
			if (!charKey.isIgnoreCase()) {
				charKey.setIgnoreCase();
				boolean v = super.contains(charKey);
				charKey.unsetIgnoreCase();
				return v;
			}
		}
		
		return super.contains(charKey);
	}

}
