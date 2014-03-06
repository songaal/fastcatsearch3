package org.fastcatsearch.ir.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.fastcatsearch.util.JAXBConfigs;
import org.junit.Test;

public class DataSourceConfigTest {

	String datasourceConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
			"<datasource>\n" + 
			"    <add-indexing>\n" + 
			"        <source id=\"db1\" active=\"false\">\n" + 
			"            <properties>\n" + 
			"                <property key=\"encoding\">utf-8</property>\n" + 
			"                <property key=\"jdbc-driver\">com.mysql.driver.Driver</property>\n" + 
			"            </properties>\n" + 
			"            <modifier>modifier</modifier>\n" + 
			"            <reader>com.abc.Reader</reader>\n" + 
			"        </source>\n" + 
			"        <source id=\"file1\" active=\"false\">\n" + 
			"            <properties>\n" + 
			"                <property key=\"encoding\">utf-8</property>\n" + 
			"            </properties>\n" + 
			"            <modifier>modifier2</modifier>\n" + 
			"            <reader>com.abc.Reader</reader>\n" + 
			"        </source>\n" + 
			"    </add-indexing>\n" + 
			"    <full-indexing>\n" + 
			"        <source id=\"db1\" active=\"false\">\n" + 
			"            <properties>\n" + 
			"                <property key=\"encoding\">utf-8</property>\n" + 
			"                <property key=\"jdbc-driver\">com.mysql.driver.Driver</property>\n" + 
			"            </properties>\n" + 
			"            <modifier>modifier</modifier>\n" + 
			"            <reader>com.abc.Reader</reader>\n" + 
			"        </source>\n" + 
			"        <source id=\"file1\" active=\"false\">\n" + 
			"            <properties>\n" + 
			"                <property key=\"encoding\">utf-8</property>\n" + 
			"            </properties>\n" + 
			"            <modifier>modifier2</modifier>\n" + 
			"            <reader>com.abc.Reader</reader>\n" + 
			"        </source>\n" + 
			"    </full-indexing>\n" + 
			"    <jdbc-sources>\n" + 
			"        <jdbc-source user=\"admin\" url=\"jdbc://localhost/test\" password=\"1234567\" name=\"name1\" id=\"id1\" driver=\"com.mysql.Driver\"/>\n" + 
			"        <jdbc-source user=\"sa\" url=\"jdbc://localhost/test\" password=\"sa1234567\" name=\"name2\" id=\"id2\" driver=\"com.oracle.Driver\"/>\n" + 
			"    </jdbc-sources>\n" + 
			"</datasource>\n" + 
			"";
	@Test
	public void testRead() throws IOException, JAXBException {
		InputStream is = new ByteArrayInputStream(datasourceConfigXml.getBytes());
		DataSourceConfig dataSourceConfig = JAXBConfigs.readConfig(is, DataSourceConfig.class);
		List<SingleSourceConfig> sourceList = dataSourceConfig.getFullIndexingSourceConfig();
		for(SingleSourceConfig config : sourceList){
			System.out.println(config);
			System.out.println(config.isActive());
		}
		
		List<SingleSourceConfig> sourceList2 = dataSourceConfig.getAddIndexingSourceConfig();
		for(SingleSourceConfig config : sourceList2){
			System.out.println(config);
			System.out.println(config.isActive());
		}
		
//		List<JDBCSourceInfo> sourceInfoList = dataSourceConfig.getJdbcSourceInfoList();
//		for(JDBCSourceInfo jdbcSourceInfo : sourceInfoList){
//			System.out.println(jdbcSourceInfo.getId());
//			System.out.println(jdbcSourceInfo.getName());
//			System.out.println(jdbcSourceInfo.getDriver());
//			System.out.println(jdbcSourceInfo.getUrl());
//			System.out.println(jdbcSourceInfo.getUser());
//			System.out.println(jdbcSourceInfo.getPassword());
//		}
	}

	
	@Test
	public void testSingleSourceConfigWrite() throws IOException, JAXBException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SingleSourceConfig config = new SingleSourceConfig();
		config.setSourceModifier("modifier");
		config.setSourceReader("com.abc.Reader");
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("encoding", "utf-8");
		config.setProperties(properties);
		
		JAXBConfigs.writeRawConfig(baos, config, SingleSourceConfig.class);
		
		System.out.println(new String(baos.toByteArray()));
		
	}
	
	@Test
	public void testDataSourceConfigWrite() throws IOException, JAXBException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		DataSourceConfig dataSourceConfig = new DataSourceConfig();
		
		List<SingleSourceConfig> fullSourceConfigList = new ArrayList<SingleSourceConfig>();
		dataSourceConfig.setFullIndexingSourceConfig(fullSourceConfigList);
		List<SingleSourceConfig> addSourceConfigList = new ArrayList<SingleSourceConfig>();
		dataSourceConfig.setAddIndexingSourceConfig(addSourceConfigList);
		{
			SingleSourceConfig config = new SingleSourceConfig();
			config.setSourceModifier("modifier");
			config.setSourceReader("com.abc.Reader");
			Map<String, String> properties = new HashMap<String, String>();
			properties.put("encoding", "utf-8");
			properties.put("jdbc-driver", "com.mysql.driver.Driver");
			config.setProperties(properties);
			
			fullSourceConfigList.add(config);
			addSourceConfigList.add(config);
		}
		{
			SingleSourceConfig config2 = new SingleSourceConfig();
			config2.setSourceModifier("modifier2");
			config2.setSourceReader("com.abc.Reader");
			Map<String, String> properties = new HashMap<String, String>();
			properties.put("encoding", "utf-8");
			config2.setProperties(properties);
			
			fullSourceConfigList.add(config2);
			addSourceConfigList.add(config2);
		}
		{
			List<JDBCSourceInfo> jdbcSourceInfoList = new ArrayList<JDBCSourceInfo>();
			JDBCSourceInfo jdbc1 = new JDBCSourceInfo();
			jdbc1.setId("id1");
			jdbc1.setName("name1");
			jdbc1.setDriver("com.mysql.Driver");
			jdbc1.setUrl("jdbc://localhost/test");
			jdbc1.setUser("admin");
			jdbc1.setPassword("1234567");
			jdbcSourceInfoList.add(jdbc1);
			jdbc1 = new JDBCSourceInfo();
			jdbc1.setId("id2");
			jdbc1.setName("name2");
			jdbc1.setDriver("com.oracle.Driver");
			jdbc1.setUrl("jdbc://localhost/test");
			jdbc1.setUser("sa");
			jdbc1.setPassword("sa1234567");
			jdbcSourceInfoList.add(jdbc1);
//			dataSourceConfig.setJdbcSourceInfoList(jdbcSourceInfoList);
		}
		
		
		JAXBConfigs.writeRawConfig(baos, dataSourceConfig, DataSourceConfig.class);
		
		System.out.println(new String(baos.toByteArray()));
		
	}
}
