package org.fastcatsearch.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.fastcatsearch.db.dao.DAOBase;
import org.fastcatsearch.db.dao.SetDictionary;
import org.fastcatsearch.db.vo.SetDictionaryVO;
import org.fastcatsearch.ir.dic.Dictionary;
import org.fastcatsearch.ir.dictionary.HashSetDictionary;
import org.fastcatsearch.ir.dictionary.ListMapDictionary;

public abstract class AnalysisPlugin extends Plugin {

	protected static String DICT_SYNONYM = "synonym";
	protected static String DICT_USER = "user";
	protected static String DICT_STOP = "stop";
	protected static String DICT_SYSTEM = "system";
	
	protected static String dictionaryPath = "dict/";
	protected static String dictionarySuffix = ".dict";

	public AnalysisPlugin(File pluginDir, PluginSetting pluginSetting) {
		super(pluginDir, pluginSetting);
	}

	protected abstract void loadDictionary();

	public abstract Dictionary<?> getDictionary();

	protected File getDictionaryFile(String dictionaryName) {
		return new File(new File(pluginDir, dictionaryPath), dictionaryName + dictionarySuffix);
	}

	public abstract void compileDictionaryFromDAO(String dictionaryId, DAOBase daoBase) throws IOException;

	protected void compileSetToSetDictionary(String dictionaryId, DAOBase daoBase) throws IOException {
		File dictFile = getDictionaryFile(dictionaryId);
		SetDictionary synonymDictionary = (SetDictionary) daoBase;
		List<SetDictionaryVO> result = synonymDictionary.selectPage(-1, -1);
		HashSetDictionary dictionary = new HashSetDictionary();
		for (int i = 0; i < result.size(); i++) {
			SetDictionaryVO vo = result.get(i);
			dictionary.addEntry(vo.keyword);
		}
		OutputStream out = null;
		try {
			out = new FileOutputStream(dictFile);
			dictionary.writeTo(out);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException ignore) {
				}
			}
		}
		logger.debug("SetToSetDictionary {} entry write to {}", result.size(), dictFile.getAbsolutePath());
	}

	// DAO : SetDictionary
	// Object : ListMapDictionary
	protected void compileSetToMapDictionary(String dictionaryId, DAOBase daoBase) throws IOException {
		File dictFile = getDictionaryFile(dictionaryId);
		SetDictionary synonymDictionary = (SetDictionary) daoBase;
		List<SetDictionaryVO> result = synonymDictionary.selectPage(-1, -1);
		ListMapDictionary dictionary = new ListMapDictionary();
		for (int i = 0; i < result.size(); i++) {
			SetDictionaryVO vo = result.get(i);
			dictionary.addEntry(vo.keyword);
		}
		OutputStream out = null;
		try {
			out = new FileOutputStream(dictFile);
			dictionary.writeTo(out);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException ignore) {
				}
			}
		}
		logger.debug("SetToMapDictionary {} entry write to {}", result.size(), dictFile.getAbsolutePath());
	}
}
