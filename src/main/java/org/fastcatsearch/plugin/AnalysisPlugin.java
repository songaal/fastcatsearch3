package org.fastcatsearch.plugin;

import java.io.File;

import org.fastcatsearch.ir.dic.Dictionary;

public abstract class AnalysisPlugin extends Plugin {

	public AnalysisPlugin(File pluginDir, PluginSetting pluginSetting) {
		super(pluginDir, pluginSetting);
		// TODO Auto-generated constructor stub
	}
	
	protected abstract void loadDictionary();
	
	public abstract Dictionary getDictionary();
}
