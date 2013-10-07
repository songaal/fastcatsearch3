package org.fastcatsearch.db.mapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.fastcatsearch.db.InternalDBModule;
import org.fastcatsearch.db.mapper.DictionaryMapper.KeyValue;
import org.junit.Test;

public class DictionaryMapperTest {

	@Test
	public void test() throws IOException {
		String dbPath = "/tmp/dict";
		String mapperFilePath = "org/fastcatsearch/db/mapper/DictionaryMapper.xml";
		File mapperFile = Resources.getResourceAsFile(mapperFilePath);
		List<File> mapperFileList = new ArrayList<File>();
		mapperFileList.add(mapperFile);
		InternalDBModule internalDBModule = new InternalDBModule(dbPath, mapperFileList, null, null, null);
		internalDBModule.load();
		
		SqlSession session = internalDBModule.openBatchSession();
		DictionaryMapper dictionaryMapper = null;
		String dictionaryName = "user_dictionary";
		String[] fieldList = new String[]{"value1", "value2"}; 
		try{
			//////////////////////////////
			dictionaryMapper = session.getMapper(DictionaryMapper.class);
			try {
				dictionaryMapper.validateTable(dictionaryName, fieldList);
			}catch(Exception e){
				try {
					dictionaryMapper.dropTable(dictionaryName);
					session.commit();
				}catch(Exception ignore){ 
					//존재하지 않을수 있다.
				}
				
				dictionaryMapper.creatTable(dictionaryName, 1000, fieldList);
				session.commit();
			}
			dictionaryMapper.putEntry(dictionaryName, Long.toString(System.currentTimeMillis())
					, new KeyValue[]{new KeyValue("value1", "def"), new KeyValue("value2", "한글")});
			dictionaryMapper.putEntry(dictionaryName, Long.toString(System.currentTimeMillis())
					, new KeyValue[]{new KeyValue("value1", "def1"), new KeyValue("value2", "한글1")});
			dictionaryMapper.putEntry(dictionaryName, Long.toString(System.currentTimeMillis())
					, new KeyValue[]{new KeyValue("value1", "def2")});
			session.commit();
			int id = dictionaryMapper.getCount(dictionaryName, null);
			session.commit();
			System.out.println("count = "+id);
			
			Map<String, Object> vo = dictionaryMapper.getEntry(dictionaryName, id);
			printVO(vo);
			
			dictionaryMapper.updateEntry(dictionaryName, id, Long.toString(System.currentTimeMillis()), new KeyValue[]{new KeyValue("value1", "def_U"), new KeyValue("value2", "한글_U")});
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
