package org.fastcatsearch.util;

import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.ir.settings.AnalyzerSetting;
import org.fastcatsearch.ir.settings.FieldIndexSetting;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.GroupIndexSetting;
import org.fastcatsearch.ir.settings.IndexRefSetting;
import org.fastcatsearch.ir.settings.IndexSetting;
import org.fastcatsearch.ir.settings.PrimaryKeySetting;
import org.fastcatsearch.ir.settings.RefSetting;
import org.fastcatsearch.ir.settings.SchemaInvalidateException;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.fastcatsearch.ir.settings.FieldSetting.Type;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON형 스키마 셋팅의 예제 모습은 다음과 같다.
 * 
{
    "group-index-list": [
        {
            "id": "CATEGORY",
            "ref": "CATEGORY",
            "name": "Category_group"
        },
        {
            "id": "USERID",
            "ref": "USERID",
            "name": "UserID_group"
        }
    ],
    "index-list": [
        {
            "id": "TITLE",
            "name": "1",
            "queryAnalyzer": "korean",
            "refList": "TITLE",
            "indexAnalyzer" : "korean"
        },
        {
            "id": "CONTENT",
            "name": "2",
            "queryAnalyzer": "korean",
            "refList": "CONTENT",
            "indexAnalyzer" : "korean"
            "storePosition": "true",
            "pig": "100"
        },
        {
            "id": "TITLECONTENT",
            "name": "3",
            "queryAnalyzer": "korean",
            "refList": "TITLE\nCONTENT",
            "indexAnalyzer" : "korean\nkorean"
            "storePosition": "true",
            "pig": "100"
        }
    ],
    "field-list": [
        {
            "id": "CODE",
            "store": "true",
            "name": "코드",
            "type": "ASTRING",
            "size": "17"
        },
        {
            "id": "SECTIONCODE",
            "store": "true",
            "name": "섹션코드",
            "type": "ASTRING",
            "size": "1"
        },
        {
            "id": "CATEGORY",
            "store": "true",
            "name": "카테고리",
            "type": "STRING"
        },
        {
            "id": "TITLE",
            "store": "true",
            "name": "제목",
            "type": "STRING"
        },
        {
            "id": "USERID",
            "store": "true",
            "name": "사용자아이디",
            "type": "STRING"
        },
        {
            "id": "USERNAME",
            "store": "true",
            "name": "사용자이름",
            "type": "STRING"
        },
        {
            "id": "CONTENT",
            "store": "true",
            "removeTag": "true",
            "name": "기사내용",
            "type": "STRING"
        },
        {
            "id": "REGDATE",
            "store": "true",
            "name": "등록일",
            "type": "DATETIME"
        },
        {
            "id": "UPDATEDATE",
            "store": "true",
            "name": "수정일",
            "type": "DATETIME"
        },
        {
            "id": "AAAA",
            "removeTag": "true",
            "name": "sss",
            "type": "aaa",
            "size": "2"
        }
    ],
    "field-index-list": [
        {
            "id": "SECTIONCODE",
            "field": "SECTIONCODE",
            "name": "SectionCode_index"
        },
        {
            "id": "REGDATE",
            "field": "REGDATE",
            "name": "RegDate_index"
        },
        {
            "id": "UPDATEDATE",
            "field": "UPDATEDATE",
            "name": "UpdateDate_index"
        }
    ],
    "primary-key": [{"ref": "CODE"}],
    "analyzer-list": [
        {
            "id": "KOREAN",
            "max_pool_size": "100",
            "class": "org.fastcatsearch.plugin.analysis.ko.standard.StandardKoreanAnalyzer",
            "core_pool_size": "10"
        },
        {
            "id": "KEYWORD",
            "max_pool_size": "100",
            "class": "org.apache.lucene.analysis.core.KeywordAnalyzer",
            "core_pool_size": "0"
        }
    ]
}
 * 
 * */
public class SchemaSettingUtil {

	private static final Logger logger = LoggerFactory.getLogger(SchemaSettingUtil.class);

	public static SchemaSetting convertSchemaSetting(JSONObject object) throws SchemaInvalidateException {

		SchemaSetting schemaSetting = new SchemaSetting();

		schemaSetting.setFieldSettingList(parseFieldSettingList(object.optJSONArray("field-list")));
		schemaSetting.setPrimaryKeySetting(parsePrimaryKeySetting(object.optJSONArray("primary-key")));
		schemaSetting.setAnalyzerSettingList(parseAnalyzerSettingList(object.optJSONArray("analyzer-list")));
		schemaSetting.setIndexSettingList(parseIndexSettingList(object.optJSONArray("index-list")));
		schemaSetting.setFieldIndexSettingList(parseFieldIndexSettingList(object.optJSONArray("field-index-list")));
		schemaSetting.setGroupIndexSettingList(parseGroupSettingList(object.optJSONArray("group-index-list")));

		return schemaSetting;
	}

	private static List<FieldSetting> parseFieldSettingList(JSONArray array) throws SchemaInvalidateException {
		List<FieldSetting> fieldSettingList = new ArrayList<FieldSetting>();

		if (array != null) {
			String fieldId = "";
			String attributeId = "";
			String value = "";
			Exception ex = null;
			int inx = 0;
			try {
				for (inx = 0; inx < array.length(); inx++) {
					FieldSetting setting = new FieldSetting();
					fieldId = setting.getId();
					JSONObject data = array.optJSONObject(inx);
					setting.setId(value = data.optString(attributeId = "id").trim());
					setting.setName(value = data.optString(attributeId = "name"));
					setting.setType(Type.valueOf(value = data.optString(attributeId = "type", "").toUpperCase()));
					setting.setSize(data.optInt(attributeId = "size", 0));
					setting.setStore("true".equals(value = data.optString(attributeId = "store")));
                    setting.setSource(value = data.optString(attributeId = "source"));
                    setting.setRemoveTag("true".equals(value = data.optString(attributeId = "removeTag")));
					setting.setMultiValue("true".equals(value = data.optString(attributeId = "multiValue")));
					setting.setMultiValueDelimiter(value = data.optString(attributeId = "multiValueDelimiter", null));
					fieldSettingList.add(setting);
				}
			} catch (NumberFormatException e) {
				ex = e; // Integer.parseInt
			} catch (IllegalArgumentException e) {
				ex = e; // Type.valueOf
			} catch (NullPointerException e) {
				ex = e; // json object null
			} finally {
				if (ex != null) {
					logger.debug("attributeId:{}, value:{}", attributeId, value);
					logger.error("", ex);
					throw new SchemaInvalidateException("fieldSetting", fieldId, attributeId, value, ex.getMessage());
				}
			}
		}
		return fieldSettingList;
	}

	private static PrimaryKeySetting parsePrimaryKeySetting(JSONArray array) throws SchemaInvalidateException {
		PrimaryKeySetting setting = new PrimaryKeySetting();

		if (array != null) {
			List<RefSetting> fieldList = new ArrayList<RefSetting>();
			for (int inx = 0; inx < array.length(); inx++) {
				JSONObject data = array.optJSONObject(inx);
				RefSetting ref = new RefSetting();
				ref.setRef(data.optString("ref").trim());
				fieldList.add(ref);
			}
			setting.setFieldList(fieldList);
		}

		return setting;
	}

	private static List<AnalyzerSetting> parseAnalyzerSettingList(JSONArray array) throws SchemaInvalidateException {
		List<AnalyzerSetting> analyzerSettingList = new ArrayList<AnalyzerSetting>();
		if(array != null) {
			for (int inx = 0; inx < array.length(); inx++) {
				AnalyzerSetting setting = new AnalyzerSetting();
				JSONObject data = array.optJSONObject(inx);
				setting.setId(data.optString("id").trim());
				setting.setClassName(data.optString("class").trim());
				setting.setMaximumPoolSize(data.optInt("maximumPoolSize"));
				setting.setCorePoolSize(data.optInt("corePoolSize"));
				analyzerSettingList.add(setting);
			}
		}
		return analyzerSettingList;
	}

	private static List<IndexSetting> parseIndexSettingList(JSONArray array) throws SchemaInvalidateException {
		List<IndexSetting> indexSettingList = new ArrayList<IndexSetting>();
		if (array != null) {
			for (int inx = 0; inx < array.length(); inx++) {
				IndexSetting setting = new IndexSetting();
				JSONObject data = array.optJSONObject(inx);

				setting.setId(data.optString("id").trim());
				setting.setName(data.optString("name"));
				setting.setQueryAnalyzer(data.optString("queryAnalyzer"));

				setting.setStorePosition("true".equals(data.optString("storePosition")));
				setting.setIgnoreCase("true".equals(data.optString("ignoreCase")));
				setting.setPositionIncrementGap(data.optInt("pig"));
                setting.setNoAdditional("true".equals(data.optString("noAdditional")));

				List<IndexRefSetting> fieldList = new ArrayList<IndexRefSetting>();
				String[] refArray = data.optString("refList").split("\n");
				String[] indexAnalyzerArray = data.optString("indexAnalyzer").split("\n");

				for (int refInx = 0; refInx < refArray.length; refInx++) {
					IndexRefSetting ref = new IndexRefSetting(refArray[refInx].trim(), indexAnalyzerArray[refInx].trim());
					fieldList.add(ref);
				}
				
				setting.setFieldList(fieldList);

				indexSettingList.add(setting);
			}
		}
		return indexSettingList;
	}

	private static List<FieldIndexSetting> parseFieldIndexSettingList(JSONArray array) throws SchemaInvalidateException {
		List<FieldIndexSetting> fieldIndexSettingList = new ArrayList<FieldIndexSetting>();

//		// 필드 인덱스는 필수가 아님
//		if (array.length() == 1 && "".equals(array.optJSONObject(0).optString("id"))) {
//			return null;
//		}
		if(array != null) {
			for (int inx = 0; inx < array.length(); inx++) {
				FieldIndexSetting setting = new FieldIndexSetting();
				JSONObject data = array.optJSONObject(inx);
				setting.setId(data.optString("id").trim());
				setting.setName(data.optString("name"));
				setting.setRef(data.optString("field").trim());
				setting.setSize(data.optInt("size"));
				setting.setIgnoreCase("true".equals(data.optString("ignoreCase")));
				fieldIndexSettingList.add(setting);
			}
		}
		return fieldIndexSettingList;
	}

	private static List<GroupIndexSetting> parseGroupSettingList(JSONArray array) throws SchemaInvalidateException {
		List<GroupIndexSetting> groupIndexesSettingList = new ArrayList<GroupIndexSetting>();

//		// 그룹 인덱스는 필수가 아님
//		if (array.length() == 1 && "".equals(array.optJSONObject(0).optString("id"))) {
//			return null;
//		}
		if (array != null) {
			for (int inx = 0; inx < array.length(); inx++) {
				GroupIndexSetting setting = new GroupIndexSetting();
				JSONObject data = array.optJSONObject(inx);
				setting.setId(data.optString("id").trim());
				setting.setName(data.optString("name"));
				setting.setRef(data.optString("ref").trim());
				setting.setIgnoreCase("true".equals(data.optString("ignoreCase")));
				groupIndexesSettingList.add(setting);
			}
		}
		return groupIndexesSettingList;
	}
}
