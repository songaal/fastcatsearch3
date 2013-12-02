package org.fastcatsearch.util;

import org.fastcatsearch.ir.settings.SchemaSetting;
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
		
		//TODO 채운다.
		
		
		return schemaSetting;
	}
	
	
}
