package org.fastcatsearch.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fastcatsearch.ir.settings.AnalyzerSetting;
import org.fastcatsearch.ir.settings.FieldIndexSetting;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.GroupIndexSetting;
import org.fastcatsearch.ir.settings.IndexSetting;
import org.fastcatsearch.ir.settings.PrimaryKeySetting;
import org.fastcatsearch.ir.settings.RefSetting;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.fastcatsearch.ir.settings.FieldSetting.Type;
import org.json.JSONArray;
import org.json.JSONObject;
/**
 * JSON형 스키마 셋팅의 예제 모습은 다음과 같다.
 * 
	{
		field-list:[
			{id: "CODE", type: "ASTRING" }
			,{id: "TITLE", type: "ASTRING", name:"", size: }
		],
		primary-key:[
			{ref:"CODE"}, {ref:"CATEGORY"}
		],
		analyzer-list: [
			{id: "KOREAN", corePoolSize:10, maximumPoolSize: 100, className:"org.fastcatsearch.plugin.analysis.ko.standard.StandardKoreanAnalyzer" }
		],
		index-list:[
			{id: "TITLE", indexAnalyzer:"korean" , ref_list: "TITLE, CONTENT"}
			,{id: "TITLE", indexAnalyzer:"korean" , ref_list: "TITLE, CONTENT"}
		],
		field-index-list: [
			{id:"SECTIONCODE", name: "SectionCode_index", ref: "SECTIONCODE"}
			,{id:"SECTIONCODE", name: "SectionCode_index", ref: "SECTIONCODE"}
		],
		group-index-list: [
			{id:"CATEGORY", name: "Category_group", ref: "CATEGORY"}
		]
	}
 * 
 * */
public class SchemaSettingUtil {
	
	public static SchemaSetting convertSchemaSetting(JSONObject object){
		
		SchemaSetting schemaSetting = new SchemaSetting();
		
		schemaSetting.setFieldSettingList(parseFieldSettingList(object.optJSONArray("field-list")));
		schemaSetting.setPrimaryKeySetting(parsePrimaryKeySetting(object.optJSONArray("primary-key")));
		schemaSetting.setAnalyzerSettingList(parseAnalyzerSettingList(object.optJSONArray("analyzer-list")));
		schemaSetting.setIndexSettingList(parseIndexSettingList(object.optJSONArray("indx-list")));
		schemaSetting.setFieldIndexSettingList(parseFieldIndexSettingList(object.optJSONArray("field-index-list")));
		schemaSetting.setGroupIndexSettingList(parseGroupSettingList(object.optJSONArray("group-index-list")));
		
		return schemaSetting;
	}
	
	private static List <FieldSetting> parseFieldSettingList(
			JSONArray array) {
		List<FieldSetting> fieldSettingList = new ArrayList<FieldSetting>();
		
		for(int inx=0;inx<array.length(); inx++) {
			FieldSetting setting = new FieldSetting();
			JSONObject data = array.optJSONObject(inx);
			
			setting.setId(data.optString("id"));
			setting.setStore(data.optBoolean("store"));
			setting.setName(data.optString("name"));
			setting.setType(Type.valueOf(data.optString("type")));
			setting.setSize(data.optInt("size"));
			fieldSettingList.add(setting);
		}
		return fieldSettingList;
	}
	
	private static PrimaryKeySetting parsePrimaryKeySetting(
			JSONArray array) {
		PrimaryKeySetting setting = new PrimaryKeySetting();
		
		for(int inx=0;inx<array.length();inx++) {
			JSONObject data = array.optJSONObject(inx);
		}
		
		return setting;
	}
	
	private static List<AnalyzerSetting> parseAnalyzerSettingList(
			JSONArray array) {
		List<AnalyzerSetting> analyzerSettingList = new ArrayList<AnalyzerSetting>();
		
		
		for(int inx=0;inx<array.length();inx++) {
			AnalyzerSetting setting = new AnalyzerSetting();
			JSONObject data = array.optJSONObject(inx);
			setting.setId(data.optString("id"));
			setting.setMaximumPoolSize(data.optInt("max_pool_size"));
			setting.setClassName(data.optString("class"));
			setting.setCorePoolSize(data.optInt("core_pool_size"));
			analyzerSettingList.add(setting);
		}
		
		return analyzerSettingList;
	}
	
	private static List<IndexSetting> parseIndexSettingList(
			JSONArray array) {
		List<IndexSetting> indexSettingList = new ArrayList<IndexSetting>();
		
		for(int inx=0;inx<array.length();inx++) {
			IndexSetting setting = new IndexSetting();
			JSONObject data = array.optJSONObject(inx);
			
			setting.setId(data.optString("id"));
			setting.setIndexAnalyzer(data.optString("index_analyzer"));
			setting.setName(data.optString("name"));
			setting.setQueryAnalyzer(data.optString("query_analyzer"));
			
			//setting.setStorePosition(data.optBoolean("store-position"));
			//setting.setPositionIncrementGap(data.optInt("pig"));
			//setting.setIgnoreCase(data.optBoolean("ignore-case"));
			
			
			List<RefSetting> fieldList = new ArrayList<RefSetting>();
			String[] refArray = data.optString("ref_list").split(",");
			
			for(int refInx=0;refInx<refArray.length;refInx++) {
				RefSetting ref = new RefSetting();
				ref.setRef(refArray[refInx]);
				fieldList.add(ref);
			}
			setting.setFieldList(fieldList);

			indexSettingList.add(setting);
		}
		return indexSettingList;
	}
	
	private static List<FieldIndexSetting> parseFieldIndexSettingList(
			JSONArray array) {
		List<FieldIndexSetting> fieldIndexSettingList = new ArrayList<FieldIndexSetting>();
		
		for(int inx=0;inx<array.length();inx++) {
			FieldIndexSetting setting = new FieldIndexSetting();
			JSONObject data = array.optJSONObject(inx);
			setting.setId(data.optString("id"));
			setting.setName(data.optString("name"));
			setting.setRef(data.optString("ref"));
			//setting.setSize(data.optInt("size"));
			//setting.setIgnoreCase(data.optBoolean("ignore_case"));
			fieldIndexSettingList.add(setting);
		}
		
		return fieldIndexSettingList;
	}

	private static List<GroupIndexSetting> parseGroupSettingList(
			JSONArray array) {
		List<GroupIndexSetting> groupIndexesSettingList = new ArrayList<GroupIndexSetting>();
		
		for(int inx=0;inx<array.length();inx++) {
			GroupIndexSetting setting = new GroupIndexSetting();
			JSONObject data = array.optJSONObject(inx);
			setting.setId(data.optString("id"));
			setting.setName(data.optString("name"));
			setting.setRef(data.optString("ref"));
			//setting.setIgnoreCase(data.optBoolean("ignore-case"));
			groupIndexesSettingList.add(setting);
		}
		
		return groupIndexesSettingList;
	}
}
