package org.fastcatsearch.keyword;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.bind.JAXBException;

import org.fastcatsearch.db.AbstractDBService;
import org.fastcatsearch.db.mapper.ADKeywordMapper;
import org.fastcatsearch.db.mapper.KeywordSuggestionMapper;
import org.fastcatsearch.db.mapper.ManagedMapper;
import org.fastcatsearch.db.mapper.PopularKeywordMapper;
import org.fastcatsearch.db.mapper.RelateKeywordMapper;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.keyword.KeywordDictionary.KeywordDictionaryType;
import org.fastcatsearch.keyword.module.PopularKeywordModule;
import org.fastcatsearch.keyword.module.RelateKeywordModule;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.KeywordServiceSettings;
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.settings.StaticticsSettings.Category;
import org.fastcatsearch.util.JAXBConfigs;

/**
 * 인기키워드 등의 키워드서비스를 제공한다.
 * */
public class KeywordService extends AbstractDBService {

	private KeywordServiceSettings keywordServiceSettings;

	private boolean isMaster;

	private PopularKeywordModule popularKeywordModule;
	private RelateKeywordModule relateKeywordModule;

	private File moduleHome;
	
	private static Class<?>[] mapperList = new Class<?>[]{
			PopularKeywordMapper.class
			,RelateKeywordMapper.class
			,KeywordSuggestionMapper.class
			,ADKeywordMapper.class
	};
	
	
	public KeywordService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super("db/keyword", KeywordService.mapperList, environment, settings, serviceManager);
		
		moduleHome = environment.filePaths().file("keyword");
		popularKeywordModule = new PopularKeywordModule(moduleHome, environment, settings);
		relateKeywordModule = new RelateKeywordModule(moduleHome, environment, settings);
	}
	
	public File getFile(String moduleName, String fileName) {
		File home = new File(moduleHome, moduleName);
		return new File(home,fileName);
	}

	@Override
	protected boolean doStart() throws FastcatSearchException {

		File keywordServiceConfigFile = environment.filePaths().configPath().path(SettingFileNames.keywordServiceConfig).file();
		try {
			keywordServiceSettings = JAXBConfigs.readConfig(keywordServiceConfigFile, KeywordServiceSettings.class);
		} catch (JAXBException e) {
			logger.error("KeywordService setting file read error.", e);
			return false;
		}
		if (keywordServiceSettings == null) {
			logger.error("Cannot load KeywordService setting file >> {}", keywordServiceSettings);
			return false;
		}

		boolean isServiceNode = keywordServiceSettings.getServiceNodeList().contains(environment.myNodeId());
		isMaster = environment.isMasterNode();
		// /서비스 노드나 ,마스터 노드가 아니면 서비스를 시작하지 않는다.
		if (!isServiceNode && !isMaster) {
			logger.info("This node does not provide KeywordService.");
			return false;
		}

		// 키워드 서비스노드이면..
		logger.info("This node provides KeywordService. isMaster > {}", isMaster);

		// 모듈 로딩.
		loadKeywordModules();

		if (isMaster) {
			// 마스터서버이면, 자동완성, 연관키워드, 인기검색어 등의 db를 연다.
			return super.doStart();
		} else {

			return true;
		}

	}

	private void loadKeywordModules() {
		List<Category> categoryList = keywordServiceSettings.getCategoryList();
		popularKeywordModule.setCategoryList(categoryList);
		popularKeywordModule.load();
		relateKeywordModule.setCategoryList(categoryList);
		relateKeywordModule.load();
	}

	private void unloadKeywordModules() {
		popularKeywordModule.unload();
		relateKeywordModule.unload();
	}

	@Override
	protected boolean doStop() throws FastcatSearchException {

		unloadKeywordModules();

		if (isMaster) {
			return super.doStop();
		} else {

			return true;
		}
	}

	@Override
	protected boolean doClose() throws FastcatSearchException {
		if (isMaster) {
			return super.doClose();
		} else {

			return true;
		}
	}

	public KeywordDictionary getKeywordDictionary(String categoryId, KeywordDictionaryType key) {
		if(key == KeywordDictionaryType.POPULAR_KEYWORD_DAY || key == KeywordDictionaryType.POPULAR_KEYWORD_REALTIME || key == KeywordDictionaryType.POPULAR_KEYWORD_WEEK){
			return popularKeywordModule.getKeywordDictionary(categoryId, key); 
		}else if(key == KeywordDictionaryType.RELATE_KEYWORD){
			return relateKeywordModule.getKeywordDictionary(categoryId);
		}else{
			//TODO ad keyword, keyword suggestion
		}
		return null;
	}

	public KeywordDictionary getKeywordDictionary(KeywordDictionaryType key){
		return getKeywordDictionary(null, key);
	}

	@Override
	protected void initMapper(ManagedMapper managedMapper) throws Exception {
		// do nothing
	}

}
