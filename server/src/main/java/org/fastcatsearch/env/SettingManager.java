package org.fastcatsearch.env;

import static org.fastcatsearch.env.FileNames.bakupSuffix;
import static org.fastcatsearch.env.FileNames.datasourceFilename;
import static org.fastcatsearch.env.FileNames.schemaFilename;
import static org.fastcatsearch.env.FileNames.schemaObject;
import static org.fastcatsearch.env.FileNames.schemaWorkFilename;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.datasource.DataSourceSetting;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.settings.Schema;
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

	public void applyWorkSchemaFile(String collection) {
		String workFilepath = getKey(collection, schemaWorkFilename);
		File workf = new File(workFilepath);

		if (!workf.exists())
			return;

		String bakFilepath = getKey(collection, schemaFilename + bakupSuffix);
		File bakf = new File(bakFilepath);

		String filepath = getKey(collection, schemaFilename);
		File f = new File(filepath);

		if (bakf.exists())
			bakf.delete();
		if (f.exists())
			f.renameTo(bakf);

		workf.renameTo(f);

	}

	public void initSchema(String collection) {
		String contents = "<schema name=\"" + collection + "\" version=\"1.0\">" + Environment.LINE_SEPARATOR + "</schema>";
		String configFile = getKey(collection, schemaFilename);
		FileOutputStream writer = null;
		try {
			writer = new FileOutputStream(configFile);
			writer.write(contents.getBytes());
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

	public Schema getSchema(String collection, boolean reload) throws SettingException {
		Element root = null;
		Schema schema = null;

		if (!reload) {
			schema = (Schema) getFromCache(collection, schemaObject);
			if (schema != null)
				return schema;
		}

		if (!reload) {
			root = (Element) getFromCache(collection, schemaFilename);
		}

		if (root == null)
			root = getXml(collection, schemaFilename);

		if (root == null)
			return null;

		putToCache(collection, schema, schemaObject);

		schema = getSchema0(collection, root);
		putToCache(schema, schemaObject);

		return schema;
	}

	public Schema getWorkSchema(String collection) throws SettingException {
		return getWorkSchema(collection, false, false);
	}

	public Schema getWorkSchema(String collection, boolean reload, boolean create) throws SettingException {
		Element root = null;
		if (reload)
			root = getXml(collection, schemaWorkFilename);

		if (root == null) {
			if (create) {
				String workSchemaFileDir = getKey(collection, schemaWorkFilename);
				String schemaFileDir = getKey(collection, schemaFilename);
				File fworkSchema = new File(workSchemaFileDir);
				try {
					if (!fworkSchema.exists())
						FileUtils.touch(fworkSchema);
					File fschema = new File(schemaFileDir);
					FileUtils.copyFile(fschema, fworkSchema);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
				root = getXml(collection, schemaWorkFilename);
			} else {
				return null;
			}
		}

		return getSchema0(collection, root);
	}


	public int deleteWorkSchema(String collection) throws SettingException {
		String xmlDir = getKey(collection, schemaWorkFilename);
		File workSchema = new File(xmlDir);
		try {
			FileUtils.forceDelete(workSchema);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return 1;
		}

		return 0;
	}

	public DataSourceSetting getDatasource(String collection, boolean reload) {
		if (!reload) {
			Properties p = (Properties) getFromCache(collection, datasourceFilename);
			if (p != null)
				return new DataSourceSetting(p);
		}
		return new DataSourceSetting(getXmlProperties(collection, datasourceFilename));
	}

	/**
	 * datasourceFilename에 .1 .2 가 붙은 파일들을 순차적으로 찾는다.
	 * */
	public List<DataSourceSetting> getMultiDatasource(String collection, boolean reload) {

		List<DataSourceSetting> list = new ArrayList<DataSourceSetting>();

		File f = new File(getKey(collection, datasourceFilename));
		Properties p = null;
		if (!reload) {
			p = (Properties) getFromCache(collection, datasourceFilename);
		}
		if (p == null) {
			p = getXmlProperties(collection, datasourceFilename);
		}
		list.add(new DataSourceSetting(p));

		for (int i = 1;; i++) {
			f = new File(getKey(collection, datasourceFilename + "." + i));

			if (!f.exists()) {
				break;
			}

			p = null;
			if (!reload) {
				p = (Properties) getFromCache(collection, datasourceFilename + "." + i);
			}
			if (p == null) {
				p = getXmlProperties(collection, datasourceFilename + "." + i);
			}
			list.add(new DataSourceSetting(p));
			logger.debug("Multi Datasource {} >> {}", i, f.getName());

		}

		return list;
	}

	public void storeDataSourceSetting(String collection, DataSourceSetting setting) {
		if (setting == null) {
			logger.error("DataSourceSetting file is null.");
		} else {
			Properties props = setting.getProperties();
			storeXmlProperties(collection, props, datasourceFilename);
			putToCache(collection, props, datasourceFilename);
		}

	}

	public void initDatasource(String collection) {
		Properties props = new Properties();
		DataSourceSetting.init(props);
		storeXmlProperties(collection, props, datasourceFilename);
	}

	public String getSimpleDatetime() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}

	private Object getFromCache(String settingName) {
		String key = environment.filePaths().makePath("conf").append(settingName).toString();
		return settingCache.get(key);
	}

	private Object getFromCache(String collection, String settingName) {
		String key = getKey(collection, settingName);
		return settingCache.get(key);
	}

	private Object putToCache(Object setting, String settingName) {
		String key = environment.filePaths().makePath("conf").append(settingName).toString();
		settingCache.put(key, setting);
		return setting;
	}

	private Object putToCache(String collection, Object setting, String settingName) {
		String key = getKey(collection, settingName);
		settingCache.put(key, setting);
		return setting;
	}

	public String getKey(String collection, String filename) {
		return environment.filePaths().makePath("collection").append(collection).append(filename).toString();
	}

	public String getKey(String filename) {
		return environment.filePaths().makePath("conf").append(filename).toString();
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

	public Settings getSettings() {
		//load config file
		Settings serverSettings = null;
		synchronized(FileNames.serverConfig){
			Object obj = getFromCache(FileNames.serverConfig);
			if(obj != null){
				return (Settings) obj;
			}
			File configFile = environment.filePaths().makePath("conf").append(FileNames.serverConfig).file();
	        InputStream input = null;
	        try{
	        	Yaml yaml = new Yaml();
	        	input = new FileInputStream(configFile);
	        	Map<String, Object> data = (Map<String, Object>) yaml.load(input);
	        	serverSettings = new Settings(data);
	        	putToCache(serverSettings, FileNames.serverConfig);
	        } catch (FileNotFoundException e) {
	        	logger.error("설정파일을 찾을수 없습니다. file = {}", configFile.getAbsolutePath());
			} finally {
	        	if(input != null){
	        		try {
						input.close();
					} catch (IOException ignore) {
					}
	        	}
	        }
		}
		
		return serverSettings;
	}
}
