package org.fastcatsearch.db.mapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.fastcatsearch.db.InternalDBModule;
import org.fastcatsearch.db.mapper.DictionaryMapper.KeyValue;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.ColumnSetting;
import org.junit.Test;

public class DictionaryMapperTest {

	@Test
	public void test() throws IOException {
		String dbPath = "/tmp/dict";
		String mapperFilePath = "org/fastcatsearch/db/mapper/DictionaryMapper.xml";
		URL mapperFile = Resources.getResourceURL(mapperFilePath);
		List<URL> mapperFileList = new ArrayList<URL>();
		mapperFileList.add(mapperFile);
		InternalDBModule internalDBModule = new InternalDBModule(dbPath, mapperFileList, null, null);
		internalDBModule.load();
		List<ColumnSetting> columnSettingList = new ArrayList<ColumnSetting>();
		
		
		SqlSession session = internalDBModule.openBatchSession();
		DictionaryMapper dictionaryMapper = null;
		String dictionaryName = "user_dictionary";
		String[] fieldList = new String[]{"value1", "value2"}; 
		try{
			//////////////////////////////
			dictionaryMapper = session.getMapper(DictionaryMapper.class);
			try {
				dictionaryMapper.validateTable(dictionaryName, columnSettingList);
			}catch(Exception e){
				try {
					dictionaryMapper.dropTable(dictionaryName);
					session.commit();
				}catch(Exception ignore){ 
					//존재하지 않을수 있다.
				}
				
				dictionaryMapper.createTable(dictionaryName, columnSettingList);
				session.commit();
				dictionaryMapper.createIndex(dictionaryName, "key");
				session.commit();
			}
			
			String[] columns = new String[]{"key", "value1", "value2"};
			dictionaryMapper.putEntry(dictionaryName, columns, new String[]{Long.toString(System.currentTimeMillis()), "def", "한글" });
			dictionaryMapper.putEntry(dictionaryName, columns, new String[]{Long.toString(System.currentTimeMillis()), "def1", "한글1" });
			dictionaryMapper.putEntry(dictionaryName, new String[]{"key", "value1"}, new String[]{Long.toString(System.currentTimeMillis()), "def2"});
			session.commit();
			int id = dictionaryMapper.getCount(dictionaryName, null, null);
			session.commit();
			System.out.println("count = "+id);
			
			Map<String, Object> vo = dictionaryMapper.getEntry(dictionaryName, id);
			printVO(vo);
			dictionaryMapper.updateEntry(dictionaryName, id, new KeyValue[]{new KeyValue("key", Long.toString(System.currentTimeMillis())),new KeyValue("value1", "def_U"), new KeyValue("value2", "한글_U")});
			session.commit();
			vo = dictionaryMapper.getEntry(dictionaryName, id);
			printVO(vo);
			
			
			dictionaryMapper.dropTable(dictionaryName);
			session.commit();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			session.commit();
			session.close();
			
			internalDBModule.unload();
		}
	}

	private void printVO(Map<String, Object> vo) {
		for(Map.Entry<String, Object> entry : vo.entrySet()){
			System.out.println(entry.getKey() + "= " + entry.getValue());
		}		
	}

}
