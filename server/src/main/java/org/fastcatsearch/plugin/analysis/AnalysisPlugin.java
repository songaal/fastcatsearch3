package org.fastcatsearch.plugin.analysis;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.ibatis.io.Resources;
import org.fastcatsearch.db.dao.DictionaryDAO;
import org.fastcatsearch.db.dao.CustomDictionaryDAO;
import org.fastcatsearch.db.dao.MapDictionaryDAO;
import org.fastcatsearch.db.dao.SetDictionaryDAO;
import org.fastcatsearch.db.dao.SynonymDictionaryDAO;
import org.fastcatsearch.ir.dic.Dictionary;
import org.fastcatsearch.ir.dictionary.CustomDictionary;
import org.fastcatsearch.ir.dictionary.DAOSourceDictionaryCompiler;
import org.fastcatsearch.ir.dictionary.MapDictionary;
import org.fastcatsearch.ir.dictionary.SetDictionary;
import org.fastcatsearch.ir.dictionary.SourceDictionary;
import org.fastcatsearch.ir.dictionary.SynonymDictionary;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginSetting;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.ColumnSetting;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.DictionarySetting;

public abstract class AnalysisPlugin extends Plugin {

	protected static String dictionaryPath = "dict/";
	protected static String dictionarySuffix = ".dict";

	private final static String dictionaryTableSuffix = "_dictionary";
	public final static String defaultDictionaryMapperFilePath = "org/fastcatsearch/db/mapper/DictionaryMapper.xml";

	protected Map<String, DictionaryDAO> daoMap;

	public AnalysisPlugin(File pluginDir, PluginSetting pluginSetting) {
		super(pluginDir, pluginSetting);
	}

	@Override
	protected void addMapperFile(List<File> mapperFileList) {

		File mapperFile = null;
		try {
			mapperFile = Resources.getResourceAsFile(defaultDictionaryMapperFilePath);
			mapperFileList.add(mapperFile);
		} catch (IOException e) {
			logger.error("error load defaultDictionaryMapperFile", e);
		}
	}

	@Override
	protected void doLoad() {
		prepareDAO();
		loadDictionary();
	}

	@Override
	protected void doUnload() {
		daoMap.clear();
	}

	private void prepareDAO() {
		daoMap = new HashMap<String, DictionaryDAO>();

		AnalysisPluginSetting setting = (AnalysisPluginSetting) pluginSetting;
		List<DictionarySetting> list = setting.getDictionarySettingList();
		if (list != null) {
			for (DictionarySetting dictionarySetting : list) {
				String dictionaryId = dictionarySetting.getId();
				String type = dictionarySetting.getType();
				String tableName = getDictionaryTableName(dictionaryId);

				List<ColumnSetting> columnSettingList = dictionarySetting.getColumnSettingList();
				if (columnSettingList != null) {
					DictionaryDAO dao = null;
					if(type.equals("synonym")){
						dao = new SynonymDictionaryDAO(tableName, columnSettingList, internalDBModule);
					}else{
						dao = new DictionaryDAO(tableName, columnSettingList, internalDBModule);
					}
					boolean isValidDAO = false;
					if(!dao.validateTable()){
						dao.dropTable();
						if(dao.creatTable()){
							isValidDAO = true;
						}
					}else{
						isValidDAO = true;
					}
					if(isValidDAO){
						daoMap.put(dictionaryId, dao);
					}else{
						logger.debug("fail to register dictionary dao > {}", dictionaryId);
					}
				}
			}
		}
	}

	protected abstract void loadDictionary();

	public abstract Dictionary<?> getDictionary();

	public DictionaryDAO getDictionaryDAO(String dictionaryId) {
		return daoMap.get(dictionaryId);
	}

	public Set<Entry<String, DictionaryDAO>> getDictionaryEntrySet() {
		return daoMap.entrySet();
	}

	public String getDictionaryTableName(String dictionaryId) {
		return dictionaryId + dictionaryTableSuffix;
	}

	public File getDictionaryFile(String dictionaryName) {
		return new File(new File(pluginDir, dictionaryPath), dictionaryName + dictionarySuffix);
	}

	@Override
	public AnalysisPluginSetting getPluginSetting() {
		return (AnalysisPluginSetting) pluginSetting;
	}

	public void compileDictionaryFromDAO(String dictionaryId) throws IOException {
		AnalysisPluginSetting setting = (AnalysisPluginSetting) pluginSetting;
		List<DictionarySetting> list = setting.getDictionarySettingList();
		if (list != null) {
			for (DictionarySetting dictionarySetting : list) {
				String id = dictionarySetting.getId();

				List<ColumnSetting> columnSettingList = dictionarySetting.getColumnSettingList();
				if (columnSettingList != null) {

					if (id.equals(dictionaryId)) {
						String type = dictionarySetting.getType();
						DictionaryDAO dictionaryDAO = daoMap.get(dictionaryId);
						// /type에 따라 set, map, synonym, custom을 확인하여 compile 작업수행.
						File targetFile = getDictionaryFile(dictionaryId);
						SourceDictionary dictionaryType = null;
						if (type.equals("set")) {
							dictionaryType = new SetDictionary();
						} else if (type.equals("map")) {
							dictionaryType = new MapDictionary();
						} else if (type.equals("synonym")) {
							dictionaryType = new SynonymDictionary();
						} else if (type.equals("custom")) {
							dictionaryType = new CustomDictionary();
						}

						try {
							DAOSourceDictionaryCompiler.compile(targetFile, dictionaryDAO, dictionaryType, columnSettingList);
						} catch (Exception e) {
							logger.error("dictionary compile error", e);
						}
					}
					break;
				}
			}
		}
	}

}
