package org.fastcatsearch.plugin.analysis;

import org.fastcatsearch.ir.analysis.AnalyzerFactory;

public class AnalyzerInfo {
	private String name;
	private AnalyzerFactory factory;
	
	public AnalyzerInfo(String name, AnalyzerFactory factory){
		this.name = name;
		this.factory = factory;
	}

	public String name() {
		return name;
	}

	public AnalyzerFactory factory() {
		return factory;
	}
	
}
