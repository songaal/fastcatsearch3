package org.fastcatsearch.ir.index;

public class MultiKeyEntry {
	private String[] keys;

	public MultiKeyEntry(String... keys) {
		this.keys = keys;
	}

	public String getKey(int i) {
		return keys[i];
	}

	public int size() {
		return keys.length;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			sb.append(key);
			if(i < keys.length - 1){
				sb.append(";");
			}
		}
		return sb.toString();
	}
}
