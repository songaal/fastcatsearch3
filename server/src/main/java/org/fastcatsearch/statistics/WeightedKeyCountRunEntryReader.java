package org.fastcatsearch.statistics;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.fastcatsearch.statistics.util.KeyCountRunEntry;
/**
 * key, count 쌍의 로그파일을 읽을 때 이전통계에 대해서는 decay factor를 적용하기 위해서 count에 1보다 작은 weight를 곱해준다.
 * */
public class WeightedKeyCountRunEntryReader extends KeyCountRunEntryReader {

	private float weight;
	
	public WeightedKeyCountRunEntryReader(File file, String encoding, float weight) throws IOException {
		super(file, encoding);
		this.weight = weight;
	}
	public WeightedKeyCountRunEntryReader(InputStream is, String encoding, float weight) throws IOException {
		super(is, encoding);
		this.weight = weight;
	}
	
	@Override
	protected KeyCountRunEntry newKeyCountRunEntry(String line, String keyword, int count){
		count = (int) (count * weight);
		return new KeyCountRunEntry(line, keyword, count);
	}

}
