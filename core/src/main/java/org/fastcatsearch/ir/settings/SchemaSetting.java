package org.fastcatsearch.ir.settings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fastcatsearch.ir.settings.FieldSetting.Type;
import org.fastcatsearch.util.DynamicClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement(name = "schema")
@XmlType(propOrder = { "fieldSettingList", "primaryKeySetting", "indexSettingList", "analyzerSettingList", "fieldIndexSettingList", "groupIndexSettingList" })
public class SchemaSetting {

	private static final Logger logger = LoggerFactory.getLogger(SchemaSetting.class);

	private static final Integer MAXIMUM_PRIMARY_SIZE = 128;

	private List<FieldSetting> fieldSettingList;
	private PrimaryKeySetting primaryKeySetting;
	private List<IndexSetting> indexSettingList;
	private List<AnalyzerSetting> analyzerSettingList;
	private List<FieldIndexSetting> fieldIndexSettingList;
	private List<GroupIndexSetting> groupIndexSettingList;

	public String toString() {
		return "[SchemaSetting] fl=" + fieldSettingList + ", pk=" + primaryKeySetting + ", idx=" + indexSettingList + ", an=" + analyzerSettingList + ", fidx="
				+ fieldIndexSettingList + ", grp=" + groupIndexSettingList;
	}

	public SchemaSetting() {
		fieldSettingList = new ArrayList<FieldSetting>();
		primaryKeySetting = new PrimaryKeySetting();
		indexSettingList = new ArrayList<IndexSetting>();
		analyzerSettingList = new ArrayList<AnalyzerSetting>();
		fieldIndexSettingList = new ArrayList<FieldIndexSetting>();
		groupIndexSettingList = new ArrayList<GroupIndexSetting>();
	}

	// @XmlElement는 FieldSetting에서 @XmlRootElement로 선언한 것과 별개로 다시 선언해주어야 한다.
	// 여기서는 field가 root가 아니므로.
	@XmlElementWrapper(name = "field-list")
	@XmlElement(name = "field")
	public List<FieldSetting> getFieldSettingList() {
		return fieldSettingList;
	}

	public void setFieldSettingList(List<FieldSetting> fieldSettingList) {
		this.fieldSettingList = fieldSettingList;
	}

	@XmlElementWrapper(name = "index-list")
	@XmlElement(name = "index")
	public List<IndexSetting> getIndexSettingList() {
		return indexSettingList;
	}

	public void setIndexSettingList(List<IndexSetting> indexSettingList) {
		this.indexSettingList = indexSettingList;
	}

	@XmlElementWrapper(name = "analyzer-list")
	@XmlElement(name = "analyzer")
	public List<AnalyzerSetting> getAnalyzerSettingList() {
		return analyzerSettingList;
	}

	public void setAnalyzerSettingList(List<AnalyzerSetting> analyzerSettingList) {
		this.analyzerSettingList = analyzerSettingList;
	}

	@XmlElementWrapper(name = "field-index-list")
	@XmlElement(name = "field-index")
	public List<FieldIndexSetting> getFieldIndexSettingList() {
		return fieldIndexSettingList;
	}

	public void setFieldIndexSettingList(List<FieldIndexSetting> fieldIndexSettingList) {
		this.fieldIndexSettingList = fieldIndexSettingList;
	}

	@XmlElementWrapper(name = "group-index-list")
	@XmlElement(name = "group-index")
	public List<GroupIndexSetting> getGroupIndexSettingList() {
		return groupIndexSettingList;
	}

	public void setGroupIndexSettingList(List<GroupIndexSetting> groupSettingList) {
		this.groupIndexSettingList = groupSettingList;
	}

	@XmlElement(name = "primary-key")
	public PrimaryKeySetting getPrimaryKeySetting() {
		return primaryKeySetting;
	}

	public void setPrimaryKeySetting(PrimaryKeySetting primaryKeySetting) {
		this.primaryKeySetting = primaryKeySetting;
	}

	public static final String NULL_OR_EMPTY = "is empty";
	public static final String DUPLICATED = "is duplicated";
	public static final String NEGATIVE = "must be 0 or positive number";
	public static final String NOT_POSITIVE = "must be positive number";
	public static final String FIELD_NOT_FOUND = "is not found field";
	private static final String NO_SUCH_VALUE = "is not found value";
	private static final String NOT_SUITABLE = "is not suitable";

	public void isValid() throws SchemaInvalidateException {

		String section = "";
		String fieldId = "";
		String attributeId = "";
		
		String value = "";
		String type = null;
		Exception ex = null;
		int inx = 0;

		// field-list : 1. field type이 올바른지 확인. 2. 옵션이 올바른지 확인.
		try {
			section = "Fields";
			Set<String> idDupCheckSet = new HashSet<String>();

			for (inx = 0; fieldSettingList != null && inx < fieldSettingList.size(); inx++) {
				FieldSetting setting = fieldSettingList.get(inx);
				fieldId = setting.getId();
				attributeId = "ID";
				value = setting.getId();
				if (value == null || "".equals(value)) {
					type = NULL_OR_EMPTY;
					break;
				} else if (idDupCheckSet.contains(value)) {
					type = DUPLICATED;
					break;
				} else {
					idDupCheckSet.add(value);
				}

				attributeId = "Name";
				value = setting.getName();
				if (value == null || "".equals(value)) {
					type = NULL_OR_EMPTY;
					break;
				}

				// type은 parse에서 미리 체크됨.

				// size 는 null 이 아닌 경우에만 체크. (null, 0 은 제한없음)
				if (setting.getSize() != null && setting.getSize() != 0) {
					attributeId = "size";
					value = String.valueOf(setting.getSize());
					if (setting.getSize() < 0) {
						type = NEGATIVE;
						break;
					}
				}

				// isStore / isRemoveTag / isMultiValue 등 불리언 필드는 체크할 필요가 없음.

				if (setting.isMultiValue()) {
					attributeId = "multiValueDelimiter";
					value = setting.getMultiValueDelimiter();

					if (value == null || "".equals(value)) {
						type = NULL_OR_EMPTY;
						break;
					}
				}

			}
		} finally {
			if (type != null || ex != null) {
				if (type == null) {
					type = ex.getMessage();
				}
				throw new SchemaInvalidateException(section, fieldId, attributeId, value, type);
			}
		}

		// primary-key : 1. ref가 field list에 존재하는지 확인. 2. field type이 고정길이가 맞는지
		// 확인.
		try {
			section = "Primary Keys";
			List<RefSetting> fieldList = primaryKeySetting.getFieldList();

			if (fieldList != null && fieldList.size() > 0) {
				fieldId = "PRIMARY KEY";
				for (inx = 0; type == null && inx < fieldList.size(); inx++) {
					RefSetting setting = fieldList.get(inx);
					attributeId = "field";
					value = setting.getRef();
					if (value == null || "".equals(value)) {
						type = NULL_OR_EMPTY;
						break;
					} else {
						// check field exists..
						boolean found = false;
						for (int inx2 = 0; inx2 < fieldSettingList.size(); inx2++) {
							FieldSetting fieldSetting = fieldSettingList.get(inx2);
							if (value.equals(fieldSetting.getId())) {

								//string일때만 길이체크를 한다.
								if (fieldSetting.getType() == Type.STRING || fieldSetting.getType() == Type.ASTRING) {
									if (fieldSetting.getSize() != null) {
										
										if(fieldSetting.getSize() > 0) {
											if(fieldSetting.getSize() <= MAXIMUM_PRIMARY_SIZE) {
												found = true;
												break;
											}else{
												type = "is larger than maximum primary key field size " + MAXIMUM_PRIMARY_SIZE;
												break;
											}
										}else if(fieldSetting.getSize() == 0) {
											//가변길이.
											type = "must be fixed size field";
											break;
										} else {
											type = NOT_POSITIVE;
											break;
										}
									}
								}else{
									found = true;
									break;
								}
							}

						}
						if (type != null) {
							break;
						}
						if (!found) {
							type = FIELD_NOT_FOUND;
							break;
						}
					}
				}

			}
		} finally {
			if (type != null || ex != null) {
				if (type == null) {
					type = ex.getMessage();
				}
				throw new SchemaInvalidateException(section, fieldId, attributeId, value, type);
			}
		}
		

		// analyzer-list : 1. 값이 올바른지 확인.
		try {
			section = "Analyzers";
			Set<String> idDupCheckSet = new HashSet<String>();

			for (inx = 0; analyzerSettingList != null && inx < analyzerSettingList.size(); inx++) {
				AnalyzerSetting setting = analyzerSettingList.get(inx);
				fieldId = setting.getId();
				attributeId = "ID";
				value = setting.getId();
				if (value == null || "".equals(value)) {
					type = NULL_OR_EMPTY;
					break;
				} else if (idDupCheckSet.contains(value)) {
					type = DUPLICATED;
					break;
				} else {
					idDupCheckSet.add(value);
				}

				//className validation 은 server 패키지에서만 수행할 수 있다.

				attributeId = "maximumPoolSize";
				value = String.valueOf(setting.getMaximumPoolSize());

				if (setting.getMaximumPoolSize() < 0) {
					type = NEGATIVE;
					break;
				}

				attributeId = "corePoolSize";
				value = String.valueOf(setting.getCorePoolSize());

				if (setting.getCorePoolSize() < 0) {
					type = NEGATIVE;
					break;
				} else if (setting.getCorePoolSize() > setting.getMaximumPoolSize()) {
					type = "is larger than maximum pool size";
					break;
				}

			}
		} finally {
			if (type != null || ex != null) {
				if (type == null) {
					type = ex.getMessage();
				}
				throw new SchemaInvalidateException(section, fieldId, attributeId, value, type);
			}
		}

		// index-list : 1.ref 가 field list에 존재하는지. 2. indexAnalyzer가
		// analyzer-list 에 존재하는지.
		try {
			section = "Search Indexes";
			Set<String> idDupCheckSet = new HashSet<String>();

			OUTTER: for (inx = 0; indexSettingList != null && inx < indexSettingList.size(); inx++) {
				IndexSetting setting = indexSettingList.get(inx);
				fieldId = setting.getId();
				attributeId = "ID";
				value = setting.getId();
				if (value == null || "".equals(value)) {
					type = NULL_OR_EMPTY;
					break;
				} else if (idDupCheckSet.contains(value)) {
					type = DUPLICATED;
					break;
				} else {
					idDupCheckSet.add(value);
				}

				attributeId = "name";
				value = setting.getName();
				if (value == null || "".equals(value)) {
					type = NULL_OR_EMPTY;
					break;
				}

				attributeId = "field";
				List<IndexRefSetting> fieldList = setting.getFieldList();
				for (int i = 0; fieldList != null && i < fieldList.size(); i++) {
					IndexRefSetting refSetting = fieldList.get(i);
//					attributeId += ">ref";
					value = refSetting.getRef();
					if (value == null || "".equals(value)) {
						type = NULL_OR_EMPTY;
						break;
					}else{
						boolean found = false;
						for (int inx2 = 0; inx2 < fieldSettingList.size(); inx2++) {
							FieldSetting fieldSetting = fieldSettingList.get(inx2);
							if (value.equals(fieldSetting.getId())) {
								found = true;
							}
						}
						if (!found) {
							type = FIELD_NOT_FOUND;
							break OUTTER;
						}
					}
					
					
					value = refSetting.getIndexAnalyzer();
					boolean found = false;
					for (int inx2 = 0; inx2 < analyzerSettingList.size(); inx2++) {
						AnalyzerSetting analyzer = analyzerSettingList.get(inx2);
						if (analyzer != null && value.equalsIgnoreCase(analyzer.getId())) {
							found = true;
							break;
						}
					}
					if (!found) {
						attributeId = "field>indexAnalyzer";
						type = NO_SUCH_VALUE;
						break;
					}
				}
					
				boolean found = false;
					
//				attributeId = "indexAnalyzer";
//				value = setting.getIndexAnalyzer();
//				for (int inx2 = 0; inx2 < analyzerSettingList.size(); inx2++) {
//					AnalyzerSetting analyzer = analyzerSettingList.get(inx2);
//					if (analyzer != null && value.equalsIgnoreCase(analyzer.getId())) {
//						found = true;
//						break;
//					}
//				}
//				if (!found) {
//					type = NO_SUCH_VALUE;
//					break;
//				}

				attributeId = "queryAnalyzer";
				value = setting.getQueryAnalyzer();
				found = false;
				for (int inx2 = 0; inx2 < analyzerSettingList.size(); inx2++) {
					AnalyzerSetting analyzer = analyzerSettingList.get(inx2);
					if (analyzer != null && value.equalsIgnoreCase(analyzer.getId())) {
						found = true;
						break;
					}
				}
				if (!found) {
					type = NO_SUCH_VALUE;
					break;
				}

				attributeId = "positionIncrementGap";
				value = String.valueOf(setting.getPositionIncrementGap());

				if (setting.getPositionIncrementGap() != null && setting.getPositionIncrementGap() < 0) {
					type = NEGATIVE;
					break;
				}

			}
		} finally {
			if (type != null || ex != null) {
				if (type == null) {
					type = ex.getMessage();
				}
				throw new SchemaInvalidateException(section, fieldId, attributeId, value, type);
			}
		}

		// field-index-list : 1. ref가 field list에 존재하는지
		try {
			section = "Field Indexes";
			Set<String> idDupCheckSet = new HashSet<String>();

			// fieldindex는 반드시 존재할 필요는 없음.
			for (inx = 0; type == null && fieldIndexSettingList != null && inx < fieldIndexSettingList.size(); inx++) {
				FieldIndexSetting setting = fieldIndexSettingList.get(inx);
				fieldId = setting.getId();
				attributeId = "ID";
				value = setting.getId();
				if (value == null || "".equals(value)) {
					type = NULL_OR_EMPTY;
					break;
				} else if (idDupCheckSet.contains(value)) {
					type = DUPLICATED;
					break;
				} else {
					idDupCheckSet.add(value);
				}

				attributeId = "name";
				value = setting.getName();
				if (value == null || "".equals(value)) {
					type = NULL_OR_EMPTY;
					break;
				}

				attributeId = "field";
				value = setting.getRef();
				if (value == null || "".equals(value)) {
					type = NULL_OR_EMPTY;
					break;
				} else {
					boolean found = false;
					for (int inx2 = 0; inx2 < fieldSettingList.size(); inx2++) {
						if (value.equals(fieldSettingList.get(inx2).getId())) {
							found = true;
							break;
						}
					}
					if (!found) {
						type = FIELD_NOT_FOUND;
						break;
					}
				}

				//1. 길이를 설정하지 않았다면 field 셋팅에 길이가 설정되어 있는지 확인 .
				Integer fieldIndexSize = setting.getSize();
				if(fieldIndexSize == 0){
					for (int inx2 = 0; inx2 < fieldSettingList.size(); inx2++) {
						FieldSetting fieldSetting = fieldSettingList.get(inx2);
						if (fieldSetting.getId().equals(value)) {
							if(fieldSetting.isVariableField()){
								if(fieldSetting.getSize() == null || fieldSetting.getSize() <= 0){
									type = "must be fixed size field or please set field index size";
								}
							}
							break;
						}
					}
				}else if(fieldIndexSize > 0){
					//2. 길이가 넘치치 않는지.
					for (int inx2 = 0; inx2 < fieldSettingList.size(); inx2++) {
						FieldSetting fieldSetting = fieldSettingList.get(inx2);
						if (fieldSetting.getId().equals(value)) {
							//0(가변길이)가 아닐때만 비교.
							if(fieldSetting.isNumericField()){
								type = "size cannot be set to numeric field";
							}else if(fieldSetting.getSize() != null && fieldSetting.getSize() != 0 && fieldIndexSize > fieldSetting.getSize()){
								type = "is larger than field size " + fieldSetting.getSize();
							}
							break;
						}
					}
				}
				
				if (type != null) {
					break;
				}
				
				attributeId = "size";
				value = String.valueOf(setting.getSize());
				if (setting.getSize() < 0) {
					type = NEGATIVE;
					break;
				}
				
			}

		} finally {
			if (type != null || ex != null) {
				if (type == null) {
					type = ex.getMessage();
				}
				throw new SchemaInvalidateException(section, fieldId, attributeId, value, type);
			}
		}

		// group-index-list : 1. ref가 field list에 존재하는지
		try {
			section = "Group Indexes";
			Set<String> idDupCheckSet = new HashSet<String>();

			// groupindex는 반드시 존재할 필요는 없음.
			for (inx = 0; type == null && groupIndexSettingList != null && inx < groupIndexSettingList.size(); inx++) {

				GroupIndexSetting setting = groupIndexSettingList.get(inx);
				fieldId = setting.getId();
				attributeId = "ID";
				value = setting.getId();

				if (value == null || "".equals(value)) {
					type = NULL_OR_EMPTY;
					break;
				} else if (idDupCheckSet.contains(value)) {
					type = DUPLICATED;
					break;
				} else {
					idDupCheckSet.add(value);
				}

				attributeId = "name";
				value = setting.getName();
				if (value == null || "".equals(value)) {
					type = NULL_OR_EMPTY;
					break;
				}

				attributeId = "field";
				value = setting.getRef();
				if (value == null || "".equals(value)) {
					type = NULL_OR_EMPTY;
					break;
				} else {
					boolean found = false;
					for (int inx2 = 0; inx2 < fieldSettingList.size(); inx2++) {
						if (value.equals(fieldSettingList.get(inx2).getId())) {
							found = true;
							break;
						}
					}
					if (!found) {
						type = FIELD_NOT_FOUND;
						break;
					}
				}
			}

		} finally {
			if (type != null || ex != null) {
				if (type == null) {
					type = ex.getMessage();
				}
				throw new SchemaInvalidateException(section, fieldId, attributeId, value, type);
			}
		}
		logger.debug("Schema is valid!");
	}
}
