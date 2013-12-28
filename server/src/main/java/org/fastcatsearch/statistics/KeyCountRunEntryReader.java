package org.fastcatsearch.statistics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.fastcatsearch.statistics.util.KeyCountRunEntry;
import org.fastcatsearch.statistics.util.RunEntryReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyCountRunEntryReader extends RunEntryReader<KeyCountRunEntry> {
	protected static Logger logger = LoggerFactory.getLogger(KeyCountRunEntryReader.class);
	private BufferedReader reader;

	private KeyCountRunEntry entry;

	public KeyCountRunEntryReader(InputStream is, String encoding) throws IOException {
		reader = new BufferedReader(new InputStreamReader(is, encoding));
	}

	public KeyCountRunEntryReader(File file, String encoding) throws IOException {
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
	}

	@Override
	public boolean next() {
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
//				logger.debug("[{}] read >> {}", this.hashCode(), line);
				String[] el = line.split("\t");
				if (el.length == 2) {
					try {
						entry = new KeyCountRunEntry(line, el[0], Integer.parseInt(el[1]));
					} catch (Exception e) {
						logger.error("", e);
					}
					return true;
				} else {
					// 파싱실패시 다음 라인확인.
					continue;
				}

			}
		} catch (Exception e) {
			logger.error("", e);
		}
		
		entry = null;
		return false;
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
	public KeyCountRunEntry entry() {
		return entry;
	}

}
