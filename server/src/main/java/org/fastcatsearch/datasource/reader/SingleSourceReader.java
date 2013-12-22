package org.fastcatsearch.datasource.reader;

import java.io.File;

import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.env.Path;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.SingleSourceConfig;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SingleSourceReader<SourceType> {
	
	protected static Logger logger = LoggerFactory.getLogger(SingleSourceReader.class);
	
	protected Path filePath;
	protected DataSourceConfig dataSourceConfig;
	protected SingleSourceConfig singleSourceConfig;
	protected String lastIndexTime; //마지막 수집시작.(시작시각)
	
	protected SourceModifier<SourceType> sourceModifier;
	protected DeleteIdSet deleteIdList;
	
	public abstract void init() throws IRException; //초기화. 파일을 여는등의 작업.
	public abstract boolean hasNext() throws IRException;
	protected abstract SourceType next() throws IRException;
	public abstract void close() throws IRException;
	
	public SingleSourceReader(){
	}
	
	public SingleSourceReader(File filePath, DataSourceConfig dataSourceConfig, SingleSourceConfig singleSourceConfig, SourceModifier<SourceType> sourceModifier, String lastIndexTime) {
		this.filePath = new Path(filePath);
		this.dataSourceConfig = dataSourceConfig;
		this.singleSourceConfig = singleSourceConfig;
		this.lastIndexTime = lastIndexTime;
		this.sourceModifier = sourceModifier;
	}
	
	protected SourceType nextElement() throws IRException {
		
		//modifier를 태운다.
		SourceType source = next();
		if(sourceModifier != null){
			sourceModifier.modify(source);
		}
		return source;
	}
	
	public void setDeleteIdList(DeleteIdSet deleteIdList) {
		this.deleteIdList = deleteIdList;
	}
	
	protected String getConfigValue(String key){
		return singleSourceConfig.getProperties().get(key);
	}
	
	protected String getConfigString(String key){
		return getConfigString(key, null);
	}
	protected String getConfigString(String key, String defaultValue){
		String value = getConfigValue(key);
		return value == null ? defaultValue : value;
	}
	protected boolean getConfigBoolean(String key){
		return getConfigBoolean(key, false);
	}
	protected boolean getConfigBoolean(String key, boolean defaultValue){
		return "true".equalsIgnoreCase(getConfigValue(key));
	}
	protected int getConfigInt(String key){
		return getConfigInt(key, 0);
	}
	protected int getConfigInt(String key, int defaultValue){
		try{
			return Integer.parseInt(getConfigValue(key));
		}catch(NumberFormatException e){
			return defaultValue;
		}
	}
	protected long getConfigLong(String key){
		return getConfigLong(key, 0L);
	}
	protected long getConfigLong(String key, long defaultValue){
		try{
			return Long.parseLong(getConfigValue(key));
		}catch(NumberFormatException e){
			return defaultValue;
		}
	}
}
