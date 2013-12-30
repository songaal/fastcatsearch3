package org.fastcatsearch.keyword;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.db.vo.PopularKeywordVO;
import org.fastcatsearch.db.vo.RelateKeywordVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeywordDictionaryCompiler {
	protected static final Logger logger = LoggerFactory.getLogger(KeywordDictionaryCompiler.class);

	public static void compilePopularKeyword(List<PopularKeywordVO> keywordList, File writeFile)
			throws Exception {

		PopularKeywordDictionary dictionary = new PopularKeywordDictionary(keywordList);

		File parentDir = writeFile.getParentFile();

		if (!parentDir.exists()) {
			FileUtils.forceMkdir(parentDir);
		}

		OutputStream ostream = null;

		try {

			ostream = new FileOutputStream(writeFile);

			dictionary.writeTo(ostream);

		} finally {

			if (ostream != null)
				try {
					ostream.close();
				} catch (IOException e) {
				}
		}
	}

	
	
	public void compileRelateKeyword(List<RelateKeywordVO> keywordList, File writeFile)
			throws Exception {

		RelateKeywordDictionary dictionary = new RelateKeywordDictionary();

		for (RelateKeywordVO keyword : keywordList) {
			dictionary.putRelateKeyword(keyword.getKeyword(), keyword.getValue());
		}

		File parentDir = writeFile.getParentFile();

		if (!parentDir.exists()) {
			FileUtils.forceMkdir(parentDir);
		}

		OutputStream ostream = null;

		try {

			ostream = new FileOutputStream(writeFile);

			dictionary.writeTo(ostream);

		} finally {

			if (ostream != null)
				try {
					ostream.close();
				} catch (IOException e) {
				}
		}
	}

}
