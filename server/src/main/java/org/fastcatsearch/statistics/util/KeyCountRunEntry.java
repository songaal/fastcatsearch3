package org.fastcatsearch.statistics.util;

/**
 * 메모리에 합산된 키워드별 갯수 데이터의 일부를 flush한 파일의 한 Entry를 나타낸다. 
 * */
public class KeyCountRunEntry extends RunEntry {
	private String key;
	private int count;

	public KeyCountRunEntry(){
		super(null);
	}
	
	public KeyCountRunEntry(String rawLine, String key, int count) {
		super(rawLine);
		this.key = key;
		this.count = count;
	}

	public String getKey() {
		return key;
	}

	public int getCount() {
		return count;
	}

	@Override
	public int compareTo(Object o) {
		if(key == null){
			return 1;
		}
		KeyCountRunEntry e = (KeyCountRunEntry) o;
		if(e.key == null){
			return -1;
		}
		
		return key.compareTo(e.key);
	}

	@Override
	public void merge(RunEntry other) {
		KeyCountRunEntry o = (KeyCountRunEntry) other;
		count += o.count;
	}
	
	@Override
	public String toString(){
		return getClass().getSimpleName()+"] "+key + ": "+ count;
	}

	@Override
	public boolean equals(Object obj) {
		
		return key.equals(((KeyCountRunEntry)obj).key);
	}

}
