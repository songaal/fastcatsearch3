package org.fastcatsearch.datasource.reader;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.env.Path;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.SingleSourceConfig;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SingleSourceReader {
	
	protected static Logger logger = LoggerFactory.getLogger(SingleSourceReader.class);
	
	protected Path filePath;
	protected DataSourceConfig dataSourceConfig;
	protected SingleSourceConfig singleSourceConfig;
	protected String lastIndexTime; //마지막 수집시작.(시작시각)
	
	protected SourceModifier sourceModifier;
	protected DeleteIdSet deleteIdList;
	
	public abstract void init() throws IRException; //초기화. 파일을 여는등의 작업.
	public abstract boolean hasNext() throws IRException;
	protected abstract Map<String, Object> next() throws IRException;
	public abstract void close() throws IRException;
	
	public SingleSourceReader(File filePath, DataSourceConfig dataSourceConfig, SingleSourceConfig singleSourceConfig, SourceModifier sourceModifier, String lastIndexTime) {
		this.filePath = new Path(filePath);
		this.dataSourceConfig = dataSourceConfig;
		this.singleSourceConfig = singleSourceConfig;
		this.lastIndexTime = lastIndexTime;
		this.sourceModifier = sourceModifier;
	}
	
	protected Map<String, Object> nextElement() throws IRException {
		
		//modifier를 태운다.
		Map<String, Object> source = next();
		if(sourceModifier != null){
			sourceModifier.modify(source);
		}
		return source;
	}
	
	public void setDeleteIdList(DeleteIdSet deleteIdList) {
		this.deleteIdList = deleteIdList;
	}
	
}
