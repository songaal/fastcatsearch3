package org.fastcatsearch.ir.index;

public class PrimaryKeys {
	private int hash;
	private String[] keys;

	public PrimaryKeys(int size) {
		keys = new String[size];
	}

	public PrimaryKeys(String... keys) {
		this.keys = keys;
	}

	public String getKey(int i) {
		return keys[i];
	}

	public int size() {
		return keys.length;
	}

	public void set(int i, String key) {
		keys[i] = key;
	}

	public boolean equals(Object obj) {
		PrimaryKeys o = (PrimaryKeys) obj;
		if (keys == null || o.keys == null) {
			return false;
		}
		if (keys.length == o.keys.length) {
			for (int i = 0; i < keys.length; i++) {
				if (!(keys[i].equals(o.keys[i]))) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			sb.append(key);
			if (i < keys.length - 1) {
				sb.append(";");
			}
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {

		int h = hash;
		
		if (h == 0 && keys != null && keys.length > 0) {
			for (int i = 0; i < keys.length; i++) {
				h += keys[i].hashCode();
			}
			hash = h;
		}
		return h;
	}
}
