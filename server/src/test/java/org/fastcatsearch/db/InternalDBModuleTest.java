package org.fastcatsearch.db;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.fastcatsearch.db.mapper.DictionaryMapper;
import org.junit.Test;

public class InternalDBModuleTest {

	@Test
	public void test() throws IOException {
		String dbPath = "/tmp/idbtest;create=true";
		String mapperFilePath = "org/fastcatsearch/db/mapper/DictionaryMapper.xml";
		File mapperFile = Resources.getResourceAsFile(mapperFilePath);
		List<File> mapperFileList = new ArrayList<File>();
		mapperFileList.add(mapperFile);
		
		//디비를 열고 닫고 여러번가능한지.. 
		for(int i =0;i<3; i++){
			InternalDBModule internalDBModule = new InternalDBModule(dbPath, mapperFileList, null, null, null);
			internalDBModule.load();
			
			SqlSession session = internalDBModule.openSession();
			DictionaryMapper mapper= session.getMapper(DictionaryMapper.class);
			try{
				mapper.createTable("a", 10);
			}catch(Exception e){
				e.printStackTrace();
			}
			try{
				mapper.createIndex("a");
			}catch(Exception e){
				e.printStackTrace();
			}
			try{
				mapper.dropTable("a");
			}catch(Exception e){
				e.printStackTrace();
			}
			session.commit();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			internalDBModule.unload();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
