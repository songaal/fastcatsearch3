package org.fastcatsearch.plugin;

import java.io.File;

public class Plugin {
	
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
