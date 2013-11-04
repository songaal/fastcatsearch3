package org.fastcatsearch.ir.settings;

import org.apache.lucene.analysis.Analyzer;
import org.fastcatsearch.ir.analysis.AnalyzerFactory;
import org.fastcatsearch.ir.analysis.DefaultAnalyzerFactory;
import org.fastcatsearch.util.DynamicClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyzerFactoryLoader {
	
	private static Logger logger = LoggerFactory.getLogger(AnalyzerFactoryLoader.class);
	
	public static AnalyzerFactory load(String analyzerClassName){
		if(analyzerClassName!=null) {
			analyzerClassName = analyzerClassName.trim();
		}
		String factoryClassName = analyzerClassName+"Factory";
		Class<?> analyzerFactoryClass = DynamicClassLoader.loadClass(factoryClassName);
		AnalyzerFactory factory = null;
		if(analyzerFactoryClass == null){
			Class<Analyzer> analyzerClass = (Class<Analyzer>) DynamicClassLoader.loadClass(analyzerClassName);
			if(analyzerClass == null){
				logger.error("Analyzer {}를 생성할수 없습니다.", analyzerClassName);
			}else{
				factory = new DefaultAnalyzerFactory(analyzerClass);
			}
		}else{
			try {
				factory = (AnalyzerFactory) analyzerFactoryClass.newInstance();
			} catch (Exception e) {
				logger.error("AnalyzerFactory {}를 생성할수 없습니다.", factoryClassName);
			}
		}
		
		return factory;
	}
}
