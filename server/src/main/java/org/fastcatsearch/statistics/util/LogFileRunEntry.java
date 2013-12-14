package org.fastcatsearch.statistics.util;

/**
 * 메모리에 합산된 키워드별 갯수 데이터의 일부를 flush한 파일의 한 Entry를 나타낸다. 
 * */
public class LogFileRunEntry extends RunEntry {
	private String key;
	private int count;

	public LogFileRunEntry(){
		super(null);
	}
	
	public LogFileRunEntry(String rawLine, String key, int count) {
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
		LogFileRunEntry e = (LogFileRunEntry) o;
		if(e.key == null){
			return -1;
		}
		
		return key.compareTo(e.key);
	}

	@Override
	public void merge(RunEntry other) {
		LogFileRunEntry o = (LogFileRunEntry) other;
		count += o.count;
	}

}
