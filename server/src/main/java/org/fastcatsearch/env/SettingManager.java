package org.fastcatsearch.env;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.settings.Settings;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class SettingManager {
	private final Logger logger = LoggerFactory.getLogger(SettingManager.class);
	private Environment environment;
	private static Map<String, Object> settingCache = new HashMap<String, Object>();

	public SettingManager(Environment environment) {
		this.environment = environment;
	}

	public String getSimpleDatetime() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}

	private Object getFromCache(String settingName) {
		String key = environment.filePaths().configPath().path(settingName).toString();
		return settingCache.get(key);
	}

	private Object getFromCache(String collection, String settingName) {
		String key = getKey(collection, settingName);
		return settingCache.get(key);
	}

	private Object putToCache(Object setting, String settingName) {
		String key = environment.filePaths().configPath().path(settingName).toString();
		settingCache.put(key, setting);
		return setting;
	}

	private Object putToCache(String collection, Object setting, String settingName) {
		String key = getKey(collection, settingName);
		settingCache.put(key, setting);
		return setting;
	}

	public String getKey(String collection, String filename) {
		return environment.filePaths().path("collection", collection, filename).toString();
	}

	public String getKey(String filename) {
		return environment.filePaths().configPath().path(filename).toString();
	}

	public Element getXml(String collection, String filename) {
		String configFile = getKey(collection, filename);
		logger.debug("Read xml = {}", configFile);
		Document doc = null;
		try {
			File f = new File(configFile);
			if (!f.exists()) {
				return null;
			}

			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(f);
			Element e = doc.getRootElement();
			putToCache(collection, e, filename);
			return e;
		} catch (JDOMException e) {
			logger.error(e.getMessage(), e);
		} catch (NullPointerException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private Properties getXmlProperties(String collection, String filename) {
		String configFile = getKey(collection, filename);
		logger.debug("Read properties = {}", configFile);
		Properties result = new Properties();
		try {
			result.loadFromXML(new FileInputStream(configFile));
			putToCache(collection, result, filename);
			return result;
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private void storeXmlProperties(String collection, Properties props, String filename) {
		String configFile = getKey(collection, filename);
		logger.debug("Store properties = {}", configFile);
		FileOutputStream writer = null;
		try {
			writer = new FileOutputStream(configFile);
			props.storeToXML(writer, new Date().toString());
			putToCache(collection, props, filename);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				// ignore
			}

		}
	}

	public Settings getIdSettings() {
		return getSettings(SettingFileNames.idConfig);
	}

	public Settings getServerSettings() {
		return getSettings(SettingFileNames.serverConfig);
	}

	public Settings getSettings(String yamlSettingName) {
		// load config file
		Settings serverSettings = null;
		synchronized (yamlSettingName) {
			Object obj = getFromCache(yamlSettingName);
			if (obj != null) {
				return (Settings) obj;
			}

			serverSettings = loadSetting(yamlSettingName);
			putToCache(serverSettings, yamlSettingName);
		}

		return serverSettings;
	}

	private void loadAndMerge(String string, Settings targetSettings) {
		Settings settings = loadSetting(string);
		if (settings != null) {
			targetSettings.overrideSettings(settings);
		}

	}

	private Settings loadSetting(String yamlSettingName) {
		Settings serverSettings = null;
		File configFile = environment.filePaths().configPath().path(yamlSettingName).file();
		InputStream input = null;
		try {
			Yaml yaml = new Yaml();
			input = new FileInputStream(configFile);
			Map<String, Object> data = (Map<String, Object>) yaml.load(input);
			serverSettings = new Settings(data);

			Object includeObj = data.get("include");
			if (includeObj != null) {
				if (includeObj instanceof List) {
					List<String> settingNameList = (List<String>) includeObj;
					for (int i = 0; i < settingNameList.size(); i++) {
						loadAndMerge(settingNameList.get(i), serverSettings);
					}
				} else {
					String settingName = (String) includeObj;
					loadAndMerge(settingName, serverSettings);
				}
			}
			putToCache(serverSettings, yamlSettingName);
		} catch (FileNotFoundException e) {
			logger.error("설정파일을 찾을수 없습니다. file = {}", configFile.getAbsolutePath());
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException ignore) {
				}
			}
		}

		return serverSettings;
	}
}
