package org.fastcatsearch.ir.util;

import java.util.HashMap;
import java.util.Map;

import org.fastcatsearch.ir.io.CharVector;

public class CharVectorHashMap<V> extends HashMap<CharVector, V> {

	private boolean isIgnoreCase;

	public CharVectorHashMap() {
		this(false);
	}

	public CharVectorHashMap(boolean isIgnoreCase) {
		super();
		this.isIgnoreCase = isIgnoreCase;
	}
	public CharVectorHashMap(Map<CharVector, V> m, boolean isIgnoreCase) {
		super(m);
		this.isIgnoreCase = isIgnoreCase;
	}
	public boolean isIgnoreCase() {
		return isIgnoreCase;
	}

	@Override
	public V get(Object key) {

		CharVector charKey = (CharVector) key;
		if (isIgnoreCase) {
			if (!charKey.isIgnoreCase()) {
				charKey.setIgnoreCase();
				V v = super.get(charKey);
				charKey.unsetIgnoreCase();
				return v;
			}
		}

		return super.get(charKey);
	}

	@Override
	public boolean containsKey(Object key) {
		return this.get(key) != null;
	}

	@Override
	public V put(CharVector charKey, V value) {
		if (isIgnoreCase) {
			charKey.setIgnoreCase();
		}
		return super.put(charKey, value);
	}

}
