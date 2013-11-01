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
import org.fastcatsearch.db.dao.DictionaryStatusDAO;
import org.fastcatsearch.db.dao.SynonymDictionaryDAO;
import org.fastcatsearch.db.mapper.DictionaryStatusMapper;
import org.fastcatsearch.db.vo.DictionaryStatusVO;
import org.fastcatsearch.ir.dic.CommonDictionary;
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
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.DictionarySetting.Type;

public abstract class AnalysisPlugin<T> extends Plugin {

	public static String DICT_SYNONYM = "synonym";
	public static String DICT_STOP = "stop";
	public static String DICT_SYSTEM = "system";
	
	protected static String dictionaryPath = "dict/";
	protected static String dictionarySuffix = ".dict";

	private final static String dictionaryTableSuffix = "_dictionary";
	public final static String defaultDictionaryMapperFilePath = "org/fastcatsearch/db/mapper/DictionaryMapper.xml";
	public final static String defaultDictionaryStatusMapperFilePath = "org/fastcatsearch/db/mapper/DictionaryStatusMapper.xml";

	protected Map<String, DictionaryDAO> daoMap;
	protected DictionaryStatusDAO dictionaryStatusDAO;
	protected DictionaryStatusMapper dictionaryStatusMapper;
	
	protected static CommonDictionary commonDictionary;
	
	public AnalysisPlugin(File pluginDir, PluginSetting pluginSetting) {
		super(pluginDir, pluginSetting);
	}

	@Override
	protected void addMapperFile(List<File> mapperFileList) {

		try {
			File mapperFile = Resources.getResourceAsFile(defaultDictionaryMapperFilePath);
			mapperFileList.add(mapperFile);
		} catch (IOException e) {
			logger.error("error load defaultDictionaryMapperFile", e);
		}
		try {
			File statusMapperFile = Resources.getResourceAsFile(defaultDictionaryStatusMapperFilePath);
			mapperFileList.add(statusMapperFile);
		} catch (IOException e) {
			logger.error("error load defaultDictionaryStatusMapperFile", e);
		}
	}

	@Override
	protected void doLoad() {
		prepareDAO();
		commonDictionary = loadDictionary();
	}

	@Override
	protected void doUnload() {
		daoMap.clear();
	}
	
	public DictionaryStatusDAO dictionaryStatusDAO(){
		return dictionaryStatusDAO;
	}
	
	private void prepareDAO() {
		
		dictionaryStatusDAO = new DictionaryStatusDAO(internalDBModule);
		//사전 상태관리 테이블.
		if(!dictionaryStatusDAO.validateTable()){
			dictionaryStatusDAO.dropTable();
			dictionaryStatusDAO.creatTable();
		}

		daoMap = new HashMap<String, DictionaryDAO>();

		AnalysisPluginSetting setting = (AnalysisPluginSetting) pluginSetting;
		List<DictionarySetting> list = setting.getDictionarySettingList();
		if (list != null) {
			for (DictionarySetting dictionarySetting : list) {
				String dictionaryId = dictionarySetting.getId();
				Type type = dictionarySetting.getType();
				String tableName = getDictionaryTableName(dictionaryId);

				try{
					//사전별 상태 row 초기화.
					DictionaryStatusVO dictionaryStatusVO = dictionaryStatusDAO.getEntry(dictionaryId);
					if(dictionaryStatusVO == null){
						dictionaryStatusDAO.putEntry(new DictionaryStatusVO(dictionaryId));
					}
				}catch(Exception ignore){
				}
				
				List<ColumnSetting> columnSettingList = dictionarySetting.getColumnSettingList();
				if (columnSettingList != null) {
					DictionaryDAO dao = null;
					if(type == Type.SYNONYM){
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

	protected abstract Dictionary<T> loadSystemDictionary();
		
	protected CommonDictionary<T> loadDictionary(){
		Dictionary<T> dictionary = loadSystemDictionary();
		CommonDictionary<T> commonDictionary = new CommonDictionary<T>(dictionary);
		
		AnalysisPluginSetting setting = (AnalysisPluginSetting) pluginSetting;
		List<DictionarySetting> list = setting.getDictionarySettingList();
		
		if (list != null) {
			for (DictionarySetting dictionarySetting : list) {
				String id = dictionarySetting.getId();
				Type type = dictionarySetting.getType();
				String tokenType = dictionarySetting.getTokenType();
				File dictFile = getDictionaryFile(id);
				SourceDictionary sourceDictionary = null;
				
				if(type == Type.SET){
					SetDictionary setDictionary = new SetDictionary(dictFile);
					if(tokenType != null){
						dictionary.appendAdditionalNounEntry(setDictionary.getSet(), tokenType);
					}
					sourceDictionary = setDictionary;
				}else if(type == Type.MAP){
					MapDictionary mapDictionary = new MapDictionary(dictFile);
					if(tokenType != null){
						dictionary.appendAdditionalNounEntry(mapDictionary.getMap().keySet(), tokenType);
					}
					sourceDictionary = mapDictionary;
				}else if(type == Type.SYNONYM){
					SynonymDictionary synonymDictionary = new SynonymDictionary(dictFile);
					if(tokenType != null){
						dictionary.appendAdditionalNounEntry(synonymDictionary.getWordSet(), tokenType);
					}
					sourceDictionary = synonymDictionary;
				}else if(type == Type.CUSTOM){
					
				}
				
				///add dictionary
				if(sourceDictionary != null){
					commonDictionary.addDictionary(id, sourceDictionary);
				}
				
			}
		}
		
		return commonDictionary;
	}
	
	public void reloadDictionary(){
		long st = System.nanoTime();
		CommonDictionary<T> oldDictionary = this.commonDictionary;
		CommonDictionary<T> commonDictionary = loadDictionary();
		this.commonDictionary = commonDictionary;
		logger.debug("{} Dictionary Reload Done. {}ms", pluginId, (System.nanoTime() - st) / 1000000);
	}
	
	public CommonDictionary<T> getDictionary(){
		return commonDictionary;
	}

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

	public int compileDictionaryFromDAO(String dictionaryId) throws IOException {
		AnalysisPluginSetting setting = (AnalysisPluginSetting) pluginSetting;
		List<DictionarySetting> list = setting.getDictionarySettingList();
		boolean isSuccess = false;
		int count = 0;
		if (list != null) {
			for (DictionarySetting dictionarySetting : list) {
				String id = dictionarySetting.getId();

				List<ColumnSetting> columnSettingList = dictionarySetting.getColumnSettingList();
				if (columnSettingList != null) {

					if (id.equals(dictionaryId)) {
						Type type = dictionarySetting.getType();
						DictionaryDAO dictionaryDAO = daoMap.get(dictionaryId);
						// /type에 따라 set, map, synonym, custom을 확인하여 compile 작업수행.
						File targetFile = getDictionaryFile(dictionaryId);
						SourceDictionary dictionaryType = null;
						if (type == Type.SET) {
							dictionaryType = new SetDictionary();
						} else if (type == Type.MAP) {
							dictionaryType = new MapDictionary();
						} else if (type == Type.SYNONYM) {
							dictionaryType = new SynonymDictionary();
						} else if (type == Type.CUSTOM) {
							dictionaryType = new CustomDictionary();
						}

						try {
							count = DAOSourceDictionaryCompiler.compile(targetFile, dictionaryDAO, dictionaryType, columnSettingList);
							isSuccess = true;
						} catch (Exception e) {
							logger.error("dictionary compile error", e);
							throw new IOException(e);
						}
						
						break;
					}
				}
			}
		}
		
		if(!isSuccess){
			throw new IOException("Dictionary not found error. name = " + dictionaryId);
		}
		
		return count;
	}

}
