package org.fastcatsearch.plugin;

import java.io.File;
import java.util.List;

import org.fastcatsearch.db.InternalDBModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Plugin {
	protected static final Logger logger = LoggerFactory.getLogger(Plugin.class);
	
	protected File pluginDir;
	
	protected PluginSetting pluginSetting;
	protected InternalDBModule internalDBModule;
	
	public Plugin(File pluginDir, PluginSetting pluginSetting){
		this.pluginDir = pluginDir;
		this.pluginSetting = pluginSetting;
	}
	
	public void load(){
		loadDB();
		doLoad();
	}
	
	public void unload(){
		doUnload();
		unloadDB();
	}
	
	public void reload(){
		unload();
		load();
	}
	
	protected abstract void doLoad();
	
	protected abstract void doUnload();
	
	private void loadDB() {
		String dbPath = pluginSetting.getDBPath();
		
		List<File> mapperFileList =  null;
		internalDBModule = new InternalDBModule(dbPath, mapperFileList, null, null, null);
		internalDBModule.load();
	}

	
	private void unloadDB() {
		internalDBModule.unload();
	}
	
	
	public File getPluginDir(){
		return pluginDir;
	}
	public PluginSetting getPluginSetting(){
		return pluginSetting;
	}

}
