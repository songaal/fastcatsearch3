package org.fastcatsearch.statistics.util;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 정렬되어 기록된 여러개의 RUN 파일을 하나로 머징한다. 
 * */
public class SortedFileMerger {
	
	protected static Logger logger = LoggerFactory.getLogger(SortedFileMerger.class);
	File[] runFileList;
	File outputFile;
	Comparator<String> comparator;
	
	public SortedFileMerger(File[] runFileList, File outputFile, Comparator<String> comparator) {
		
	}

	public void merge() {
		int runSize = runFileList.length;
		RunEntryReader[] readerList = new RunEntryReader[runSize];
		MergeEntryReader mergeEntryReader = new MergeEntryReader(readerList);
		
		//TODO MergeEntryReaderTest를 참조하여 구현..
		
		
	}


}
