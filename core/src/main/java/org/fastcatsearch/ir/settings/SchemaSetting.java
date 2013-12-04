package org.fastcatsearch.ir.settings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@XmlRootElement(name = "schema")
@XmlType(propOrder = { "fieldSettingList", "primaryKeySetting", "indexSettingList", "analyzerSettingList", "fieldIndexSettingList", "groupIndexSettingList" })
public class SchemaSetting {
	
	private static final Logger logger = LoggerFactory.getLogger(SchemaSetting.class);
	
	private static final Integer MAXIMUM_PRIMARY_SIZE = 255;
	
	private List<FieldSetting> fieldSettingList;
	private PrimaryKeySetting primaryKeySetting;
	private List<IndexSetting> indexSettingList;
	private List<AnalyzerSetting> analyzerSettingList;
	private List<FieldIndexSetting> fieldIndexSettingList;
	private List<GroupIndexSetting> groupIndexSettingList;
	
	public String toString(){
		return "[SchemaSetting] fl="+fieldSettingList+", pk="+primaryKeySetting+", idx="+indexSettingList+", an="+analyzerSettingList+", fidx="+fieldIndexSettingList+", grp="+groupIndexSettingList;
	}
	
	public SchemaSetting(){
		fieldSettingList = new ArrayList<FieldSetting>();
		primaryKeySetting = new PrimaryKeySetting();
		indexSettingList = new ArrayList<IndexSetting>();
		analyzerSettingList = new ArrayList<AnalyzerSetting>();
		fieldIndexSettingList = new ArrayList<FieldIndexSetting>();
		groupIndexSettingList = new ArrayList<GroupIndexSetting>();
	}
	
	//@XmlElement는 FieldSetting에서 @XmlRootElement로 선언한 것과 별개로 다시 선언해주어야 한다. 여기서는 field가 root가 아니므로.
	@XmlElementWrapper(name="field-list")
	@XmlElement(name="field")
	public List<FieldSetting> getFieldSettingList() {
		return fieldSettingList;
	}
	public void setFieldSettingList(List<FieldSetting> fieldSettingList) {
		this.fieldSettingList = fieldSettingList;
	}
	
	@XmlElementWrapper(name="index-list")
	@XmlElement(name="index")
	public List<IndexSetting> getIndexSettingList() {
		return indexSettingList;
	}
	public void setIndexSettingList(List<IndexSetting> indexSettingList) {
		this.indexSettingList = indexSettingList;
	}
	
	@XmlElementWrapper(name="analyzer-list")
	@XmlElement(name="analyzer")
	public List<AnalyzerSetting> getAnalyzerSettingList() {
		return analyzerSettingList;
	}
	public void setAnalyzerSettingList(List<AnalyzerSetting> analyzerSettingList) {
		this.analyzerSettingList = analyzerSettingList;
	}
	
	@XmlElementWrapper(name="field-index-list")
	@XmlElement(name="field-index")
	public List<FieldIndexSetting> getFieldIndexSettingList() {
		return fieldIndexSettingList;
	}
	public void setFieldIndexSettingList(List<FieldIndexSetting> fieldIndexSettingList) {
		this.fieldIndexSettingList = fieldIndexSettingList;
	}
	
	@XmlElementWrapper(name="group-index-list")
	@XmlElement(name="group-index")
	public List<GroupIndexSetting> getGroupIndexSettingList() {
		return groupIndexSettingList;
	}
	public void setGroupIndexSettingList(List<GroupIndexSetting> groupSettingList) {
		this.groupIndexSettingList = groupSettingList;
	}
	
	@XmlElement(name="primary-key")
	public PrimaryKeySetting getPrimaryKeySetting() {
		return primaryKeySetting;
	}
	public void setPrimaryKeySetting(PrimaryKeySetting primaryKeySetting) {
		this.primaryKeySetting = primaryKeySetting;
	}
	
	public static final String NULL_OR_BLANK = "NULL_OR_BLANK";
	public static final String DUPLICATED = "DUPLICATED";
	public static final String UNDERFLOW = "UNDERFLOW";
	public static final String NO_PRIMARY_KEY = "NO_PRIMARY_KEY";
	public static final String FIELD_NOT_FOUND = "FIELD_NOT_FOUND";
	public static final String NO_CLASS = "NO_CLASS";
	public static final String OVERFLOW = "OVERFLOW";
	public static final String NO_DATA = "NO_DATA";
	private static final String NO_SUCH_VALUE = "NO_SUCH_VALUE";
	
	
	public void isValid() throws SchemaInvalidateException {
		
		String section = "";
		String fieldName = "";
		String value = "";
		String message = null;
		Exception ex = null;
		int inx=0;
		
	
		//field-list : 1. field type이 올바른지 확인. 2. 옵션이 올바른지 확인.
		try {
			section = FieldSetting.class.getName();
			Set<String> idDupCheckSet = new HashSet<String>();
			
			if(fieldSettingList==null || fieldSettingList.size()==0) {
				message=NO_DATA;
			}
			
			for(inx=0;message==null && inx<fieldSettingList.size();inx++) {
				FieldSetting setting = fieldSettingList.get(inx);
				
				fieldName="id";
				value=setting.getId();
				if(value==null || "".equals(value)) {
					message=NULL_OR_BLANK;
					break;
				} else if(idDupCheckSet.contains(value)) {
					message = DUPLICATED;
					break;
				} else {
					idDupCheckSet.add(value);
				}
				
				fieldName="name";
				value=setting.getName();
				if(value==null || "".equals(value)) {
					message = NULL_OR_BLANK;
					break;
				}
				
				//type은 parse에서 미리 체크됨.
				
				//size 는 null 이 아닌 경우에만 체크. (null, 0 은 제한없음)
				if(setting.getSize() != null && setting.getSize()!=0) {
					fieldName="size";
					value=String.valueOf(setting.getSize());
					if(setting.getSize()!=0 && setting.getSize() < 1) {
						message = UNDERFLOW;
					} 
				}
				
				//isStore / isRemoveTag / isMultiValue 등 불리언 필드는 체크할 필요가 없음.
				
				if(setting.isMultiValue()) {
					fieldName = "multiValueDelimiter";
					value = setting.getMultiValueDelimiter();
					
					if(value==null || "".equals(value)) {
						message = NULL_OR_BLANK;
						break;
					}
				}
				
			}
		} finally {
			if(message!=null || ex!=null) {
				if(message==null) { message = ex.getMessage(); }
				throw new SchemaInvalidateException(section,fieldName+"_"+inx,value,message);
			}
		}
	
		//primary-key : 1. ref가 field list에 존재하는지 확인. 2. field type이 고정길이가 맞는지 확인. 
		try {
			section = PrimaryKeySetting.class.getName();
			List<RefSetting> fieldList = primaryKeySetting.getFieldList();
			
			if(fieldList==null || fieldList.size()==0) {
				message = NO_PRIMARY_KEY;
			}
			
			for(inx=0;message==null && inx<fieldList.size();inx++) {
				RefSetting refSetting = fieldList.get(inx);
				fieldName = "ref";
				if(refSetting==null || refSetting.getRef()==null || "".equals(refSetting.getRef())) {
					message=NULL_OR_BLANK;
					break;
				} else {
					//check field exists..
					boolean found = false;
					for(int inx2=0;inx2<fieldSettingList.size();inx2++) {
						FieldSetting setting = fieldSettingList.get(inx2);
						if(setting.getId().equals(refSetting.getRef())) {
							if(setting.getSize()!=null && setting.getSize() > 0) {
								if(setting.getSize() > MAXIMUM_PRIMARY_SIZE) {
									message = OVERFLOW;
									break;
								}
								found = true;
								break;
							}
						}
					}
					if(message!=null) {
						break;
					}
					if(!found) {
						message = FIELD_NOT_FOUND;
						break;
					}
				}
			}
			
		} finally {
			if(message!=null || ex!=null) {
				if(message==null) { message = ex.getMessage(); }
				throw new SchemaInvalidateException(section,fieldName+"_"+inx,value,message);
			}
		}
		
		//analyzer-list : 1. 값이 올바른지 확인.
		try {
			section = AnalyzerSetting.class.getName();
			Set<String> idDupCheckSet = new HashSet<String>();
			
			if(analyzerSettingList==null || analyzerSettingList.size()==0) {
				message = NO_DATA;
			}
			
			for(inx=0;message==null && inx<analyzerSettingList.size();inx++) {
				AnalyzerSetting setting = analyzerSettingList.get(inx);
				
				fieldName="id";
				value=setting.getId();
				if(value==null || "".equals(value)) {
					message = NULL_OR_BLANK;
					break;
				} else if(idDupCheckSet.contains(value)) {
					message = DUPLICATED;
					break;
				} else {
					idDupCheckSet.add(value);
				}
				
				fieldName="class";
				value=setting.getClassName();
				if(value==null || "".equals(value)) {
					message = NULL_OR_BLANK;
					break;
				} else {
					Class.forName(value);
				}
				
				fieldName = "maximumPoolSize";
				value = String.valueOf(setting.getMaximumPoolSize());
				
				if(setting.getMaximumPoolSize()!=0 && 
						setting.getMaximumPoolSize()<1) {
					message = UNDERFLOW;
					break;
				}
				
				fieldName = "corePoolSize";
				value = String.valueOf(setting.getCorePoolSize());
				
				if(setting.getCorePoolSize()!=0 && 
						setting.getCorePoolSize()<1) {
					message = UNDERFLOW;
					break;
				} else if(setting.getCorePoolSize() > setting.getMaximumPoolSize()) {
					message = OVERFLOW;
					break;
				}
					
			}
		} catch (ClassNotFoundException e) {
			message = NO_CLASS;
		} finally {
			if(message!=null || ex!=null) {
				if(message==null) { message = ex.getMessage(); }
				throw new SchemaInvalidateException(section,fieldName+"_"+inx,value,message);
			}
		}
		
		//index-list : 1.ref 가 field list에 존재하는지. 2. indexAnalyzer가 analyzer-list 에 존재하는지.
		try {
			section = IndexSetting.class.getName();
			Set<String> idDupCheckSet = new HashSet<String>();
			
			if(indexSettingList==null || indexSettingList.size()==0) {
				message = NO_DATA;
			}
			
			for(inx=0;message==null && inx<indexSettingList.size();inx++) {
				IndexSetting setting = indexSettingList.get(inx);
				
				fieldName="id";
				value=setting.getId();
				if(value==null || "".equals(value)) {
					message = NULL_OR_BLANK;
					break;
				} else if(idDupCheckSet.contains(value)) {
					message = DUPLICATED;
					break;
				} else {
					idDupCheckSet.add(value);
				}
				
				fieldName="name";
				value=setting.getName();
				if(value==null || "".equals(value)) {
					message = NULL_OR_BLANK;
					break;
				}
				
				fieldName="indexAnalyzer";
				value=setting.getIndexAnalyzer();
				boolean found = false;
				for(int inx2=0;inx2<analyzerSettingList.size();inx2++) {
					AnalyzerSetting analyzer = analyzerSettingList.get(inx2);
					if(analyzer!=null && value.equalsIgnoreCase(analyzer.getId())) {
						found = true;
						break;
					}
				}
				if(!found) {
					message = NO_SUCH_VALUE;
					break;
				}
				
				fieldName="queryAnalyzer";
				value=setting.getQueryAnalyzer();
				found = false;
				for(int inx2=0;inx2<analyzerSettingList.size();inx2++) {
					AnalyzerSetting analyzer = analyzerSettingList.get(inx2);
					if(analyzer!=null && value.equalsIgnoreCase(analyzer.getId())) {
						found = true;
						break;
					}
				}
				if(!found) {
					message = NO_SUCH_VALUE;
					break;
				}
				
				fieldName="positionIncrementGap";
				value=String.valueOf(setting.getPositionIncrementGap());
				
				if(setting.getPositionIncrementGap()!=null 
						&& setting.getPositionIncrementGap()!=0 
						&& setting.getPositionIncrementGap() < 1) {
					message = UNDERFLOW;
					break;
				}
				
			}
		} finally {
			if(message!=null || ex!=null) {
				if(message==null) { message = ex.getMessage(); }
				throw new SchemaInvalidateException(section,fieldName+"_"+inx,value,message);
			}
		}
		
		//field-index-list : 1. ref가 field list에 존재하는지
		try {
			section = FieldIndexSetting.class.getName();
			Set<String> idDupCheckSet = new HashSet<String>();
			
			//fieldindex는 반드시 존재할 필요는 없음.
			for(inx=0;message==null && fieldIndexSettingList!=null && 
					inx<fieldIndexSettingList.size();inx++) {
				FieldIndexSetting setting = fieldIndexSettingList.get(inx);
				
				fieldName="id";
				value=setting.getId();
				if(value==null || "".equals(value)) {
					message=NULL_OR_BLANK;
					break;
				} else if(idDupCheckSet.contains(value)) {
					message=DUPLICATED;
					break;
				} else {
					idDupCheckSet.add(value);
				}
				
				fieldName="name";
				value=setting.getName();
				if(value==null || "".equals(value)) {
					message=NULL_OR_BLANK;
					break;
				}
				
				fieldName="ref";
				value=setting.getRef();
				if(value==null || "".equals(value)) {
					message=NULL_OR_BLANK;
				} else {
					boolean found=false;
					for(int inx2=0;inx2<fieldSettingList.size();inx2++) {
						if(value.equals(fieldSettingList.get(inx2).getId())) {
							found = true;
							break;
						}
					}
					if(!found) {
						message=FIELD_NOT_FOUND;
						break;
					}
				}
				
				fieldName="size";
				value=String.valueOf(setting.getSize());
				if(setting.getSize()!=null 
						&& setting.getSize()!=0
						&& setting.getSize() < 1) {
					message=UNDERFLOW;
					break;
				}
			}
			
		} finally {
			if(message!=null || ex!=null) {
				if(message==null) { message = ex.getMessage(); }
				throw new SchemaInvalidateException(section,fieldName+"_"+inx,value,message);
			}
		}
		
		// group-index-list : 1. ref가 field list에 존재하는지
		try {
			section = GroupIndexSetting.class.getName();
			Set<String> idDupCheckSet = new HashSet<String>();
			
			
			//groupindex는 반드시 존재할 필요는 없음.
			for(inx=0;message==null && groupIndexSettingList!=null && 
					inx<groupIndexSettingList.size();inx++) {
				
				GroupIndexSetting setting = groupIndexSettingList.get(inx);
				
				fieldName="id";
				value=setting.getId();
				
				if(value==null || "".equals(value)) {
					message=NULL_OR_BLANK;
					break;
				} else if(idDupCheckSet.contains(value)) {
					message=DUPLICATED;
					break;
				} else {
					idDupCheckSet.add(value);
				}
				
				fieldName="name";
				value=setting.getName();
				if(value==null || "".equals(value)) {
					message=NULL_OR_BLANK;
					break;
				}
				
				fieldName="ref";
				value=setting.getRef();
				if(value==null || "".equals(value)) {
					message=NULL_OR_BLANK;
					break;
				}
				
				fieldName="ref";
				value=setting.getRef();
				if(value==null || "".equals(value)) {
					message=NULL_OR_BLANK;
				} else {
					boolean found=false;
					for(int inx2=0;inx2<fieldSettingList.size();inx2++) {
						if(value.equals(fieldSettingList.get(inx2).getId())) {
							found = true;
							break;
						}
					}
					if(!found) {
						message=FIELD_NOT_FOUND;
						break;
					}
				}
			}
			
		} finally {
			if(message!=null || ex!=null) {
				if(message==null) { message = ex.getMessage(); }
				throw new SchemaInvalidateException(section,fieldName+"_"+inx,value,message);
			}
		}
		
		logger.debug("Schema All OK");
	}
}
