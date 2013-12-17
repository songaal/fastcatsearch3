package org.fastcatsearch.additional;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.ibatis.io.Resources;
import org.fastcatsearch.db.AbstractDBService;
import org.fastcatsearch.db.InternalDBModule;
import org.fastcatsearch.db.mapper.ADKeywordMapper;
import org.fastcatsearch.db.mapper.KeywordSuggestionMapper;
import org.fastcatsearch.db.mapper.ManagedMapper;
import org.fastcatsearch.db.mapper.PopularKeywordMapper;
import org.fastcatsearch.db.mapper.RelateKeywordMapper;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.AdditionalServiceSettings;
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.util.JAXBConfigs;
/**
 * 인기키워드 등의 키워드서비스를 제공한다. 
 * */
public class KeywordService extends AbstractDBService {

	private AdditionalServiceSettings additionalServiceSettings;
	
	private static Class<?>[] mapperList = new Class<?>[]{
			PopularKeywordMapper.class
			,RelateKeywordMapper.class
			,KeywordSuggestionMapper.class
			,ADKeywordMapper.class
	};
	
	private boolean isMaster;
	
	public KeywordService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super("db/keyword", mapperList, environment, settings, serviceManager);
		
		if(environment.isMasterNode()){
			isMaster = true;
			String dbPath = environment.filePaths().file("db/keyword").getAbsolutePath();
			//system관련 mapper설정.
			List<URL> mapperFileList = new ArrayList<URL>();
			for(Class<?> mapperDAO : mapperList){
				try {
					String mapperFilePath = mapperDAO.getName().replace('.', '/') +".xml";
					URL mapperFile = Resources.getResourceURL(mapperFilePath);
					mapperFileList.add(mapperFile);
				} catch (IOException e) {
					logger.error("error load defaultDictionaryMapperFile", e);
				}
			}
			internalDBModule = new InternalDBModule(dbPath, mapperFileList, environment, settings, serviceManager);
		}
		
	}

	@Override
	protected boolean doStart() throws FastcatSearchException {
		
		File additionalServiceConfigFile = environment.filePaths().configPath().path(SettingFileNames.keywordServiceConfig).file();
		try {
			additionalServiceSettings = JAXBConfigs.readConfig(additionalServiceConfigFile, AdditionalServiceSettings.class);
		} catch (JAXBException e) {
			logger.error("additionalService setting file read error.", e);
			return false;
		}
		if(additionalServiceSettings == null){
			logger.error("Cannot load additionalService setting file >> {}", additionalServiceSettings);
			return false;
		}
		
		boolean found = false;
		for(String serviceNodeId : additionalServiceSettings.getServiceNodeList()){
			if(environment.myNodeId().equals(serviceNodeId)){
				logger.info("This node provides AdditionalService.");
				found = true;
				break;
			}
		}
		
		if(!found){
			logger.info("This node does not provide AdditionalService.");
			return false;
		}
		
		
		if (isMaster){
			//TODO 
			//마스터서버이면, 자동완성, 연관키워드, 인기검색어 등의 db를 연다.
			return super.doStart();
		}else{
			
			
			
			return true;
		}
		
		
		
		
	}

	@Override
	protected boolean doStop() throws FastcatSearchException {
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

	@Override
	protected void initMapper(ManagedMapper managedMapper) throws Exception {
		//do nothing
	}

}
