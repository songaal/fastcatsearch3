package org.fastcatsearch.statistics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.fastcatsearch.statistics.util.LogFileRunEntry;
import org.fastcatsearch.statistics.util.RunEntryReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogFileRunEntryReader extends RunEntryReader<LogFileRunEntry> {
	protected static Logger logger = LoggerFactory.getLogger(LogFileRunEntryReader.class);
	private BufferedReader reader;

	private LogFileRunEntry entry;

	public LogFileRunEntryReader(File file) throws IOException {
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
	}

	@Override
	public boolean next() {
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				logger.debug("[{}] read >> {}", this.hashCode(), line);
				String[] el = line.split("\t");
				if (el.length == 2) {
					entry = new LogFileRunEntry(line, el[0], Integer.parseInt(el[1]));
					return true;
				}
			}
		} catch (Exception e) {
			logger.error("", e);
		}

		// EOF와 구분되어야하므로, 파싱실패는 빈 객체를 넘겨준다.
		entry = null;
		return true;
	}

	@Override
	public void close() {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				logger.error("", e);
			}
		}
	}

	@Override
	public LogFileRunEntry entry() {
		return entry;
	}

}
