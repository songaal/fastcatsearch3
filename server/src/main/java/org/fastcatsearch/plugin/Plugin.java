package org.fastcatsearch.plugin;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.db.InternalDBModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Plugin {
	protected static final Logger logger = LoggerFactory.getLogger(Plugin.class);
	
	protected File pluginDir;
	protected String pluginId;
	protected PluginSetting pluginSetting;
	protected InternalDBModule internalDBModule;
	
	public Plugin(File pluginDir, PluginSetting pluginSetting){
		this.pluginDir = pluginDir;
		this.pluginSetting = pluginSetting;
		this.pluginId = pluginSetting.getId();
	}
	
	public final void load(boolean isMaster){
		boolean isLoadDb = isMaster && pluginSetting.isUseDB();
		if(isLoadDb){
			loadDB();
		}
		doLoad(isLoadDb);
	}
	
	public final void unload(){
		doUnload();
		if(pluginSetting.isUseDB()){
			unloadDB();
		}
	}
	
	public final void reload(boolean isMaster){
		unload();
		load(isMaster);
	}
	
	protected abstract void doLoad(boolean isLoadDb);
	
	protected abstract void doUnload();
	
	private final void loadDB() {
		String dbPath = getPluginDBDataDir().getAbsolutePath();
		new File(dbPath).getParentFile().mkdirs();
		List<URL> mapperFileList = new ArrayList<URL>();
		addMapperFile(mapperFileList);
		internalDBModule = new InternalDBModule(dbPath, mapperFileList, null, null, null);
		internalDBModule.load();
	}

	protected abstract void addMapperFile(List<URL> mapperFileList);
	
	private void unloadDB() {
		if(internalDBModule != null){
			internalDBModule.unload();
		}
	}
	
	public InternalDBModule internalDBModule(){
		return internalDBModule;
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
