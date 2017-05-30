package org.fastcatsearch.plugin.analysis;

import org.apache.ibatis.io.Resources;
import org.fastcatsearch.db.dao.DictionaryDAO;
import org.fastcatsearch.db.dao.DictionaryStatusDAO;
import org.fastcatsearch.db.dao.SynonymDictionaryDAO;
import org.fastcatsearch.db.mapper.DictionaryStatusMapper;
import org.fastcatsearch.db.vo.DictionaryStatusVO;
import org.fastcatsearch.ir.analysis.AnalyzerFactory;
import org.fastcatsearch.ir.analysis.AnalyzerPool;
import org.fastcatsearch.ir.analysis.AnalyzerPoolManager;
import org.fastcatsearch.ir.dic.CommonDictionary;
import org.fastcatsearch.ir.dic.Dictionary;
import org.fastcatsearch.ir.dic.PreResult;
import org.fastcatsearch.ir.dictionary.*;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.plugin.LicenseInvalidException;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginSetting;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.Analyzer;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.ColumnSetting;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.DictionarySetting;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.DictionarySetting.Type;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

public abstract class AnalysisPlugin<T, P> extends Plugin {

	protected static String dictionaryPath = "dict/";
	protected static String dictionarySuffix = ".dict";

	private final static String dictionaryTableSuffix = "_dictionary";
	public final static String defaultDictionaryMapperFilePath = "org/fastcatsearch/db/mapper/DictionaryMapper.xml";
	public final static String defaultDictionaryStatusMapperFilePath = "org/fastcatsearch/db/mapper/DictionaryStatusMapper.xml";

	protected Map<String, DictionaryDAO> daoMap;
	protected DictionaryStatusDAO dictionaryStatusDAO;
	protected DictionaryStatusMapper dictionaryStatusMapper;
	
	protected CommonDictionary<T, P> commonDictionary;
	protected Map<String, AnalyzerInfo> analyzerFactoryMap;
	
	protected AnalyzerPoolManager analyzerPoolManager;
	
	public AnalysisPlugin(File pluginDir, PluginSetting pluginSetting, String serverId) {
		super(pluginDir, pluginSetting, serverId);
		analyzerPoolManager = new AnalyzerPoolManager();
	}

	@Override
	protected void addMapperFile(List<URL> mapperFileList) {

		try {
			URL mapperFile = Resources.getResourceURL(defaultDictionaryMapperFilePath);
			mapperFileList.add(mapperFile);
		} catch (IOException e) {
			logger.error("error load defaultDictionaryMapperFile", e);
		}
		try {
			URL statusMapperURL = Resources.getResourceURL(defaultDictionaryStatusMapperFilePath);
			mapperFileList.add(statusMapperURL);
		} catch (IOException e) {
			logger.error("error load defaultDictionaryStatusMapperFile", e);
		}
	}

	@Override
	protected void doLoad(boolean isMasterNode) throws LicenseInvalidException {
		if(isMasterNode){
			prepareDAO();
		}
		commonDictionary = loadDictionary();
		loadAnalyzerFactory();
	}

	@Override
	protected void doUnload() {
		if(daoMap != null){
			daoMap.clear();
		}
		if(analyzerFactoryMap != null){
			analyzerFactoryMap.clear();
		}
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
					logger.error("error update dictionary status.", ignore);
				}
				
				List<ColumnSetting> columnSettingList = dictionarySetting.getColumnSettingList();
				if (columnSettingList != null) {
					DictionaryDAO dao = null;
					if(type == Type.SYNONYM || type == Type.SYNONYM_2WAY){
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

	protected abstract Dictionary<T, P> loadSystemDictionary(DictionarySetting dictionarySetting);
		
	protected CommonDictionary<T, P> loadDictionary(){
		AnalysisPluginSetting setting = (AnalysisPluginSetting) pluginSetting;
		List<DictionarySetting> list = setting.getDictionarySettingList();
		
		Dictionary<T, P> dictionary = null;
		CommonDictionary<T, P> commonDictionary = null;
		
		if (list != null) {
			for (DictionarySetting dictionarySetting : list) {
				Type type = dictionarySetting.getType();
				if(type == Type.SYSTEM){
					dictionary = loadSystemDictionary(dictionarySetting);
					commonDictionary = new CommonDictionary<T, P>(dictionary);
					break;
				}
			}
			
			for (DictionarySetting dictionarySetting : list) {
				String dictionaryId = dictionarySetting.getId();
				Type type = dictionarySetting.getType();
				String tokenType = dictionarySetting.getTokenType();
				File dictFile = getDictionaryFile(dictionaryId);
				SourceDictionary sourceDictionary = null;
				boolean isIgnoreCase = dictionarySetting.isIgnoreCase();
				
				if(type == Type.SET){
					SetDictionary setDictionary = new SetDictionary(dictFile, isIgnoreCase);
					if(tokenType != null){
						dictionary.appendAdditionalNounEntry(setDictionary.set(), tokenType);
					}
					sourceDictionary = setDictionary;
				}else if(type == Type.MAP){
					MapDictionary mapDictionary = new MapDictionary(dictFile, isIgnoreCase);
					if(tokenType != null){
						dictionary.appendAdditionalNounEntry(mapDictionary.map().keySet(), tokenType);
					}
					sourceDictionary = mapDictionary;
				}else if(type == Type.SYNONYM || type == Type.SYNONYM_2WAY){
					SynonymDictionary synonymDictionary = new SynonymDictionary(dictFile, isIgnoreCase);
					if(tokenType != null){
//						logger.debug("synonym word set > {}", synonymDictionary.getWordSet());
						dictionary.appendAdditionalNounEntry(synonymDictionary.getWordSet(), tokenType);
					}
					sourceDictionary = synonymDictionary;
				}else if(type == Type.SPACE){
					SpaceDictionary spaceDictionary = new SpaceDictionary(dictFile, isIgnoreCase);
					if(tokenType != null){
//						logger.debug("SPACE > {}", spaceDictionary.getWordSet());
						dictionary.appendAdditionalNounEntry(spaceDictionary.getWordSet(), tokenType);
					}
					sourceDictionary = spaceDictionary;
					Map map = new HashMap<CharVector, PreResult<CharVector>>();
					for(Entry<CharVector, CharVector[]> e : spaceDictionary.map().entrySet()){
						PreResult preResult = new PreResult<T>();
						preResult.setResult(e.getValue());
						map.put(e.getKey(), preResult);
						
//						logger.debug("PreResult {} > {}", e.getKey(), e.getValue());
					}
					commonDictionary.setPreDictionary(map);
				}else if(type == Type.CUSTOM){
					CustomDictionary customDictionary = new CustomDictionary(dictFile, isIgnoreCase);
					if(tokenType != null){
						dictionary.appendAdditionalNounEntry(customDictionary.getWordSet(), tokenType);
					}
					sourceDictionary = customDictionary;

                }else if(type == Type.INVERT_MAP){
                    InvertMapDictionary invertMapDictionary = new InvertMapDictionary(dictFile, isIgnoreCase);
                    if(tokenType != null){
                        dictionary.appendAdditionalNounEntry(invertMapDictionary.map().keySet(), tokenType);
                    }
                    sourceDictionary = invertMapDictionary;
				}else if(type == Type.COMPOUND){
					CompoundDictionary compoundDictionary = new CompoundDictionary(dictFile, isIgnoreCase);
					if(tokenType != null){
						dictionary.appendAdditionalNounEntry(compoundDictionary.map().keySet(), tokenType);
					}
					sourceDictionary = compoundDictionary;
				}else if(type == Type.SYSTEM){
					//ignore
				}else{
					logger.error("Unknown Dictionary type > {}", type);
				}
				logger.info("Dictionary {} is loaded. tokenType[{}] isIgnoreCase[{}]", dictionaryId, tokenType, isIgnoreCase);
				///add dictionary
				if(sourceDictionary != null){
					commonDictionary.addDictionary(dictionaryId, sourceDictionary);
				}
				
			}
		}
		
		return commonDictionary;
	}
	
	public void reloadDictionary(){
		long st = System.nanoTime();
		CommonDictionary<T, P> newCommonDictionary = loadDictionary();
		
		//1. commonDictionary에 systemdictinary셋팅.
		commonDictionary.reset(newCommonDictionary);
		//2. dictionaryMap 에 셋팅.
		Map<String, Object> dictionaryMap = commonDictionary.getDictionaryMap();
		for(Entry<String, Object> entry : dictionaryMap.entrySet()){
			String dictionaryId = entry.getKey();
			Object dictionary = entry.getValue();
			//dictionary 객체 자체는 유지하고, 내부 실데이터(map,set등)만 업데이트해준다.
			//상속시 instanceof로는 정확한 클래스가 판별이 불가능하므로 isAssignableFrom 로 판별한다.
			if(dictionary.getClass().isAssignableFrom(SetDictionary.class)){
				SetDictionary setDictionary = (SetDictionary) dictionary;
				SetDictionary newDictionary = (SetDictionary) newCommonDictionary.getDictionary(dictionaryId);
				setDictionary.setSet(newDictionary.set());
			}else if(dictionary.getClass().isAssignableFrom(MapDictionary.class)){
				MapDictionary mapDictionary = (MapDictionary) dictionary;
				MapDictionary newDictionary = (MapDictionary) newCommonDictionary.getDictionary(dictionaryId);
				mapDictionary.setMap(newDictionary.map());
			}else if(dictionary.getClass().isAssignableFrom(SynonymDictionary.class)){
				SynonymDictionary synonymDictionary = (SynonymDictionary) dictionary;
				SynonymDictionary newDictionary = (SynonymDictionary) newCommonDictionary.getDictionary(dictionaryId);
				synonymDictionary.setMap(newDictionary.map());
				synonymDictionary.setWordSet(newDictionary.getWordSet());
			}else if(dictionary.getClass().isAssignableFrom(SpaceDictionary.class)){
				SpaceDictionary spaceDictionary = (SpaceDictionary) dictionary;
				SpaceDictionary newDictionary = (SpaceDictionary) newCommonDictionary.getDictionary(dictionaryId);
				spaceDictionary.setMap(newDictionary.map());
				spaceDictionary.setWordSet(newDictionary.getWordSet());
			}else if(dictionary.getClass().isAssignableFrom(CustomDictionary.class)){
				CustomDictionary customDictionary = (CustomDictionary) dictionary;
				CustomDictionary newDictionary = (CustomDictionary) newCommonDictionary.getDictionary(dictionaryId);
				customDictionary.setMap(newDictionary.map());
				customDictionary.setWordSet(newDictionary.getWordSet());
			}else if(dictionary.getClass().isAssignableFrom(CompoundDictionary.class)){
				CompoundDictionary compoundDictionary = (CompoundDictionary) dictionary;
				CompoundDictionary newDictionary = (CompoundDictionary) newCommonDictionary.getDictionary(dictionaryId);
				compoundDictionary.setMap(newDictionary.map());
			}
			logger.info("Dictionary {} is updated!", dictionaryId);
			
		}
		newCommonDictionary = null;
		logger.debug("{} Dictionary Reload Done. {}ms", pluginId, (System.nanoTime() - st) / 1000000);
	}
	
	private void loadAnalyzerFactory(){
		analyzerFactoryMap = new HashMap<String, AnalyzerInfo>();
		loadAnalyzerFactory(analyzerFactoryMap);
		
		///로딩된 analyzer list를 동적으로 setting에 넣어준다. 
		AnalysisPluginSetting setting = (AnalysisPluginSetting) pluginSetting;
		List<Analyzer> analyzerList = new ArrayList<Analyzer>();
		setting.setAnalyzerList(analyzerList);
		
		for(Entry<String, AnalyzerInfo> entry : analyzerFactoryMap.entrySet()){
			String id = entry.getKey();
			String name = entry.getValue().name();
			AnalyzerFactory factory = entry.getValue().factory();
			Class clazz = factory.getAnalyzerClass();
			Analyzer analyzer = new Analyzer(id, name, clazz.getName());
			analyzerList.add(analyzer);
		}
	}
	
	protected abstract void loadAnalyzerFactory(Map<String, AnalyzerInfo> analyzerFactoryMap);
	
	public Map<String, AnalyzerInfo> analyzerFactoryMap(){
		return analyzerFactoryMap;
	}
	
	public CommonDictionary<T, P> getDictionary(){
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
	public File getDictionaryDirectory(){
		return new File(pluginDir, dictionaryPath);
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
						boolean isIgnoreCase = dictionarySetting.isIgnoreCase();
						// /type에 따라 set, map, synonym, custom을 확인하여 compile 작업수행.
						File targetFile = getDictionaryFile(dictionaryId);
						SourceDictionary dictionaryType = null;
						if (type == Type.SET) {
							dictionaryType = new SetDictionary(isIgnoreCase);
						} else if (type == Type.MAP) {
							dictionaryType = new MapDictionary(isIgnoreCase);
						} else if (type == Type.SYNONYM) {
							dictionaryType = new SynonymDictionary(isIgnoreCase);
						} else if (type == Type.SYNONYM_2WAY) {
							dictionaryType = new SynonymDictionary(isIgnoreCase);
						} else if (type == Type.SPACE) {
							dictionaryType = new SpaceDictionary(isIgnoreCase);
						} else if (type == Type.CUSTOM) {
							dictionaryType = new CustomDictionary(isIgnoreCase);
						} else if (type == Type.INVERT_MAP) {
                            dictionaryType = new InvertMapDictionary(isIgnoreCase);
                        } else if (type == Type.COMPOUND) {
							dictionaryType = new CompoundDictionary(isIgnoreCase);
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
	
	protected void registerAnalyzer(Map<String, AnalyzerInfo> analyzerFactoryMap, String key, String description, AnalyzerFactory analyzerFactory){
		analyzerFactoryMap.put(key, new AnalyzerInfo(description, analyzerFactory));
		//기본으로 plugin에서 가지고 있는 analyzer. max는 2이다.
		analyzerPoolManager.registerAnalyzer(key.toUpperCase(), analyzerFactory, 0, 2);
	}
	
	public AnalyzerPool getAnalyzerPool(String analyzerId){
		return analyzerPoolManager.getPool(analyzerId);
	}

}
