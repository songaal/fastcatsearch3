package org.fastcatsearch.additional;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.bind.JAXBException;

import org.fastcatsearch.additional.KeywordDictionary.KeywordDictionaryType;
import org.fastcatsearch.additional.module.PopularKeywordModule;
import org.fastcatsearch.additional.module.RelateKeywordModule;
import org.fastcatsearch.db.AbstractDBService;
import org.fastcatsearch.db.mapper.ADKeywordMapper;
import org.fastcatsearch.db.mapper.KeywordSuggestionMapper;
import org.fastcatsearch.db.mapper.ManagedMapper;
import org.fastcatsearch.db.mapper.PopularKeywordMapper;
import org.fastcatsearch.db.mapper.RelateKeywordMapper;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.KeywordServiceSettings;
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.util.JAXBConfigs;
/**
 * 인기키워드 등의 키워드서비스를 제공한다. 
 * */
public class KeywordService extends AbstractDBService {

	private KeywordServiceSettings keywordServiceSettings;
	private Map<KeywordDictionaryType, KeywordDictionary> keywordDictionaryMap;
	private ReentrantReadWriteLock keywordDictionaryLock; //read일때는 lock없고, write일때만 lock걸린다.
	
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
		popularKeywordModule = new PopularKeywordModule(moduleHome, this, environment, settings);
		relateKeywordModule = new RelateKeywordModule(moduleHome, this, environment, settings);
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
		if(keywordServiceSettings == null){
			logger.error("Cannot load KeywordService setting file >> {}", keywordServiceSettings);
			return false;
		}
		
		boolean isServiceNode = keywordServiceSettings.getServiceNodeList().contains(environment.myNodeId());
		isMaster = environment.isMasterNode();
		///서비스 노드나 ,마스터 노드가 아니면 서비스를 시작하지 않는다.
		if(!isServiceNode && !isMaster){
			logger.info("This node does not provide KeywordService.");
			return false;
		}
		
		//키워드 서비스노드이면..
		logger.info("This node provides KeywordService. isMaster > {}", isMaster);
		
		keywordDictionaryLock = new ReentrantReadWriteLock();
		keywordDictionaryMap = new HashMap<KeywordDictionaryType, KeywordDictionary>();
			
		//모듈 로딩.
		loadKeywordModules();
		
		if (isMaster){
			//마스터서버이면, 자동완성, 연관키워드, 인기검색어 등의 db를 연다.
			return super.doStart();
		}else{
			
			return true;
		}
		
	}

	
	private void loadKeywordModules() {
		popularKeywordModule.load();
		relateKeywordModule.load();
	}
	
	private void unloadKeywordModules() {
		popularKeywordModule.unload();
		relateKeywordModule.unload();
	}

	
	@Override
	protected boolean doStop() throws FastcatSearchException {
		
		unloadKeywordModules();
		
		if (isMaster){
			return super.doStop();
		}else{
			
			
			return true;
		}
	}

	@Override
	protected boolean doClose() throws FastcatSearchException {
		if (isMaster){
			return super.doClose();
		}else{
			
			
			return true;
		}
	}


	
	public KeywordDictionary getKeywordDictionary(KeywordDictionaryType key){
		keywordDictionaryLock.readLock().lock();
		try{
			return keywordDictionaryMap.get(key);
		}finally {
			keywordDictionaryLock.readLock().unlock();
		}
	}
	
	public void putKeywordDictionary(KeywordDictionaryType key, KeywordDictionary value){
		keywordDictionaryLock.writeLock().lock();
		try{
			keywordDictionaryMap.put(key, value);
		}finally {
			keywordDictionaryLock.writeLock().unlock();
		}
	}
	
	public void removeKeywordDictionary(KeywordDictionaryType key){
		keywordDictionaryLock.writeLock().lock();
		try{
			keywordDictionaryMap.remove(key);
		}finally {
			keywordDictionaryLock.writeLock().unlock();
		}
	}
	
	@Override
	protected void initMapper(ManagedMapper managedMapper) throws Exception {
		//do nothing
	}

}
