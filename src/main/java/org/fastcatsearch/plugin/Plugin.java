package org.fastcatsearch.plugin;

import java.io.File;

public class Plugin {
	
	private File pluginDir;
	
	private PluginSetting pluginSetting;
	
	public Plugin(File pluginDir, PluginSetting pluginSetting){
		this.pluginDir = pluginDir;
		this.pluginSetting = pluginSetting;
	}
	
	public void init(){ }
	
	public File getPluginDir(){
		return pluginDir;
	}
	public PluginSetting getPluginSetting(){
		return pluginSetting;
	}

}
