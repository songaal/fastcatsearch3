package org.fastcatsearch.statistics.util;

import java.io.File;
import java.io.IOException;

import org.fastcatsearch.statistics.KeyCountRunEntryReader;
import org.fastcatsearch.statistics.WeightedKeyCountRunEntryReader;

/**
 * decay factor를 적용한 머징로그파일을 만들기 위한 클래스. 
 * 예) 0.log * 1.0 + 1.log * 0.9 + 2.log * 0.8 + ...
 * SortedRunFileMerger를 상속받아 구현함.
 * */
public class WeightedSortedRunFileMerger extends SortedRunFileMerger {
	
	private float[] weightList;
	
	public WeightedSortedRunFileMerger(File[] runFileList, float[] weightList, String encoding, AggregationResultWriter writer) {
		super(runFileList, encoding, writer);
		this.weightList = weightList;
	}
	
	@Override
	protected KeyCountRunEntryReader[] getReaderList(File[] fileList) throws IOException {
		KeyCountRunEntryReader[] list = new KeyCountRunEntryReader[fileList.length];
		for (int i = 0; i < fileList.length; i++) {
			File f = fileList[i];
			list[i] = new WeightedKeyCountRunEntryReader(f, encoding, weightList[i]);
			list[i].next();
		}
		return list;
	}
}
