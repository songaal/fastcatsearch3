package org.fastcatsearch.plugin;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Plugin {
	protected static final Logger logger = LoggerFactory.getLogger(Plugin.class);
	
	protected File pluginDir;
	
	protected PluginSetting pluginSetting;
	
	public Plugin(File pluginDir, PluginSetting pluginSetting){
		this.pluginDir = pluginDir;
		this.pluginSetting = pluginSetting;
	}
	
	public void load(){ }
	
	public void reload(){ }
	
	public void unload(){ }
	
	public File getPluginDir(){
		return pluginDir;
	}
	public PluginSetting getPluginSetting(){
		return pluginSetting;
	}

}
