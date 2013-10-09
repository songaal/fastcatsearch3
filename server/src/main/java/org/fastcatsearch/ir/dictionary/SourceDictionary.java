package org.fastcatsearch.ir.dictionary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.fastcatsearch.ir.dic.Dictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SourceDictionary extends Dictionary implements WritableDictionary {
	protected static Logger logger = LoggerFactory.getLogger(SourceDictionary.class);

	public SourceDictionary(){
	}
	
	public void loadSource(File file, boolean ignoreCase, boolean[] valuesIgnoreCase) {
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			loadSource(is, ignoreCase, valuesIgnoreCase);
		} catch (FileNotFoundException e) {
			logger.error("사전소스파일을 찾을수 없습니다.", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ignore) {
				}
			}
		}

	}

	public void loadSource(InputStream is, boolean ignoreCase, boolean[] valuesIgnoreCase) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
			String line = null;
			while ((line = br.readLine()) != null) {
				addSourceLineEntry(line, ignoreCase, valuesIgnoreCase);
			}
		} catch (IOException e) {
			logger.error("", e);
		}
	}
	
	public abstract void addSourceLineEntry(String line, boolean ignoreCase, boolean[] valuesIgnoreCase);
	
}
