package org.fastcatsearch.env;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.ir.config.ColumnSetting;
import org.fastcatsearch.ir.config.DataSourceSetting;
import org.fastcatsearch.ir.config.FieldSetting;
import org.fastcatsearch.ir.config.FilterSetting;
import org.fastcatsearch.ir.config.GroupSetting;
import org.fastcatsearch.ir.config.IRConfig;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.ir.config.IndexSetting;
import org.fastcatsearch.ir.config.Schema;
import org.fastcatsearch.ir.config.SettingException;
import org.fastcatsearch.ir.config.SortSetting;
import org.fastcatsearch.ir.config.FieldSetting.Type;
import org.fastcatsearch.settings.Settings;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import static org.fastcatsearch.env.FileNames.*;

public class SettingManager {
	private final Logger logger = LoggerFactory.getLogger(SettingManager.class);
	private Environment environment;
	private static Map<String, Object> settingCache = new HashMap<String, Object>();
	
	
	public SettingManager(Environment environment) {
		this.environment = environment;
	}

	public void applyWorkSchemaFile(String collection) {
		String workFilepath = getKey(collection, schemaWorkFilename);
		File workf = new File(workFilepath);

		if (!workf.exists())
			return;

		String bakFilepath = getKey(collection, schemaFilename + bakupSuffix);
		File bakf = new File(bakFilepath);

		String filepath = getKey(collection, schemaFilename);
		File f = new File(filepath);

		if (bakf.exists())
			bakf.delete();
		if (f.exists())
			f.renameTo(bakf);

		workf.renameTo(f);

	}

	public void initSchema(String collection) {
		String contents = "<schema name=\"" + collection + "\" version=\"1.0\">" + Environment.LINE_SEPARATOR + "</schema>";
		String configFile = getKey(collection, schemaFilename);
		FileOutputStream writer = null;
		try {
			writer = new FileOutputStream(configFile);
			writer.write(contents.getBytes());
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				// ignore
			}

		}
	}

	public Schema getSchema(String collection, boolean reload) throws SettingException {
		Element root = null;
		Schema schema = null;

		if (!reload) {
			schema = (Schema) getFromCache(collection, schemaObject);
			if (schema != null)
				return schema;
		}

		if (!reload) {
			root = (Element) getFromCache(collection, schemaFilename);
		}

		if (root == null)
			root = getXml(collection, schemaFilename);

		if (root == null)
			return null;

		putToCache(collection, schema, schemaObject);

		schema = getSchema0(collection, root);
		putToCache(schema, schemaObject);

		return schema;
	}

	public Schema getWorkSchema(String collection) throws SettingException {
		return getWorkSchema(collection, false, false);
	}

	public Schema getWorkSchema(String collection, boolean reload, boolean create) throws SettingException {
		Element root = null;
		if (reload)
			root = getXml(collection, schemaWorkFilename);

		if (root == null) {
			if (create) {
				String workSchemaFileDir = getKey(collection, schemaWorkFilename);
				String schemaFileDir = getKey(collection, schemaFilename);
				File fworkSchema = new File(workSchemaFileDir);
				try {
					if (!fworkSchema.exists())
						FileUtils.touch(fworkSchema);
					File fschema = new File(schemaFileDir);
					FileUtils.copyFile(fschema, fworkSchema);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
				root = getXml(collection, schemaWorkFilename);
			} else {
				return null;
			}
		}

		return getSchema0(collection, root);
	}

	public int updateWorkSchema(String collection, String key, String value) throws SettingException {
		Element root = null;

		String workSchemaFileDir = getKey(collection, schemaWorkFilename);
		String schemaFileDir = getKey(collection, schemaFilename);
		File fworkSchema = new File(workSchemaFileDir);
		if (fworkSchema.exists()) {
			root = getXml(collection, schemaWorkFilename);
		} else {
			try {
				FileUtils.touch(fworkSchema);
				File fschema = new File(schemaFileDir);
				FileUtils.copyFile(fschema, fworkSchema);
			} catch (IOException e) {
				throw new SettingException(e.getMessage());
			}
			root = getXml(collection, schemaWorkFilename);
		}

		if (root == null)
			return 3;

		return updateWorkSchema0(collection, root, key, value);
	}

	public int addField2WorkSchema(String collection) throws SettingException {
		Element root = null;

		if (root == null) {
			root = getXml(collection, schemaWorkFilename);
		}

		if (root == null)
			return 1;

		return addField2WorkSchema0(collection, root);
	}

	public int deleteField2WorkSchema(String collection, String fieldName) throws SettingException {
		Element root = null;

		if (root == null) {
			root = getXml(collection, schemaWorkFilename);
		}

		if (root == null)
			return 1;

		return deleteField2WorkSchema0(collection, root, fieldName);
	}

	public int deleteWorkSchema(String collection) throws SettingException {
		String xmlDir = getKey(collection, schemaWorkFilename);
		File workSchema = new File(xmlDir);
		try {
			FileUtils.forceDelete(workSchema);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return 1;
		}

		return 0;
	}

	public int recoverWorkeSchema(String collection, String fieldName) throws SettingException {
		Element root = null;

		if (root == null)
			root = getXml(collection, schemaWorkFilename);

		return recoverWorkSchema0(collection, root, fieldName);
	}

	public int isChange(String collection, String key) throws SettingException {
		String posStr = key.substring(0, 2);
		int pos = 0;
		if (posStr.startsWith("0")) {
			pos = Integer.parseInt(key.substring(1, 2));
		} else {
			pos = Integer.parseInt(posStr);
		}
		String fieldName = key.substring(2);
		String attrName = "";
		switch (pos) {
		case 1:
			attrName = "name";
			break;
		case 2:
			attrName = "primary";
			break;
		case 3:
			attrName = "type";
			break;
		case 4:
			attrName = "size";
			break;
		case 5:
			attrName = "index";
			break;
		case 6:
			attrName = "sort";
			break;
		case 7:
			attrName = "sortSize";
			break;
		case 8:
			attrName = "group";
			break;
		case 9:
			attrName = "filter";
			break;
		case 10:
			attrName = "store";
			break;
		case 11:
			attrName = "normalize";
			break;
		case 12:
			attrName = "virtual";
			break;
		case 13:
			attrName = "modify";
			break;
		case 14:
			attrName = "tagRemove";
			break;
		case 15:
			attrName = "multiValue";
			break;
		default:
			break;
		}

		int tag = 0;
		Element schemaRoot = null;
		Element workSchemaRoot = null;
		Element schemaElement = null;
		Element workSchemaElement = null;
		if (schemaRoot == null)
			schemaRoot = getXml(collection, schemaFilename);
		if (workSchemaRoot == null)
			workSchemaRoot = getXml(collection, schemaWorkFilename);
		List schemaFields = schemaRoot.getChildren("field");
		for (int i = 0; i < schemaFields.size(); i++) {
			Element el = (Element) schemaFields.get(i);
			if (fieldName.equals(el.getAttributeValue("name"))) {
				schemaElement = el;
			}
		}
		List workSchemaFields = workSchemaRoot.getChildren("field");
		for (int i = 0; i < workSchemaFields.size(); i++) {
			Element el = (Element) workSchemaFields.get(i);
			if (fieldName.equals(el.getAttributeValue("name"))) {
				workSchemaElement = el;
			}
		}

		// attrName
		Attribute att = schemaElement.getAttribute(attrName);
		Attribute watt = workSchemaElement.getAttribute(attrName);
		if ((att == null) ^ (watt == null)) {
			tag = 1;
			return tag;
		}
		if (att == null) {
			return tag;
		}
		String schemaAttrValue = schemaElement.getAttribute(attrName).getValue();
		String workSchemaAttrValue = workSchemaElement.getAttribute(attrName).getValue();
		if ((schemaAttrValue == null) ^ (workSchemaAttrValue == null)) {
			tag = 1;
			return tag;
		}
		if (schemaAttrValue == null) {
			return tag;
		}
		if (schemaAttrValue.equals(workSchemaAttrValue)) {
			return tag;
		} else {
			tag = 1;
			return tag;
		}

	}

	private Schema getSchema0(String collection, Element root) throws SettingException {
		Schema schema = new Schema();
		schema.collection = collection;
		List fields = root.getChildren();

		boolean primaryKeyFound = false;

		int indexSequence = 0;
		int groupSequence = 0;
		int sortSequence = 0;
		int filterSequence = 0;
		int columnSequence = 0;
		for (int i = 0; i < fields.size(); i++) {

			Element el = (Element) fields.get(i);

			if (el.getName().equals("field")) {
				/*
				 * 1. field setting
				 */
				String name = el.getAttributeValue("name").toLowerCase();
				schema.fieldnames.put(name, i);

				String type = el.getAttributeValue("type");
				FieldSetting f = new FieldSetting(name, type, i);
				schema.addFieldSetting(f);
				// logger.debug("field =>> {}, {}", name, f);

				String tmp = el.getAttributeValue("size");

				// 여기서 하드코딩하게되면 필드타입을 int에서 achar로 바꿀때 길이가 4인것
				// 처럼 보이게 된다. 하지만 스키마 셋팅에는 -1로 되어있을 경우가 많다.
				// 그러므로 페이지를 재로딩해주어야 하는데 재로딩시 스키마가 잘못되어서 편집을 할수가
				// 없다.
				// 즉, 매번 재로딩을 하게되면 잘못셋팅하는즉시 스키마를 에디트할수가 없게 되기때문에
				// 이 부분을 주석처리한다.
				// 이렇게 되면 관리도구상에서는 int -1 로 보이게 되지만, 가상의 size때문에
				// achar로 변경시 길이를 4로 오해하는 상황을 피할수 있다.
				// if(type.equals("int")){
				// f.size = IOUtil.SIZE_OF_INT;
				// }else if(type.equals("long")){
				// f.size = IOUtil.SIZE_OF_LONG;
				// }else if(type.equals("float")){
				// f.size = IOUtil.SIZE_OF_INT;
				// }else if(type.equals("double")){
				// f.size = IOUtil.SIZE_OF_LONG;
				// }else if(type.equals("datetime")){
				// f.size = IOUtil.SIZE_OF_LONG;
				// }else
				if (tmp != null) {
					try {
						f.size = Integer.parseInt(tmp);
					} catch (NumberFormatException ex) {
						logger.warn("Field size in not a number! name = " + name + ", size = " + tmp);
						f.size = -1;
					}
				} else {
					f.size = -1;
				}
				int fieldSize = f.size;
				boolean isFixed = fieldSize > 0 || type.equals("int") || type.equals("long") || type.equals("float") || type.equals("double") || type.equals("datetime");

				if ("true".equals(el.getAttributeValue("primary"))) {
					f.primary = true;
					schema.setIndexID(i);
					if (primaryKeyFound)
						throw new SettingException("Primary key appears more than once. name = " + name);
					else
						primaryKeyFound = true;

					if (f.type != FieldSetting.Type.Int && f.type != FieldSetting.Type.Long && f.type != FieldSetting.Type.AChar) {
						throw new SettingException("Primary key should be int, long or achar type.");
					}
				}

				if ("false".equals(el.getAttributeValue("store"))) {
					f.store = false;
				}

				if ("true".equals(el.getAttributeValue("virtual"))) {
					f.virtual = true;
				}

				if ("true".equals(el.getAttributeValue("modify"))) {
					f.modify = true;
				}

				if ("true".equals(el.getAttributeValue("normalize"))) {
					f.normalize = true;
				}

				if ("true".equals(el.getAttributeValue("tagRemove"))) {
					f.tagRemove = true;
				}

				if ("true".equals(el.getAttributeValue("column"))) {
					f.column = true;
				}

				if ("true".equals(el.getAttributeValue("multiValue"))) {
					
					if (!isFixed)
						throw new SettingException("다중값 필드는 고정길이어야 합니다. name = " + name + ", fieldSize = " + fieldSize);		
					
					if ( f.type == Type.DateTime )
						throw new SettingException("DateTime 필드는 다중값 필드로 설정 할수 없습니다.  name = " + name + ", fieldSize = " + fieldSize);
					
					f.multiValue = true;
					f.multiValueMaxCount = Short.MAX_VALUE; // 멀티밸류
										// 기본값.
					String delimiter = el.getAttributeValue("multiValueDelimiter");
					if (delimiter != null && delimiter.length() > 0) {
						f.multiValueDelimiter = delimiter.charAt(0);
					}
					String maxCount = el.getAttributeValue("multiValueMaxCount");
					if (maxCount != null && maxCount.length() > 0) {
						try {
							int count = Integer.parseInt(maxCount);
							if (count <= Short.MAX_VALUE) {
								f.multiValueMaxCount = count;
							}
						} catch (Exception e) {
							logger.error("다중값 MaxCount 읽는도중 에러발생.", e);
						}
					}
				}

				/*
				 * 2. index
				 */
				String indexTokenizer = el.getAttributeValue("index");
				if (indexTokenizer != null && indexTokenizer.trim().length() > 0) {
					// Do not make primary key search index.
					if (!f.primary) {
						String queryTokenizer = el.getAttributeValue("query");
						IndexSetting is = new IndexSetting(name, f, indexTokenizer, queryTokenizer);
						schema.addIndexSetting(is);
						f.indexSetting = is;
						schema.indexnames.put(name, indexSequence++);
					} else {
						logger.warn("Primary do not need to make an index!");
					}
				}

				/*
				 * 3. sort
				 */
				if ("true".equals(el.getAttributeValue("sort"))) {
					String sortSize = el.getAttributeValue("sortSize");
					int sSize = -1;
					int sortByteSize = 0;
					if (sortSize == null || sortSize.trim().length() == 0) {
						sortByteSize = f.getByteSize();
					} else {
						try {
							sSize = Integer.parseInt(sortSize);
							sortByteSize = sSize;
						} catch (NumberFormatException e) {
							logger.error("sortSize is not numeric. value=" + sortSize, e);
							sortByteSize = f.getByteSize();
						}

						if (type.equals("uchar")) {
							sortByteSize *= 2;
						}
					}

					if (!isFixed && sortByteSize <= 0)
						throw new SettingException("Either fieldsize or sortsize must be set! name = " + name + ", fieldSize = " + fieldSize + ", sortsize = " + sortSize);

					SortSetting ss = new SortSetting(name, f, sSize, sortByteSize);
					schema.addSortSetting(ss);
					f.sortSetting = ss;
					schema.sortnames.put(name, sortSequence++);
				}

				/*
				 * 4. group
				 */
				if ("true".equals(el.getAttributeValue("group"))) {
					String groupKeySize = el.getAttributeValue("groupKeySize");
					int keyByteSize = 0;
					if (groupKeySize == null || groupKeySize.trim().length() == 0) {
						keyByteSize = f.getByteSize();
					} else {
						try {
							keyByteSize = Integer.parseInt(groupKeySize);
						} catch (NumberFormatException e) {
							logger.error("groupKeySize is not numeric. value=" + groupKeySize, e);
							keyByteSize = f.getByteSize();
						}

						if (type.equals("uchar")) {
							keyByteSize *= 2;
						}
					}
					if (!isFixed && keyByteSize <= 0)
						throw new SettingException("Either fieldsize or groupKeySize must be set! name = " + name + ", fieldSize = " + fieldSize + ", groupKeySize = "
						                + groupKeySize);

					GroupSetting gs = new GroupSetting(name, f, keyByteSize);
					schema.addGroupSetting(gs);
					f.groupSetting = gs;
					schema.groupnames.put(name, groupSequence++);
				}

				/*
				 * 5. filter
				 */
				if ("true".equals(el.getAttributeValue("filter"))) {
					if (!isFixed)
						throw new SettingException("FilterField must be fixed-length. name = " + name + ", fieldSize = " + fieldSize);

					FilterSetting fs = new FilterSetting(name, f);
					schema.addFilterSetting(fs);
					f.filterSetting = fs;
					schema.filternames.put(name, filterSequence++);
				}

				/*
				 * 6. Column 필드
				 */
				if ("true".equals(el.getAttributeValue("column"))) {
					if (!isFixed)
						throw new SettingException("컬럼필드는 고정길이어야 합니다. name = " + name + ", fieldSize = " + fieldSize);				
					
					String inMemory = el.getAttributeValue("inMemory");

					ColumnSetting cs = new ColumnSetting(name, f, Boolean.parseBoolean(inMemory));
					
										
					schema.addColumnSetting(cs);
					f.columnSetting = cs;
					schema.columnnames.put(name, columnSequence++);

					logger.debug("컬럼필드 확인 >> {}", cs);
				}
			} else {
				logger.warn("Unknown schema element name = " + el.getName() + " at " + (i + 1) + "-th element");
			}

		}

		return schema;
	}

	/*
	 * return 0 : OK return 1 : grammar error return 2 : not changed return
	 * 3 : file write error return 9 : 데이터값수정. 페이지 재로딩필요.
	 */
	private int updateWorkSchema0(String collection, Element root, String key, String value) throws SettingException {
		// logger.debug("updateWorkSchema0 {}, key={}, value={}, root= {}",
		// collection, key, value, root);
		logger.debug("updateWorkSchema0 {}, key={} ", collection, key);
		logger.debug("value={}, root= {}", value, root);
		String posStr = key.substring(0, 2);
		int pos = 0;
		if (posStr.startsWith("0")) {
			pos = Integer.parseInt(key.substring(1, 2));
		} else {
			pos = Integer.parseInt(posStr);
		}
		String fieldName = key.substring(2);

		String attrName = "";
		switch (pos) {
		case 1:
			attrName = "name";
			break;
		case 2:
			attrName = "primary";
			break;
		case 3:
			attrName = "type";
			break;
		case 4:
			attrName = "size";
			break;
		case 5:
			attrName = "index";
			break;
		case 6:
			attrName = "sort";
			break;
		case 7:
			attrName = "sortSize";
			break;
		case 8:
			attrName = "group";
			break;
		case 9:
			attrName = "filter";
			break;
		case 10:
			attrName = "store";
			break;
		case 11:
			attrName = "normalize";
			break;
		case 12:
			attrName = "column";
			break;
		case 13:
			attrName = "virtual";
			break;
		case 14:
			attrName = "modify";
			break;
		case 15:
			attrName = "tagRemove";
			break;
		case 16:
			attrName = "multiValue";
			break;
		default:
			break;

		// TODO groupKeySize

		}
		// logger.debug("attrName = {}", attrName);

		if ("name".equals(attrName))
			value = value.toLowerCase();
		List fields = root.getChildren("field");

		String xmlDir = getKey(collection, schemaWorkFilename);

		// schema.xml 臾몃쾿寃�궗
		for (int i = 0; i < fields.size(); i++) {
			// 2012-01-17 swsong 셋팅후 페이지를 재로딩 해야하는지 여부
			boolean valueChanged = false;
			Element el = (Element) fields.get(i);
			if (fieldName.equals(el.getAttributeValue("name"))) {
				// logger.debug("attrName={}, value={}, size={}",
				// attrName, value,
				// el.getAttributeValue("size"));
				if ((attrName.equals("group") || attrName.equals("filter")) && "true".equals(value)) {
					String size = el.getAttributeValue("size");
					if (size != null && size.startsWith("-")) {
						return 1;
					}
				}

				if (attrName.equals("sort")) {
					String size = el.getAttributeValue("size");
					String sortSize = el.getAttributeValue("sortSize");

					if (value.equals("true")) {

						// int s =
						// Integer.parseInt(size);
						// int ss =
						// Integer.parseInt(sortSize);
						if (size != null) {
							if (size.startsWith("-")) {
								// 가변길이필드는 기본
								// sortSize셋팅 4
								sortSize = "4";
							} else {
								// 고정길이필드는 size의
								// 길이를 그대로 사용.
								int s = Integer.parseInt(size);
								if (s > 8) {
									sortSize = "8";
								} else {
									sortSize = size;
								}
							}
							el.setAttribute("sortSize", sortSize);
							valueChanged = true;
						}
					} else {
						// 선택해제시 사이즈도 공란으로 설정.
						el.setAttribute("sortSize", "");
						valueChanged = true;
					}
				}

				if (attrName.equals("sortSize")) {
					if (value.startsWith("-"))
						return 1;

					if ("true".equals(el.getAttributeValue("sort"))) {

						String size = el.getAttributeValue("size");
						String sortSize = value;
						int s = -1;
						int ss = -1;
						try {
							s = Integer.parseInt(size);
						} catch (NumberFormatException e) {
						}

						try {
							ss = Integer.parseInt(sortSize);
						} catch (NumberFormatException e) {
						}
						// logger.debug("s= {}, ss={}",
						// s, ss);

						// 정렬사이즈는 사이즈가 -1일때를 빼고는 사이즈보다
						// 클수 없다.
						if (s != -1 && ss > s) {
							return 1;
						}
						if (s <= 0 && ss <= 0) {
							return 1;
						}
					}
				}

				if (attrName.equals("size")) {
					String type1 = el.getAttributeValue("type");
					if ("int".equals(type1) || "float".equals(type1)) {
						value = "4";
						valueChanged = true;
					} else if ("long".equals(type1) || "double".equals(type1) || "datetime".equals(type1)) {
						value = "8";
						valueChanged = true;
					} else {
						int s = -1;
						try {
							s = Integer.parseInt(value);
						} catch (NumberFormatException e) {
							return 1;
						}
						// -1이 아닌 음수는 문법오류이다.
						if (s < -1) {
							// 무시한다.
							return 1;
						} else if (s == 0) {
							return 1;
						} else if (s < 0) {
							if ("true".equals(el.getAttributeValue("group")) || "true".equals(el.getAttributeValue("filter"))
							                || "true".equals(el.getAttributeValue("sort"))) {
								// return 1;
								// 강제 무효셋팅
								el.setAttribute("sort", "false");
								el.setAttribute("sortSize", "");
								el.setAttribute("group", "false");
								el.setAttribute("filter", "false");
								valueChanged = true;
							} else {
								if (s == 0) {
									return 1;
								}
								// 최대 256까지
								// 설정가능.
								if (s > 256) {
									return 1;
								}
							}

						} else {
							// 정렬이 셋팅되어있고 정렬사이즈가 있을때
							if ("true".equals(el.getAttributeValue("sort"))) {
								String sortSize = el.getAttributeValue("sortSize");
								try {
									int v = Integer.parseInt(value);
									int ss = Integer.parseInt(sortSize);
									if (v > 0 && v < ss) {
										if (v > 8) {
											el.setAttribute("sortSize", "8");
										} else {
											el.setAttribute("sortSize", value);
										}
									}
									valueChanged = true;
								} catch (Exception e) {
									return 1;
								}
							}

						}
					}// if("int".equals(type) ||
					 // "float".equals(type)){
				}

				// primary키의 type만 바꿀경우
				if (attrName.equals("type")) {
					// 타입이 int, long, achar 가 아니면 문법오류.
					if ("true".equals(el.getAttributeValue("primary")) && (!"int".equals(value) && !"long".equals(value) && !"achar".equals(value))) {
						return 1;
					}

					if ("achar".equals(value) || "uchar".equals(value)) {
						String size = el.getAttributeValue("size");
						int s = -1;
						try {
							s = Integer.parseInt(size);
							el.setAttribute("size", s + "");
						} catch (NumberFormatException e) {
							// 잘못된 데이터입력시 10으로 셋팅
							el.setAttribute("size", "10");
						}

						// 가변길이필드의 길이가 -1 또는 0 일때 셋팅할때에는
						if (s <= 0) {
							// sort일때 sortSize가 없으면
							// 문법오류이다.
							el.setAttribute("sort", "false");
							el.setAttribute("sortSize", "");
							el.setAttribute("group", "false");
							el.setAttribute("filter", "false");
						}

					} else if ("int".equals(value) || "float".equals(value)) {
						el.setAttribute("size", "4");
					} else if ("long".equals(value) || "double".equals(value) || "datetime".equals(value)) {
						el.setAttribute("size", "8");
					}

					// 정렬이 셋팅되어있고 정렬사이즈가 있을때
					if ("true".equals(el.getAttributeValue("sort"))) {
						String sortSize = el.getAttributeValue("sortSize");
						try {
							int v = Integer.parseInt(el.getAttributeValue("size"));
							int ss = Integer.parseInt(sortSize);
							if (v > 0 && v < ss) {
								if (v > 8) {
									el.setAttribute("sortSize", "8");
								} else {
									el.setAttribute("sortSize", el.getAttributeValue("size"));
								}
							}
							valueChanged = true;
						} catch (Exception e) {
							return 1;
						}
					}

					// 타입변경시는 고정길이타입의 경우 size도 함께 변경되어야하기때문에
					// 무조건 재로딩하도록해준다.
					// 고정길이타입(int등)의 size가 -1일때 사이즈변경은 페이지에서
					// 하기때문이다.
					valueChanged = true;
				}

				// pk를 셋팅할 경우
				if (attrName.equals("primary")) {
					for (int j = 0; j < fields.size(); j++) {
						Element elj = (Element) fields.get(j);
						if (("true".equals(elj.getAttributeValue("primary"))) && "true".equals(value)) {
							return 1;
						}
					}
					// PK에 허용되는 필드인지 확인. int, long, achar만
					// 허용된다.
					String type = el.getAttributeValue("type");
					if ((!"int".equals(type) && !"long".equals(type) && !"achar".equals(type)) && "true".equals(value)) {
						return 1;
					}
					if ("true".equals(value)) {
						String index = el.getAttributeValue("index");
						if (index == null || index.trim().length() == 0) {
							// OK
						} else {
							// pk는 색인필드로 셋팅불가. 기본적으로
							// whitespace index를
							// 제공한다.
							return 1;
						}
					}
				}

				if (attrName.equals("index")) {
					if (value.trim().length() > 0) {
						if ("true".equals(el.getAttributeValue("primary"))) {
							return 1;
						}
					}
				}

//				if (attrName.equals("column")) {
//					el.setAttribute("column", value);
////					valueChanged = true;
//				}

				String oldValue = el.getAttributeValue(attrName);
				// logger.debug("value = {}, oldValue={}",
				// value, oldValue);
				if (value.equals(oldValue)) {
					// 이전값과 같더라도 입력값을 변경하는등의 작업으로 페이지 재로딩이
					// 필요하면 먼저 9를 리턴한다.
					if (valueChanged) {
						return 9;
					}
					// not changed
					return 2;
				}
				el.setAttribute(attrName, value);
				XMLOutputter xmlOut = new XMLOutputter();
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(new File(xmlDir));
					xmlOut.output(root.getDocument(), fos);
					// logger.debug("셋팅변경! {}", xmlDir);
				} catch (FileNotFoundException e) {
					logger.error(e.getMessage(), e);
					return 3;
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
					return 3;
				} finally {
					try {
						fos.close();
					} catch (IOException e) {
						logger.error(e.getMessage(), e);
					}
				}

				// 페이지재로딩 필요.
				if (valueChanged) {
					return 9;
				}

				return 0;
			}

		}

		return 0;
	}

	private int addField2WorkSchema0(String collection, Element root) throws SettingException {
		int count = root.getChildren("field").size();
		String xmlDir = getKey(collection, schemaWorkFilename);
		Element newField = new Element("field");
		newField.setAttribute("name", "newfield" + count);
		newField.setAttribute("type", "uchar");
		newField.setAttribute("size", "10");
		root.addContent(newField);
		XMLOutputter xmlOut = new XMLOutputter();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(xmlDir));
			xmlOut.output(root.getDocument(), fos);

		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
			return 1;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return 1;
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}

		return 0;
	}

	private int deleteField2WorkSchema0(String collection, Element root, String fieldName) throws SettingException {
		String xmlDir = getKey(collection, schemaWorkFilename);
		List fields = root.getChildren("field");

		for (int i = 0; i < fields.size(); i++) {

			Element el = (Element) fields.get(i);
			if (fieldName.equals(el.getAttributeValue("name"))) {
				root.removeContent(el);
			}
		}

		XMLOutputter xmlOut = new XMLOutputter();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(xmlDir));
			xmlOut.output(root.getDocument(), fos);

		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
			return 1;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return 1;
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}

		return 0;
	}

	private int recoverWorkSchema0(String collection, Element root, String fieldName) throws SettingException {

		Element schemaRoot = getXml(collection, schemaFilename);
		Element srcElement = null;
		List schemaFields = schemaRoot.getChildren("field");
		for (int i = 0; i < schemaFields.size(); i++) {
			Element el = (Element) schemaFields.get(i);
			if (fieldName.equals(el.getAttributeValue("name"))) {
				srcElement = el;
			}
		}

		List fields = root.getChildren("field");

		String xmlDir = getKey(collection, schemaWorkFilename);

		for (int i = 0; i < fields.size(); i++) {

			Element el = (Element) fields.get(i);
			if (fieldName.equals(el.getAttributeValue("name"))) {
				// replace
				// List attrList = srcElement.getAttributes();
				// el.removeContent();
				root.removeContent(el);
				root.addContent((Element) srcElement.clone());
				FileOutputStream fos = null;
				XMLOutputter xmlOut = new XMLOutputter();
				try {
					fos = new FileOutputStream(new File(xmlDir));
					xmlOut.output(root.getDocument(), fos);

				} catch (FileNotFoundException e) {
					throw new SettingException(e.getMessage());
				} catch (IOException e) {
					throw new SettingException(e.getMessage());
				} finally {
					try {
						fos.close();
					} catch (IOException e) {
						logger.error(e.getMessage(), e);
					}
				}
			}

		}

		return 0;
	}

	// 주키와 색인필드가 셋팅되어있는지 검증하는 메소드. swsong
	public int checkWorkSchema(String collection) {
		Element root = getXml(collection, schemaWorkFilename);
		if (root == null) {
			return 0;
		}
		List fields = root.getChildren("field");
		boolean isPrimaryKeyExist = false;
		boolean isIndexExist = false;

		// 먼저 pk설정여부를 확인한다.
		for (int j = 0; j < fields.size(); j++) {
			Element el = (Element) fields.get(j);
			if ("true".equals(el.getAttributeValue("primary"))) {
				isPrimaryKeyExist = true;
			}
			String value = el.getAttributeValue("index");
			if (value != null && value.length() > 0) {
				isIndexExist = true;
			}
		}

		// pk설정확인후 색인필드셋팅여부확인한다.
		if (!isPrimaryKeyExist) {
			// PK없음!
			return 1;
		}

		if (!isIndexExist) {
			// PK없음!
			return 2;
		}

		return 0;
	}

	public DataSourceSetting getDatasource(String collection, boolean reload) {
		if (!reload) {
			Properties p = (Properties) getFromCache(collection, datasourceFilename);
			if (p != null)
				return new DataSourceSetting(p);
		}
		return new DataSourceSetting(getXmlProperties(collection, datasourceFilename));
	}

	/**
	 * datasourceFilename에 .1 .2 가 붙은 파일들을 순차적으로 찾는다.
	 * */
	public List<DataSourceSetting> getMultiDatasource(String collection, boolean reload) {

		List<DataSourceSetting> list = new ArrayList<DataSourceSetting>();

		File f = new File(getKey(collection, datasourceFilename));
		Properties p = null;
		if (!reload) {
			p = (Properties) getFromCache(collection, datasourceFilename);
		}
		if (p == null) {
			p = getXmlProperties(collection, datasourceFilename);
		}
		list.add(new DataSourceSetting(p));

		for (int i = 1;; i++) {
			f = new File(getKey(collection, datasourceFilename + "." + i));

			if (!f.exists()) {
				break;
			}

			p = null;
			if (!reload) {
				p = (Properties) getFromCache(collection, datasourceFilename + "." + i);
			}
			if (p == null) {
				p = getXmlProperties(collection, datasourceFilename + "." + i);
			}
			list.add(new DataSourceSetting(p));
			logger.debug("Multi Datasource {} >> {}", i, f.getName());

		}

		return list;
	}

	public void storeDataSourceSetting(String collection, DataSourceSetting setting) {
		if (setting == null) {
			logger.error("DataSourceSetting file is null.");
		} else {
			Properties props = setting.getProperties();
			storeXmlProperties(collection, props, datasourceFilename);
			putToCache(collection, props, datasourceFilename);
		}

	}

	public void initDatasource(String collection) {
		Properties props = new Properties();
		DataSourceSetting.init(props);
		storeXmlProperties(collection, props, datasourceFilename);
	}

	public IRConfig getConfig() {
		return getConfig(false);
	}
	
	public IRConfig getConfig(boolean reload) {
		IRConfig config = null;
		if (!reload) {
			config = (IRConfig) getFromCache(configFilename);
			if (config != null) {
				return config;
			}
		}

		File configFile = environment.filePaths().makePath("conf").append(configFilename).file();
		logger.debug("configFile = {}", configFile.getAbsolutePath());
		Properties props = new Properties();
		try {
			FileInputStream fis = new FileInputStream(configFile);
			props.load(fis);
			fis.close();
			
		} catch (FileNotFoundException e) {
			logger.error("",e);
		} catch (IOException e) {
			logger.error("",e);
		}
		
		config = new IRConfig(props);
		putToCache(config, configFilename);

		return config;
	}
	
	public IRConfig getConfig(String filepath) {
		return getConfig(filepath, false);
	}
	
	public IRConfig getConfig(String filepath, boolean reload) {
		String settingsPath = IRSettings.HOME + filepath;
		
		IRConfig config = null;
		if (!reload) {
			config = (IRConfig) getFromCache(settingsPath);
			if (config != null) {
				return config;
			}
		}

		logger.debug("configFile = {}", new File(settingsPath).getAbsolutePath());
		Properties props = new Properties();
		try {
			FileInputStream fis = new FileInputStream(settingsPath);
			props.load(fis);
			fis.close();
			
		} catch (FileNotFoundException e) {
			logger.error("",e);
		} catch (IOException e) {
			logger.error("",e);
		}
		
		config = new IRConfig(props);
		putToCache(config, settingsPath);

		return config;
	}

	public void storeConfig(IRConfig config) {
		if (config == null) {
			logger.error("Config file is null.");
		} else {
			File configFile = environment.filePaths().makePath("conf").append(configFilename).file();
			Properties props = config.getProperties();
			try {
				FileOutputStream fos = new FileOutputStream(configFile);
				props.store(fos, "Auto saved config file.");
				fos.close();
			} catch (FileNotFoundException e) {
				logger.error(e.getMessage(), e);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}

			putToCache(config, configFilename);
		}

	}

	public IRConfig getServerEnvConfig(boolean reload) {
		IRConfig config = null;
		if (!reload) {
			config = (IRConfig) getFromCache(mirrorEnvFilename);
		} else {
			File configFile = environment.filePaths().makePath("conf").append(mirrorEnvFilename).file();
			logger.debug("mirrorEnv configFile = {}", configFile.getAbsolutePath());
			Properties props = new Properties();
			try {
				FileInputStream fis = new FileInputStream(configFile);
				props.load(fis);
				fis.close();
			} catch (FileNotFoundException e) {
				logger.error(e.getMessage(), e);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			config = new IRConfig(props);
			putToCache(config, mirrorEnvFilename);
		}
		return config;
	}

	public Properties getIndextime(String collection, boolean reload) {
		if (!reload) {
			Properties p = (Properties) getFromCache(collection, indextimeFilename);
			if (p != null)
				return p;
		}
		return getProperties(collection, indextimeFilename);
	}

	public void storeIndextime(String collection, String type, String startDt, String endDt, String duration, int docSize) {
		Properties props = new Properties();
		props.put("type", type);
		props.put("start_dt", startDt);
		props.put("end_dt", endDt);
		props.put("duration", duration);
		props.put("size", Integer.toString(docSize));
		storeProperties(collection, props, indextimeFilename);
	}

	/*
	 * AUTH
	 */
	public String[] isCorrectPasswd(String username, String passwd) {
		if (username == null)
			return null;

		Properties props = null;
		Object obj = getFromCache(passwdFilename);
		if (obj != null) {
			props = (Properties) obj;
		} else {
			props = getProperties(passwdFilename);
		}
		logger.debug("props = {}, username = {}", props, username);
		String p = props.getProperty(username);
		if (p == null)
			return null;

		String p2 = encryptPasswd(passwd);
		if (p2.equalsIgnoreCase((String) p)) {
			String[] log = new String[2];
			String ip = props.getProperty(username + ".ip");
			if (ip == null) {
				log[0] = "";
			} else {
				log[0] = ip;
			}

			String time = props.getProperty(username + ".time");
			if (time == null) {
				log[1] = "";
			} else {
				log[1] = time;
			}
			return log;
		} else {
			return null;
		}
	}

	public boolean isAuthUsed() {
		Properties props = null;
		Object obj = getFromCache(passwdFilename);
		if (obj != null) {
			props = (Properties) obj;
		} else {
			props = getProperties(passwdFilename);
		}

		if (props != null && props.containsKey("use")) {
			return !("false".equalsIgnoreCase(props.getProperty("use")));
		}

		return false;

	}

	public void storePasswd(String username, String passwd) {
		Properties props = getProperties(passwdFilename);
		props.put(username, encryptPasswd(passwd));
		storeProperties(props, passwdFilename);
		putToCache(props, passwdFilename);
	}

	public void storeAccessLog(String username, String ip) {
		Properties props = getProperties(passwdFilename);
		props.setProperty(username + ".ip", ip);
		props.setProperty(username + ".time", getSimpleDatetime());
		storeProperties(props, passwdFilename);
		putToCache(props, passwdFilename);
	}

	private String encryptPasswd(String passwd) {
		String[] hexArray = { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0A", "0B", "0C", "0D", "0E", "0F", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "1A",
		                "1B", "1C", "1D", "1E", "1F", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "2A", "2B", "2C", "2D", "2E", "2F", "30", "31", "32", "33", "34", "35", "36",
		                "37", "38", "39", "3A", "3B", "3C", "3D", "3E", "3F", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "4A", "4B", "4C", "4D", "4E", "4F", "50", "51", "52",
		                "53", "54", "55", "56", "57", "58", "59", "5A", "5B", "5C", "5D", "5E", "5F", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "6A", "6B", "6C", "6D", "6E",
		                "6F", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "7A", "7B", "7C", "7D", "7E", "7F", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "8A",
		                "8B", "8C", "8D", "8E", "8F", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "9A", "9B", "9C", "9D", "9E", "9F", "A0", "A1", "A2", "A3", "A4", "A5", "A6",
		                "A7", "A8", "A9", "AA", "AB", "AC", "AD", "AE", "AF", "B0", "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9", "BA", "BB", "BC", "BD", "BE", "BF", "C0", "C1", "C2",
		                "C3", "C4", "C5", "C6", "C7", "C8", "C9", "CA", "CB", "CC", "CD", "CE", "CF", "D0", "D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8", "D9", "DA", "DB", "DC", "DD", "DE",
		                "DF", "E0", "E1", "E2", "E3", "E4", "E5", "E6", "E7", "E8", "E9", "EA", "EB", "EC", "ED", "EE", "EF", "F0", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "FA",
		                "FB", "FC", "FD", "FE", "FF" };
		StringBuffer sb = new StringBuffer();
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] encData = md5.digest(passwd.getBytes());
			for (int i = 0; i < encData.length; i++) {
				sb.append(hexArray[0xff & encData[i]]);
			}
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(), e);
		}
		return sb.toString();
	}

	public String getSimpleDatetime() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}

	private Object getFromCache(String settingName) {
		String key = environment.filePaths().makePath("conf").append(settingName).toString();
		return settingCache.get(key);
	}

	private Object getFromCache(String collection, String settingName) {
		String key = getKey(collection, settingName);
		return settingCache.get(key);
	}

	private Object putToCache(Object setting, String settingName) {
		String key = environment.filePaths().makePath("conf").append(settingName).toString();
		settingCache.put(key, setting);
		return setting;
	}

	private Object putToCache(String collection, Object setting, String settingName) {
		String key = getKey(collection, settingName);
		settingCache.put(key, setting);
		return setting;
	}

	public String getKey(String collection, String filename) {
		return environment.filePaths().makePath("collection").append(collection).append(filename).toString();
	}

	public String getKey(String filename) {
		return environment.filePaths().makePath("conf").append(filename).toString();
	}

	public Element getXml(String collection, String filename) {
		String configFile = getKey(collection, filename);
		logger.debug("Read xml = {}", configFile);
		Document doc = null;
		try {
			File f = new File(configFile);
			if (!f.exists()) {
				return null;
			}

			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(f);
			Element e = doc.getRootElement();
			putToCache(collection, e, filename);
			return e;
		} catch (JDOMException e) {
			logger.error(e.getMessage(), e);
		} catch (NullPointerException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private Properties getProperties(String filename) {
		File f = environment.filePaths().makePath("conf").append(filename).file();
		Properties result = new Properties();
		try {
			if (!f.exists()) {
				f.createNewFile();
			}
			result.load(new FileInputStream(f));
			putToCache(result, filename);
			return result;

		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	//
	public Properties getProperties(String collection, String filename) {
		String configFile = getKey(collection, filename);
		logger.debug("Read properties = {}", configFile);
		Properties result = new Properties();
		try {
			result.load(new FileInputStream(configFile));
			putToCache(collection, result, filename);
			return result;
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
			return null;
		} catch (IOException e) {
			// logger.error(e.getMessage(), e);
			return null;
		}
	}

	private Properties getXmlProperties(String collection, String filename) {
		String configFile = getKey(collection, filename);
		logger.debug("Read properties = {}", configFile);
		Properties result = new Properties();
		try {
			result.loadFromXML(new FileInputStream(configFile));
			putToCache(collection, result, filename);
			return result;
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private void storeXmlProperties(String collection, Properties props, String filename) {
		String configFile = getKey(collection, filename);
		logger.debug("Store properties = {}", configFile);
		FileOutputStream writer = null;
		try {
			writer = new FileOutputStream(configFile);
			props.storeToXML(writer, new Date().toString());
			putToCache(collection, props, filename);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				// ignore
			}

		}
	}

	private void storeProperties(String collection, Properties props, String filename) {
		String configFile = getKey(collection, filename);
		logger.debug("Store properties = {}", configFile);
		FileOutputStream writer = null;
		try {
			writer = new FileOutputStream(configFile);
			props.store(writer, null);
			putToCache(collection, props, filename);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				// ignore
			}

		}
	}

	private void storeProperties(Properties props, String filename) {
		String configFile = getKey(filename);
		logger.debug("Store properties = {}", configFile);
		FileOutputStream writer = null;
		try {
			writer = new FileOutputStream(configFile);
			props.store(writer, null);
			putToCache(props, filename);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				// ignore
			}

		}
	}

	public Settings getSettings() {
		//load config file
		synchronized(FileNames.serverConfig){
			Object obj = getFromCache(FileNames.serverConfig);
			if(obj != null){
				return (Settings) obj;
			}
			File configFile = environment.filePaths().makePath("conf").append(FileNames.serverConfig).file();
	        InputStream input = null;
	        try{
	        	Yaml yaml = new Yaml();
	        	input = new FileInputStream(configFile);
	        	Map<String, Object> data = (Map<String, Object>) yaml.load(input);
	        	Settings settings = new Settings(data);
	        	putToCache(settings, FileNames.serverConfig);
	        	return settings;
	        } catch (FileNotFoundException e) {
	        	logger.error("설정파일을 찾을수 없습니다. file = {}", configFile.getAbsolutePath());
			} finally {
	        	if(input != null){
	        		try {
						input.close();
					} catch (IOException ignore) {
					}
	        	}
	        }
		}
		return null;
	}
}
