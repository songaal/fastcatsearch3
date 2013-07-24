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
	
	private AnalyzerPoolManager analyzerPoolManager;
	private String collectionId;
	
	public String toString(){
		return "[Schema]" + schemaSetting; 
		
	}
	
	public Schema(String collectionId, SchemaSetting schemaSetting){
		this(schemaSetting);
		this.collectionId = collectionId;
	}
	public Schema(SchemaSetting schemaSetting){
		if(schemaSetting == null){
			return;
		}
		
		this.schemaSetting = schemaSetting;
		
		//필드명 : 번호 맵핑.
		this.fieldSequenceMap = new HashMap<String, Integer>();
		List<FieldSetting> fieldSettingList = schemaSetting.getFieldSettingList();
		for (int i = 0; i < fieldSettingList.size(); i++) {
			FieldSetting fieldSetting = fieldSettingList.get(i);
			fieldSequenceMap.put(fieldSetting.getId(), i);
		}
		//search index 필드명 : 번호 맵핑.
		this.searchIndexSequenceMap = new HashMap<String, Integer>();
		List<IndexSetting> indexSettingList = schemaSetting.getIndexSettingList();
		for (int i = 0; i < indexSettingList.size(); i++) {
			IndexSetting indexSetting = indexSettingList.get(i);
			searchIndexSequenceMap.put(indexSetting.getId(), i);
		}
		
		//field index 필드명 : 번호 맵핑.
		this.fieldIndexSequenceMap = new HashMap<String, Integer>();
		List<FieldIndexSetting> fieldIndexSettingList = schemaSetting.getFieldIndexSettingList();
		for (int i = 0; i < fieldIndexSettingList.size(); i++) {
			FieldIndexSetting fieldIndexSetting = fieldIndexSettingList.get(i);
			fieldIndexSequenceMap.put(fieldIndexSetting.getId(), i);
		}
		
		//group index 필드명 : 번호 맵핑.
		this.groupIndexSequenceMap = new HashMap<String, Integer>();
		List<GroupIndexSetting> groupIndexSettingList = schemaSetting.getGroupIndexSettingList();
		for (int i = 0; i < groupIndexSettingList.size(); i++) {
			GroupIndexSetting groupIndexSetting = groupIndexSettingList.get(i);
			groupIndexSequenceMap.put(groupIndexSetting.getId(), i);
		}
		
		//필드셋팅객체
		this.fieldSettingMap = new HashMap<String, FieldSetting>();
		for(FieldSetting fieldSetting : schemaSetting.getFieldSettingList()){
			fieldSettingMap.put(fieldSetting.getId(), fieldSetting);
		}
		
		//분석기 객체화.
		analyzerPoolManager = new AnalyzerPoolManager();
		
		List<AnalyzerSetting> analyzerSettingSettingList = schemaSetting.getAnalyzerSettingList();
		for(AnalyzerSetting analyzerSetting : analyzerSettingSettingList){
			String analyzerClassName = analyzerSetting.getAnalyzer();
			String analyzerId = analyzerSetting.getId();
			int corePoolSize = analyzerSetting.getCorePoolSize();
			int maximumPoolSize = analyzerSetting.getMaximumPoolSize();
			AnalyzerFactory factory = AnalyzerFactoryLoader.load(analyzerClassName);
			
			if(factory != null){
				if(corePoolSize == -1 || maximumPoolSize == -1){
					analyzerPoolManager.registerAnalyzer(analyzerId, factory);
				}else{
					analyzerPoolManager.registerAnalyzer(analyzerId, factory, corePoolSize, maximumPoolSize);
				}
			}else{
				logger.error("분석기 {}를 로드할수 없습니다.", analyzerClassName);
			}
			
		}
		
		//primary key
		
	}
	
	public String collectionId(){
		return collectionId;
	}
	public AnalyzerPool getAnalyzerPool(String analyzerId){
		return analyzerPoolManager.getPool(analyzerId);
	}
	public int getFieldSequence(String fieldId){
		return fieldSequenceMap.get(fieldId);
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
		return searchIndexSequenceMap.get(indexFieldId);
	}
	
	public int getFieldIndexSequence(String indexFieldId) {
		return fieldIndexSequenceMap.get(indexFieldId);
	}
	
	public int getGroupIndexSequence(String indexFieldId) {
		return groupIndexSequenceMap.get(indexFieldId);
	}
	
	public int getFieldSize(){
		return fieldSettingMap.size();
	}

	public void update(Schema other) {
		this.collectionId = other.collectionId;
		this.schemaSetting = other.schemaSetting;
		//work schema는 업데이트하지 않는다.
		this.fieldSequenceMap = other.fieldSequenceMap;
		this.searchIndexSequenceMap = other.searchIndexSequenceMap;
		this.fieldIndexSequenceMap = other.fieldIndexSequenceMap;
		this.groupIndexSequenceMap = other.groupIndexSequenceMap;
		this.fieldSettingMap = other.fieldSettingMap;
		this.analyzerPoolManager = other.analyzerPoolManager;
	}
	
}
