package org.fastcatsearch.ir.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * factory가 없는 analyzer에 대해서 factory를 생성해준다.
 * analyzer생성에 별도의 외부객체나 로직이 필요없는 경우 이 factory를 사용하여 별도의 factory개발의 수고를 덜수 있다. 
 * */
public class DefaultAnalyzerFactory implements AnalyzerFactory {
	protected static final Logger logger = LoggerFactory.getLogger(DefaultAnalyzerFactory.class);
	protected Class<? extends Analyzer> analyzerClass;
	
	public DefaultAnalyzerFactory(Class<? extends Analyzer> analyzerClass) {
		this.analyzerClass = analyzerClass;
	}

	@Override
	public Analyzer create() {
		try {
			return analyzerClass.newInstance();
		} catch (Exception e) {
			logger.error("{}를 생성하지 못했습니다.", analyzerClass.getName());
		}
		
		return null;
	}

	@Override
	public void init() {
		
	}

	@Override
	public Class<? extends Analyzer> getAnalyzerClass() {
		return analyzerClass;
	}

}
