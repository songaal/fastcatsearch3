package org.fastcatsearch.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.derby.tools.ij;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.fastcatsearch.db.mapper.DictionaryMapper;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.ColumnSetting;
import org.junit.Test;

public class InternalDBModuleTest {

	@Test
	public void test() throws IOException {
		List<ColumnSetting> columnSettingList = new ArrayList<ColumnSetting>();
		String dbPath = "/tmp/idbtest;create=true";
		String mapperFilePath = "org/fastcatsearch/db/mapper/DictionaryMapper.xml";
		URL mapperFile = Resources.getResourceURL(mapperFilePath);
		List<URL> mapperFileList = new ArrayList<URL>();
		mapperFileList.add(mapperFile);
		// 디비를 열고 닫고 여러번가능한지..
		for (int i = 0; i < 3; i++) {
			InternalDBModule internalDBModule = new InternalDBModule(dbPath, mapperFileList, null, null);
			internalDBModule.load();

			SqlSession session = internalDBModule.openSession();
			DictionaryMapper mapper = session.getMapper(DictionaryMapper.class);
			try {
				mapper.createTable("a", columnSettingList);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				mapper.createIndex("a", "key");
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				mapper.dropTable("a");
			} catch (Exception e) {
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

	@Test
	public void test2() {
//		try {
//			Thread.sleep(100000);
//		} catch (InterruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		System.out.println("Start!");
		String dbPath = "/Users/swsong/TEST_HOME/danawa1022/node1/db/system";
		List<URL> mapperFileList = new ArrayList<URL>();

		InternalDBModule internalDBModule = new InternalDBModule(dbPath, mapperFileList, null, null);
		internalDBModule.load();
		SqlSession sqlSession = internalDBModule.openSession();
		try {

			Connection connection = sqlSession.getConnection();

			String line = null;
			Console console = System.console();
			while ((line = console.readLine()) != null) {
				ByteArrayOutputStream resultOutput = new ByteArrayOutputStream();
				ij.runScript(connection, new ByteArrayInputStream(line.getBytes()), "UTF-8", resultOutput, "UTF-8");

				String resultString = resultOutput.toString();
				System.out.println(resultString);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {

			if (sqlSession != null) {
				sqlSession.close();
			}
		}

	}

}
