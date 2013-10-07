package org.fastcatsearch.plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.ibatis.io.Resources;
import org.fastcatsearch.db.dao.AbstractDictionaryDAO;
import org.fastcatsearch.db.dao.CustomDictionaryDAO;
import org.fastcatsearch.db.dao.MapDictionaryDAO;
import org.fastcatsearch.db.dao.SetDictionaryDAO;
import org.fastcatsearch.ir.dic.Dictionary;
import org.fastcatsearch.ir.dictionary.CustomDictionary;
import org.fastcatsearch.ir.dictionary.DAOSourceDictionaryCompiler;
import org.fastcatsearch.ir.dictionary.MapDictionary;
import org.fastcatsearch.ir.dictionary.SetDictionary;
import org.fastcatsearch.ir.dictionary.SourceDictionary;
import org.fastcatsearch.ir.dictionary.SynonymDictionary;
import org.fastcatsearch.plugin.AnalysisPluginSetting.DictionarySetting;

public abstract class AnalysisPlugin extends Plugin {

	protected static String DICT_SYNONYM = "synonym";
	protected static String DICT_USER = "user";
	protected static String DICT_STOP = "stop";
	protected static String DICT_SYSTEM = "system";
	
	protected static String dictionaryPath = "dict/";
	protected static String dictionarySuffix = ".dict";

	private final static String dictionaryTableSuffix = "_dictionary"; 
	public final static String defaultDictionaryMapperFilePath = "org/fastcatsearch/db/mapper/DictionaryMapper.xml";
	
	protected Map<String,AbstractDictionaryDAO> daoMap;
	
	public AnalysisPlugin(File pluginDir, PluginSetting pluginSetting) {
		super(pluginDir, pluginSetting);
	}

	@Override
	protected void addMapperFile(List<File> mapperFileList){
		
		File mapperFile = null;
		try {
			mapperFile = Resources.getResourceAsFile(defaultDictionaryMapperFilePath);
			mapperFileList.add(mapperFile);
		} catch (IOException e) {
			logger.error("error load defaultDictionaryMapperFile", e);
		}
	}
	
	@Override
	protected void doLoad(){
		prepareDAO();
		loadDictionary();
	}
	
	@Override
	protected void doUnload(){
		daoMap.clear();
	}
	private void prepareDAO() {
		daoMap = new HashMap<String,AbstractDictionaryDAO>();
		
		AnalysisPluginSetting setting = (AnalysisPluginSetting) pluginSetting;
		List<DictionarySetting> list = setting.getDictionarySettingList();
		for(DictionarySetting dictionarySetting : list){
			String dictionaryId = dictionarySetting.getId();
			String type = dictionarySetting.getType();
			String tableName = getDictionaryTableName(dictionaryId);
			///type에 따라 set, map, custom을 확인하여 해당 DAO리턴.
			if(type.equals("set") || type.equals("synonym")){
				daoMap.put(dictionaryId, new SetDictionaryDAO(tableName, internalDBModule));
			}else if(type.equals("map")){
				daoMap.put(dictionaryId, new MapDictionaryDAO(tableName, internalDBModule));
				
			}else if(type.equals("custom")){
				String valueColumnList = dictionarySetting.getValueColumnList();
				String[] columnList = null;
				if(valueColumnList != null) {
					columnList = dictionarySetting.getValueColumnList().split(",");
				}
				daoMap.put(dictionaryId, new CustomDictionaryDAO(tableName, columnList, internalDBModule));
			}
		}
	}

	protected abstract void loadDictionary();

	public abstract Dictionary<?> getDictionary();

	public AbstractDictionaryDAO getDictionaryDAO(String dictionaryId) {
		return daoMap.get(dictionaryId);  
	}
	public Set<Entry<String, AbstractDictionaryDAO>> getDictionaryEntrySet(){
		return daoMap.entrySet();
	}
	
	public String getDictionaryTableName(String dictionaryId){
		return dictionaryId + dictionaryTableSuffix;
	}
	
	protected File getDictionaryFile(String dictionaryName) {
		return new File(new File(pluginDir, dictionaryPath), dictionaryName + dictionarySuffix);
	}
	
	@Override
	public AnalysisPluginSetting getPluginSetting(){
		return (AnalysisPluginSetting) pluginSetting;
	}

	public void compileDictionaryFromDAO(String dictionaryId) throws IOException {
		AnalysisPluginSetting setting = (AnalysisPluginSetting) pluginSetting;
		List<DictionarySetting> list = setting.getDictionarySettingList();
		for(DictionarySetting dictionarySetting : list){
			String id = dictionarySetting.getId();
			if(id.equals(dictionaryId)){
				String type = dictionarySetting.getType();
				boolean ignoreCase = dictionarySetting.isIgnoreCase();
				AbstractDictionaryDAO dictionaryDAO = daoMap.get(dictionaryId);
				///type에 따라 set, map, synonym, custom을 확인하여 compile 작업수행.
				File targetFile = getDictionaryFile(dictionaryId);
				SourceDictionary dictionaryType = null;
				if(type.equals("set")){
					dictionaryType = new SetDictionary(ignoreCase);
				}else if(type.equals("map")){
					dictionaryType = new MapDictionary(ignoreCase);
				}else if(type.equals("synonym")){
					dictionaryType = new SynonymDictionary(ignoreCase);
				}else if(type.equals("custom")){
					String valueColumnList = dictionarySetting.getValueColumnList();
					String[] columnList = null;
					if(valueColumnList != null) {
						columnList = dictionarySetting.getValueColumnList().split(",");
					}
					dictionaryType = new CustomDictionary(ignoreCase);
				}
				
				try {
					DAOSourceDictionaryCompiler.compile(targetFile, dictionaryDAO, dictionaryType);
				} catch (Exception e) {
					logger.error("dictionary compile error", e);
				}
				
				break;
			}
		}
	}
	
}
