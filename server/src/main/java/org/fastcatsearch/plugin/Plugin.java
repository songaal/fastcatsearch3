package org.fastcatsearch.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.io.Resources;
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
	
	public final void load(){
		if(pluginSetting.isUseDB()){
			loadDB();
		}
		doLoad();
	}
	
	public final void unload(){
		doUnload();
		if(pluginSetting.isUseDB()){
			unloadDB();
		}
	}
	
	public final void reload(){
		unload();
		load();
	}
	
	protected abstract void doLoad();
	
	protected abstract void doUnload();
	
	private final void loadDB() {
		String dbPath = getPluginDBDataDir().getAbsolutePath();
		new File(dbPath).mkdirs();
		List<File> mapperFileList = new ArrayList<File>();
		addMapperFile(mapperFileList);
		internalDBModule = new InternalDBModule(dbPath, mapperFileList, null, null, null);
		internalDBModule.load();
	}

	protected abstract void addMapperFile(List<File> mapperFileList);
	
	private void unloadDB() {
		if(internalDBModule != null){
			internalDBModule.unload();
		}
	}
	
	
	public File getPluginDir(){
		return pluginDir;
	}
	protected File getPluginDBDir(){
		return new File(pluginDir, "db");
	}
	protected File getPluginDBDataDir(){
		return new File(getPluginDBDir(), "data");
	}
	protected File getPluginDBConfigDir(){
		return new File(getPluginDBDir(), "config");
	}
	public PluginSetting getPluginSetting(){
		return pluginSetting;
	}

}
