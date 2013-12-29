package org.fastcatsearch.statistics.util;

import java.io.File;
import java.io.IOException;

import org.fastcatsearch.statistics.KeyCountRunEntryReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 정렬되어 기록된 여러개의 RUN 파일을 하나로 머징한다. 
 * */
public class SortedRunFileMerger implements RunMerger {
	
	protected static Logger logger = LoggerFactory.getLogger(SortedRunFileMerger.class);
	
	private File[] runFileList;
	private AggregationResultWriter writer;
	protected String encoding;
	
	public SortedRunFileMerger(File[] runFileList, String encoding, AggregationResultWriter writer) {
		this.runFileList = runFileList;
		this.encoding = encoding;
		this.writer = writer;
	}

	@Override
	public void merge() throws IOException {
		KeyCountRunEntryReader[] entryReaderList = getReaderList(runFileList);
		RunEntryMergeReader<KeyCountRunEntry> reader = new RunEntryMergeReader<KeyCountRunEntry>(entryReaderList);

		try {
			KeyCountRunEntry entry = null;

			while ((entry = reader.read()) != null) {
				writer.write(entry.getKey(), entry.getCount());
			}

		} finally {
			for (KeyCountRunEntryReader r : entryReaderList) {
				r.close();
			}
			
			writer.close();
		}
		
		
	}
	
	protected KeyCountRunEntryReader[] getReaderList(File[] fileList) throws IOException {
		KeyCountRunEntryReader[] list = new KeyCountRunEntryReader[fileList.length];
		for (int i = 0; i < fileList.length; i++) {
			File f = fileList[i];
			list[i] = new KeyCountRunEntryReader(f, encoding);
			list[i].next();
		}
		return list;
	}

}
