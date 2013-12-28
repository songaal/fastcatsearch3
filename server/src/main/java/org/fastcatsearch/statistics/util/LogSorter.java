package org.fastcatsearch.statistics.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.fastcatsearch.statistics.KeyCountRunEntryReader;
import org.fastcatsearch.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 로그를 정렬한다. 예를들어, 키워드순으로 되어있는 로그를 count순으로 재정렬할때 필요하다.
 * comparator 구현에 따라 다른 정렬도 가능하다.
 * 
 * 기본적으로 중복키가 없다는 가정하에 정렬만 수행한다. distinct기능은 없다. 
 * 
 * 메모리에서 정렬이 힘든 대용량 정렬을 위해 runKeysize만큼만 메모리에서 정렬하고 run파일로 만든후 최종적으로 run파일을 머징하여 정렬된 OutputStream을 내보낸다.
 * */
public class LogSorter {
	protected static Logger logger = LoggerFactory.getLogger(LogSorter.class);

	private int runKeySize;
	private String encoding;
	private InputStream is;

	public LogSorter(InputStream is, String encoding, int runKeySize) {
		this.is = is;
		this.encoding = encoding;
		this.runKeySize = runKeySize;
	}

	public void sort(OutputStream os, Comparator<KeyCountRunEntry> comparator, File workDir) throws IOException {
		if (!workDir.exists()) {
			workDir.mkdir();
		}
		List<KeyCountRunEntry> list = new ArrayList<KeyCountRunEntry>(runKeySize);
		int flushCount = 0;
		KeyCountRunEntryReader entryReader = new KeyCountRunEntryReader(is, encoding);
		try {
			while (entryReader.next()) {
				KeyCountRunEntry entry = entryReader.entry();
//				logger.debug(">>> {}", entry);
				if (entry != null) {
					list.add(entry);

					if (list.size() >= runKeySize) {
						flush(workDir, flushCount++, list, comparator);

					}
				}
			}

		} catch (IOException e) {
			entryReader.close();
		}

		if (list.size() > 0) {
			flush(workDir, flushCount++, list, comparator);
		}

		/*
		 * 2. run들을 하나로 합친다.
		 */
		File[] runFileList = new File[flushCount];
		for (int i = 0; i < flushCount; i++) {
			runFileList[i] = getRunFile(workDir, i);
		}
		KeyCountRunEntryReader[] entryReaderList = getReaderList(runFileList);
		RunEntryMergeReader<KeyCountRunEntry> mergeReader = new RunEntryMergeReader<KeyCountRunEntry>(entryReaderList, comparator);

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, encoding));
		try {
			KeyCountRunEntry entry = null;

			while ((entry = mergeReader.read()) != null) {
				writer.write(entry.getRawLine());
				writer.write("\n");
			}

		} finally {
			for (KeyCountRunEntryReader r : entryReaderList) {
				r.close();
			}

			writer.close();

			FileUtils.deleteQuietly(workDir);
		}

	}

	private void flush(File workDir, int flushCount, List<KeyCountRunEntry> list, Comparator<KeyCountRunEntry> comparator) throws IOException {
		File runFile = getRunFile(workDir, flushCount);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(runFile), encoding));
		try {
			Collections.sort(list, comparator);
			for (KeyCountRunEntry entry : list) {
				writer.write(entry.getRawLine());
				writer.write("\n");
			}
		} finally {
			if (writer != null) {
				writer.close();
			}
			list.clear();
		}
	}

	private File getRunFile(File workDir, int i) {
		return new File(workDir, Integer.valueOf(i) + ".run");
	}

	private KeyCountRunEntryReader[] getReaderList(File[] fileList) throws IOException {
		KeyCountRunEntryReader[] list = new KeyCountRunEntryReader[fileList.length];
		for (int i = 0; i < fileList.length; i++) {
			File f = fileList[i];
			list[i] = new KeyCountRunEntryReader(f, encoding);
			list[i].next();
		}
		return list;
	}
}
