package org.fastcatsearch.statistics.util;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.IOUtil;

public class SortedFileMerger {
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
