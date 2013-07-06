package org.fastcatsearch.util;

public class PluginMenu {
	String pluginId;
	String categoryLabel;
	
	public PluginMenu(String pluginId, String categoryLabel){
		this.pluginId = pluginId;
		this.categoryLabel = categoryLabel;
	}
	
	public String pluginId(){
		return pluginId;
	}
	
	public String categoryLabel(){
		return categoryLabel;
	}
}
