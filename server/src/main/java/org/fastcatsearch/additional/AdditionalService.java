package org.fastcatsearch.additional;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.AdditionalServiceSettings;
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.util.JAXBConfigs;

public class AdditionalService extends AbstractService {

	private AdditionalServiceSettings additionalServiceSettings;
	public AdditionalService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
		
		
	}

	@Override
	protected boolean doStart() throws FastcatSearchException {
		
		File additionalServiceConfigFile = environment.filePaths().configPath().path(SettingFileNames.additionalServiceConfig).file();
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
		
		//TODO 
		//마스터서버이면, 자동완성, 연관키워드, 인기검색어 등의 db를 연다.
		
		
		return true;
	}

	@Override
	protected boolean doStop() throws FastcatSearchException {
		return true;
	}

	@Override
	protected boolean doClose() throws FastcatSearchException {
		return true;
	}

}
