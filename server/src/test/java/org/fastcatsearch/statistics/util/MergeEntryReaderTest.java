package org.fastcatsearch.statistics.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.fastcatsearch.statistics.LogFileRunEntryReader;
import org.junit.Test;

public class MergeEntryReaderTest {

	@Test
	public void testLogFileMerge() throws IOException {
		String destDir = "src/test/resources/statistics/rt/test";
		
		File[] fileList = new File(destDir).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".log");
			}
		});

		LogFileRunEntryReader[] entryReaderList = getReaderList(fileList);
		MergeEntryReader<LogFileRunEntry> reader = new MergeEntryReader<LogFileRunEntry>(entryReaderList);

		try {
			LogFileRunEntry entry = null;

			while ((entry = reader.read()) != null) {
				System.out.println(entry);
			}

		} finally {
			for (LogFileRunEntryReader r : entryReaderList) {
				r.close();
			}
		}
	}

	private LogFileRunEntryReader[] getReaderList(File[] fileList) throws IOException {
		LogFileRunEntryReader[] list = new LogFileRunEntryReader[fileList.length];
		for (int i = 0; i < fileList.length; i++) {
			File f = fileList[i];
			list[i] = new LogFileRunEntryReader(f);
			list[i].next();
		}
		return list;
	}
}
