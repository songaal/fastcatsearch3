package org.fastcatsearch.plugin;

import java.io.File;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.fastcatsearch.ir.dic.Dictionary;

public abstract class AnalysisPlugin extends Plugin {

	public AnalysisPlugin(File pluginDir, PluginSetting pluginSetting) {
		super(pluginDir, pluginSetting);
	}
	
	protected abstract void loadDictionary();
	
	public abstract Dictionary<?> getDictionary();
	
}
