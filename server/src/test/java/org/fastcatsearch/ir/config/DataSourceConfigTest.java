package org.fastcatsearch.ir.config;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.fastcatsearch.ir.config.CollectionsConfig.Collection;
import org.junit.Test;

public class DataSourceConfigTest {

	String datasourceConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
			"<datasource>\n" + 
			"	\n" + 
			"	<full-indexing>\n" + 
			"		<db id=\"db1\" name=\"db first\" active=\"false\">\n" + 
			"			<sourceModifier></sourceModifier>\n" + 
			"	        <sourceReader>org.fastcatsearch.datasource.reader.DBReader</sourceReader>\n" + 
			"	        <bulkSize>0</bulkSize>\n" + 
			"	        <fetchSize>0</fetchSize>\n" + 
			"	        <resultBuffering>false</resultBuffering>\n" + 
			"	        <shard-sources>\n" + 
			"	        	<shard id=\"sample\">\n" + 
			"	        		<dataSql>select * from table_sample</dataSql>\n" + 
			"	        	</shard>\n" + 
			"	        	<shard id=\"sample1\">\n" + 
			"	        		<dataSql>select * from table_sample</dataSql>\n" + 
			"	        	</shard>\n" + 
			"	        	<shard id=\"sample2\">\n" + 
			"	        		<dataSql>select * from table_sample</dataSql>\n" + 
			"	        	</shard>\n" + 
			"	        </shard-sources>\n" + 
			"		</db>\n" + 
			"		\n" + 
			"		<file active=\"true\">\n" + 
			"	        <sourceModifier></sourceModifier>\n" + 
			"	        <sourceReader>org.fastcatsearch.datasource.reader.FSFileSourceReader</sourceReader>\n" + 
			"	        \n" + 
			"	        <shard-sources>\n" + 
			"	        	<shard id=\"sample\">\n" + 
			"	        		<fileEncoding>utf-8</fileEncoding>\n" + 
			"			        <filePath>testData/full1</filePath>\n" + 
			"	        	</shard>\n" + 
			"	        	<shard id=\"sample1\">\n" + 
			"	        		<fileEncoding>utf-8</fileEncoding>\n" + 
			"			        <filePath>testData/full1</filePath>\n" + 
			"	        	</shard>\n" + 
			"	        	<shard id=\"sample2\">\n" + 
			"	        		<fileEncoding>utf-8</fileEncoding>\n" + 
			"			        <filePath>testData/full1</filePath>\n" + 
			"	        	</shard>\n" + 
			"	        </shard-sources>\n" + 
			"	    </file>\n" + 
			"	</full-indexing>\n" + 
			"			\n" + 
			"	<add-indexing>\n" + 
			"		<db id=\"db1\" name=\"db first\" active=\"false\">\n" + 
			"			<sourceModifier></sourceModifier>\n" + 
			"	        <sourceReader>org.fastcatsearch.datasource.reader.DBReader</sourceReader>\n" + 
			"	        <bulkSize>0</bulkSize>\n" + 
			"	        <fetchSize>0</fetchSize>\n" + 
			"	        <resultBuffering>false</resultBuffering>\n" + 
			"	        <shard-sources>\n" + 
			"	        	<shard id=\"sample\">\n" + 
			"	        		<dataSql>select * from table_sample</dataSql>\n" + 
			"	        		<deleteSql>select id from table_sample where status=1</deleteSql>\n" + 
			"	        		<beforeSql>select id from table_sample where status=1</beforeSql>\n" + 
			"	        		<afterSql>select id from table_sample where status=1</afterSql>\n" + 
			"	        	</shard>\n" + 
			"	        	<shard id=\"sample1\">\n" + 
			"	        		<dataSql>select * from table_sample</dataSql>\n" + 
			"	        		<deleteSql>select id from table_sample where status=1</deleteSql>\n" + 
			"	        		<beforeSql>select id from table_sample where status=1</beforeSql>\n" + 
			"	        		<afterSql>select id from table_sample where status=1</afterSql>\n" + 
			"	        	</shard>\n" + 
			"	        	<shard id=\"sample2\">\n" + 
			"	        		<dataSql>select * from table_sample</dataSql>\n" + 
			"	        		<deleteSql>select id from table_sample where status=1</deleteSql>\n" + 
			"	        		<beforeSql>select id from table_sample where status=1</beforeSql>\n" + 
			"	        		<afterSql>select id from table_sample where status=1</afterSql>\n" + 
			"	        	</shard>\n" + 
			"	        </shard-sources>\n" + 
			"		</db>\n" + 
			"		\n" + 
			"		<file active=\"true\">\n" + 
			"	        <sourceModifier></sourceModifier>\n" + 
			"	        <sourceReader>org.fastcatsearch.datasource.reader.FSFileSourceReader</sourceReader>\n" + 
			"	        \n" + 
			"	        <shard-sources>\n" + 
			"	        	<shard id=\"sample\">\n" + 
			"	        		<fileEncoding>utf-8</fileEncoding>\n" + 
			"			        <filePath>testData/full1</filePath>\n" + 
			"	        	</shard>\n" + 
			"	        	<shard id=\"sample1\">\n" + 
			"	        		<fileEncoding>utf-8</fileEncoding>\n" + 
			"			        <filePath>testData/full1</filePath>\n" + 
			"	        	</shard>\n" + 
			"	        	<shard id=\"sample2\">\n" + 
			"	        		<fileEncoding>utf-8</fileEncoding>\n" + 
			"			        <filePath>testData/full1</filePath>\n" + 
			"	        	</shard>\n" + 
			"	        </shard-sources>\n" + 
			"	    </file>\n" + 
			"	</add-indexing>\n" + 
			"    <jdbc-sources>\n" + 
			"    	<jdbc-source id=\"db1\" name=\"디비1\" driver=\"mysql.Driver\" url=\"jdbc:mysql:127.0.0.1/test\" user=\"james\" password=\"1111\" />\n" + 
			"    	<jdbc-source id=\"db2\" name=\"디비2\" driver=\"oracle.Driver\" url=\"jdbc:oracle:127.0.0.1/test\" user=\"scott\" password=\"1234\" />\n" + 
			"    </jdbc-sources>\n" + 
			"</datasource>\n" + 
			"";
	@Test
	public void testRead() throws IOException, JAXBException {
		InputStream is = new ByteArrayInputStream(datasourceConfigXml.getBytes());
		DataSourceConfig dataSourceConfig = JAXBConfigs.readConfig(is, DataSourceConfig.class);
		List<DBSourceConfig> sourceList = dataSourceConfig.getFullIndexingSourceConfig().getDBSourceConfigList();
		for(DBSourceConfig config : sourceList){
			System.out.println(config);
			System.out.println(config.isActive());
		}
		List<FileSourceConfig> fileSourceList = dataSourceConfig.getFullIndexingSourceConfig().getFileSourceConfigList();
		for(FileSourceConfig config : fileSourceList){
			System.out.println(config);
			System.out.println(config.isActive());
		}
		
		
		List<DBSourceConfig> sourceList2 = dataSourceConfig.getAddIndexingSourceConfig().getDBSourceConfigList();
		for(DBSourceConfig config : sourceList2){
			System.out.println(config);
			System.out.println(config.isActive());
		}
		List<FileSourceConfig> fileSourceList2 = dataSourceConfig.getAddIndexingSourceConfig().getFileSourceConfigList();
		for(FileSourceConfig config : fileSourceList2){
			System.out.println(config);
			System.out.println(config.isActive());
		}
		
		List<JDBCSourceInfo> sourceInfoList = dataSourceConfig.getJdbcSourceInfoList();
		for(JDBCSourceInfo jdbcSourceInfo : sourceInfoList){
			System.out.println(jdbcSourceInfo.getId());
			System.out.println(jdbcSourceInfo.getName());
			System.out.println(jdbcSourceInfo.getDriver());
			System.out.println(jdbcSourceInfo.getUrl());
			System.out.println(jdbcSourceInfo.getUser());
			System.out.println(jdbcSourceInfo.getPassword());
		}
	}

}
