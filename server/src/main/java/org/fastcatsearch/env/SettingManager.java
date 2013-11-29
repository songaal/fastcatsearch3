package org.fastcatsearch.env;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import org.fastcatsearch.settings.NodeListSettings;
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.util.JAXBConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 엔진내 conf/하위 셋팅들을 가지고 있다.
 * system.properties : 시스템 설정. 각 엔진마다 커스터마이징할 수 있고, 고급설정이므로 관리도구에서는 편집기능을 제공하지 않는다.
 * id.properties : 엔진이 시작시 사용되는 설정 값들. 각 서버마다 수정을 한뒤 엔진을 시작해야한다. 
 * node-list.xml : 분산노드들의 정보를 가지고 있다. admin노드에서 관리가 되며, 타 노드들은 start시에 설정파일을 사용하며, 차후 admin으로 부터 최신 node-list를 전달받게된다.
 * */
public class SettingManager {
	private final Logger logger = LoggerFactory.getLogger(SettingManager.class);
	private Environment environment;
	private static Map<String, Object> settingCache = new HashMap<String, Object>();

	public SettingManager(Environment environment) {
		this.environment = environment;
	}

	public String getConfigFilepath(String filename) {
		return environment.filePaths().configPath().path(filename).toString();
	}


	private Properties getProperties(String configFilepath) {
		
		logger.debug("Read properties = {}", configFilepath);
		Properties result = new Properties();
		InputStream is = null;
		try {
			is = new FileInputStream(configFilepath);
			result.load(is);
			return result;
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				if(is != null){
					is.close();
				}
			} catch (IOException ignore) {
			}
		}
		return null;
	}

	private boolean storeProperties(Properties properties, String filename) {
		String configFilepath = getConfigFilepath(filename);
		logger.debug("Store properties = {}", configFilepath);
		
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(configFilepath);
			properties.store(os, new Date().toString());
			settingCache.put(configFilepath, properties);
			return true;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				if(os != null){
					os.close();
				}
			} catch (IOException ignore) {
			}

		}
		
		return false;
	}
	
	public Settings getIdSettings() {
		return getSettings(SettingFileNames.idProperties);
	}
	public Settings getSystemSettings() {
		return getSettings(SettingFileNames.systemProperties);
	}
	public boolean storeIdSettings(Settings settings) {
		return storeProperties(settings.properties(), SettingFileNames.idProperties);
	}
	public boolean storeSystemSettings(Settings settings) {
		return storeProperties(settings.properties(), SettingFileNames.systemProperties);
	}
	
	private Settings getSettings(String configFilename) {
		String configFilepath = getConfigFilepath(configFilename);
		Object obj = settingCache.get(configFilepath);
		if(obj != null){
			return (Settings) obj;
		}
		
		Properties properties = getProperties(configFilepath);
		Settings settings = new Settings(properties);
		settingCache.put(configFilepath, settings);
		return settings;
	}

	public NodeListSettings getNodeListSettings() {
		String configFilepath = getConfigFilepath(SettingFileNames.nodeListSettings);
		Object obj = settingCache.get(configFilepath);
		if(obj != null){
			return (NodeListSettings) obj;
		}
		
		File file = new File(configFilepath);
		if(file.exists()){
			NodeListSettings settings = null;
			try {
				settings = JAXBConfigs.readConfig(file, NodeListSettings.class);
				settingCache.put(configFilepath, settings);
				return settings;
			} catch (JAXBException e) {
				logger.error("", e);
			}
		}
		
		return null;
	}

	public boolean storeNodeListSettings(NodeListSettings nodeListSettings) {
		String configFilepath = getConfigFilepath(SettingFileNames.nodeListSettings);
		File file = new File(configFilepath);
		try {
			JAXBConfigs.writeConfig(file, nodeListSettings, NodeListSettings.class);
			settingCache.put(configFilepath, nodeListSettings);
			return true;
		} catch (JAXBException e) {
			logger.error("", e);
			return false;
		}
	}
}
