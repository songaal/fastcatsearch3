package org.fastcatsearch.datasource.reader;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.SingleSourceConfig;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.fastcatsearch.util.DynamicClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SingleSourceReader {
	
	protected static Logger logger = LoggerFactory.getLogger(SingleSourceReader.class);
	
	protected File filePath;
	protected SingleSourceConfig singleSourceConfig;
	protected String lastIndexTime; //마지막 수집시작.(시작시각)
	protected boolean isFull; //전체색인용 수집인지.
	
	protected SourceModifier sourceModifier;
	protected DeleteIdSet deleteIdList;
	
	public abstract boolean hasNext() throws IRException;
	protected abstract Map<String, Object> next() throws IRException;
	public abstract void close() throws IRException;
	
	public SingleSourceReader(File filePath, SingleSourceConfig singleSourceConfig, String lastIndexTime, boolean isFull) {
		this.filePath = filePath;
		this.singleSourceConfig = singleSourceConfig;
		this.lastIndexTime = lastIndexTime;
		this.isFull = isFull;
		this.sourceModifier = DynamicClassLoader.loadObject(singleSourceConfig.getSourceModifier(), SourceModifier.class);
		if(sourceModifier == null){
			logger.error("unable to find source modifier class {}", singleSourceConfig.getSourceModifier());
		}
	}
	
	protected Map<String, Object> nextElement() throws IRException {
		
		//modifier를 태운다.
		
		if(sourceModifier == null){
			return next();
		}else{
			Map<String, Object> source = next();
			Iterator<Entry<String, Object>> iterator = source.entrySet().iterator();
			while(iterator.hasNext()){
				Entry<String, Object> entry = iterator.next();
				Object newValue = sourceModifier.modify(entry.getKey(), source);
				//원본 데이터를 변경해준다.
				entry.setValue(newValue);
			}
			return source;
		}
	}
	
	public void setDeleteIdList(DeleteIdSet deleteIdList) {
		this.deleteIdList = deleteIdList;
	}
	
}
