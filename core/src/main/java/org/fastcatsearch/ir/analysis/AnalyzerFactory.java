package org.fastcatsearch.ir.analysis;

import org.apache.lucene.analysis.Analyzer;

public interface AnalyzerFactory {
	
	public void init();
	
	public Analyzer create();
	
	public Analyzer create(Object option);
	
	public Class<? extends Analyzer> getAnalyzerClass();
}
