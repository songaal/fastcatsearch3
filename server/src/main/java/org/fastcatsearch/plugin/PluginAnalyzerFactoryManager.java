package org.fastcatsearch.plugin;

import java.util.HashMap;
import java.util.Map;

import org.fastcatsearch.ir.analysis.AnalyzerFactory;
import org.fastcatsearch.ir.analysis.AnalyzerFactoryManager;

public class PluginAnalyzerFactoryManager extends AnalyzerFactoryManager {

	private Map<String, AnalyzerFactory> map;
	
	public PluginAnalyzerFactoryManager(){
		map = new HashMap<String, AnalyzerFactory>();
	}
	
	@Override
	public AnalyzerFactory getAnalyzerFactory(String analyzerId) {
		return map.get(analyzerId.toUpperCase());
	}
	
	public void addAnalyzerFactory(String analyzerId, AnalyzerFactory factory){
		map.put(analyzerId.toUpperCase(), factory);
	}
	
	public void clear(){
		if(map != null){
			map.clear();
		}
	}

}
