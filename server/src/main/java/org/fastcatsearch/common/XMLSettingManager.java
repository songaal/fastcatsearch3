package org.fastcatsearch.common;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.fastcatsearch.util.JAXBConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMLSettingManager<T> {
	protected static final Logger logger = LoggerFactory.getLogger(XMLSettingManager.class);
	protected File settingFile;
	protected T setting;
	protected Class<T> clazz;
	public XMLSettingManager(File settingFile, Class<T> clazz){
		this.settingFile = settingFile;
		this.clazz = clazz;
		load();
	}
	
	public void reload(){
		
	}
	private void load(){
		try {
			setting = JAXBConfigs.readConfig(settingFile, clazz);
		} catch (JAXBException e) {
			logger.error("", e);
		}
	}
	public T getSetting(){
		return setting;
	}
	
	public void save(){
		try {
			JAXBConfigs.writeConfig(settingFile, setting, clazz);
		} catch (JAXBException e) {
			logger.error("", e);
		}
	}
}
