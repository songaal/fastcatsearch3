package org.fastcatsearch.ir.settings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.ir.analysis.AnalyzerFactory;
import org.fastcatsearch.ir.analysis.AnalyzerPool;
import org.fastcatsearch.ir.analysis.AnalyzerPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Schema {
	
	private static Logger logger = LoggerFactory.getLogger(Schema.class);
	
	private SchemaSetting schemaSetting;
	private Map<String, Integer> fieldSequenceMap;
	private Map<String, Integer> searchIndexSequenceMap;
	private Map<String, Integer> fieldIndexSequenceMap;
	private Map<String, Integer> groupIndexSequenceMap;
	private Map<String, FieldSetting> fieldSettingMap;
	
//	private AnalyzerPoolManager analyzerPoolManager;
	private String collectionId;
	
	public String toString(){
		return "[Schema]" + schemaSetting; 
		
	}
	
	public Schema(String collectionId, SchemaSetting schemaSetting){
		this(schemaSetting);
		this.collectionId = collectionId;
	}
	public boolean isEmpty(){
		return schemaSetting == null;
	}
	public Schema(SchemaSetting schemaSetting){
		if(schemaSetting == null){
			return;
		}
		
		this.schemaSetting = schemaSetting;
		
		//필드명 : 번호 맵핑.
		this.fieldSequenceMap = new HashMap<String, Integer>();
		this.fieldSettingMap = new HashMap<String, FieldSetting>();
		List<FieldSetting> fieldSettingList = schemaSetting.getFieldSettingList();
		for (int i = 0; fieldSettingList != null && i < fieldSettingList.size(); i++) {
			FieldSetting fieldSetting = fieldSettingList.get(i);
			fieldSequenceMap.put(fieldSetting.getId().toUpperCase(), i);
			//필드셋팅객체
			fieldSettingMap.put(fieldSetting.getId().toUpperCase(), fieldSetting);
		}
		//search index 필드명 : 번호 맵핑.
		this.searchIndexSequenceMap = new HashMap<String, Integer>();
		List<IndexSetting> indexSettingList = schemaSetting.getIndexSettingList();
		for (int i = 0; indexSettingList != null && i < indexSettingList.size(); i++) {
			IndexSetting indexSetting = indexSettingList.get(i);
			searchIndexSequenceMap.put(indexSetting.getId().toUpperCase(), i);
		}
		
		//field index 필드명 : 번호 맵핑.
		this.fieldIndexSequenceMap = new HashMap<String, Integer>();
		List<FieldIndexSetting> fieldIndexSettingList = schemaSetting.getFieldIndexSettingList();
		for (int i = 0; fieldIndexSettingList != null && i < fieldIndexSettingList.size(); i++) {
			FieldIndexSetting fieldIndexSetting = fieldIndexSettingList.get(i);
			fieldIndexSequenceMap.put(fieldIndexSetting.getId().toUpperCase(), i);
		}
		
		//group index 필드명 : 번호 맵핑.
		this.groupIndexSequenceMap = new HashMap<String, Integer>();
		List<GroupIndexSetting> groupIndexSettingList = schemaSetting.getGroupIndexSettingList();
		for (int i = 0; groupIndexSettingList != null && i < groupIndexSettingList.size(); i++) {
			GroupIndexSetting groupIndexSetting = groupIndexSettingList.get(i);
			groupIndexSequenceMap.put(groupIndexSetting.getId().toUpperCase(), i);
		}
		
//		//분석기 객체화.
//		analyzerPoolManager = new AnalyzerPoolManager();
//		
//		List<AnalyzerSetting> analyzerSettingSettingList = schemaSetting.getAnalyzerSettingList();
//		if(analyzerSettingSettingList != null){
//			logger.debug("AnalyzerSetting size ={}", analyzerSettingSettingList.size());
//			
//			for(AnalyzerSetting analyzerSetting : analyzerSettingSettingList){
//				String analyzerClassName = analyzerSetting.getClassName();
//				String analyzerId = analyzerSetting.getId();
//				if(analyzerClassName == null || analyzerId == null){
//					continue;
//				}
//				int corePoolSize = analyzerSetting.getCorePoolSize();
//				int maximumPoolSize = analyzerSetting.getMaximumPoolSize();
//				AnalyzerFactory factory = AnalyzerFactoryLoader.load(analyzerClassName);
//				
//				if(factory != null){
//					if(corePoolSize == -1 || maximumPoolSize == -1){
//						analyzerPoolManager.registerAnalyzer(analyzerId.toUpperCase(), factory);
//					}else{
//						analyzerPoolManager.registerAnalyzer(analyzerId.toUpperCase(), factory, corePoolSize, maximumPoolSize);
//					}
//				}else{
//					logger.error("분석기 {}를 로드할수 없습니다.", analyzerClassName);
//				}
//				
//			}
//		}
		//primary key
		
	}
	
	public String collectionId(){
		return collectionId;
	}
	
//	public AnalyzerPoolManager getAnalyzerPoolManager(){
//		return analyzerPoolManager;
//	}
//	public AnalyzerPool getAnalyzerPool(String analyzerId){
//		return analyzerPoolManager.getPool(analyzerId);
//	}
	public int getFieldSequence(String fieldId){
		Integer i =  fieldSequenceMap.get(fieldId);
		if(i == null){
			return -1;
		}else{
			return i;
		}
	}
	
	public SchemaSetting schemaSetting(){
		return schemaSetting;
	}
	
	public Map<String, Integer> fieldSequenceMap(){
		return fieldSequenceMap;
	}
	
	public Map<String, FieldSetting> fieldSettingMap(){
		return fieldSettingMap;
	}
	
	public FieldSetting getFieldSetting(String fieldId){
		return fieldSettingMap.get(fieldId);
	}

	
	public int getSearchIndexSequence(String indexFieldId) {
		Integer i = searchIndexSequenceMap.get(indexFieldId);
		if(i == null){
			return -1;
		}else{
			return i;
		}
	}
	
	public int getFieldIndexSequence(String indexFieldId) {
		Integer i = fieldIndexSequenceMap.get(indexFieldId);
		if(i == null){
			return -1;
		}else{
			return i;
		}
	}
	
	public int getGroupIndexSequence(String indexFieldId) {
		Integer i = groupIndexSequenceMap.get(indexFieldId);
		if(i == null){
			return -1;
		}else{
			return i;
		}
	}
	
	public IndexSetting getSearchIndexSetting(String indexFieldId){
		int sequence = getSearchIndexSequence(indexFieldId);
		if(sequence == -1){
			return null;
		}
		List<IndexSetting> list = schemaSetting.getIndexSettingList();
		return list.get(sequence);
	}
	
	public FieldIndexSetting getFieldIndexSetting(String indexFieldId){
		int sequence = getFieldIndexSequence(indexFieldId);
		if(sequence == -1){
			return null;
		}
		List<FieldIndexSetting> list = schemaSetting.getFieldIndexSettingList();
		return list.get(sequence);
	}
	
	public GroupIndexSetting getGroupIndexSetting(String indexFieldId){
		int sequence = getGroupIndexSequence(indexFieldId);
		if(sequence == -1){
			return null;
		}
		List<GroupIndexSetting> list = schemaSetting.getGroupIndexSettingList();
		return list.get(sequence);
	}
	public int getFieldSize(){
		return fieldSettingMap.size();
	}

	public void update(Schema other) {
		this.collectionId = other.collectionId;
		this.schemaSetting = other.schemaSetting;
		this.fieldSequenceMap = other.fieldSequenceMap;
		this.searchIndexSequenceMap = other.searchIndexSequenceMap;
		this.fieldIndexSequenceMap = other.fieldIndexSequenceMap;
		this.groupIndexSequenceMap = other.groupIndexSequenceMap;
		this.fieldSettingMap = other.fieldSettingMap;
//		this.analyzerPoolManager = other.analyzerPoolManager;
	}
	
}
