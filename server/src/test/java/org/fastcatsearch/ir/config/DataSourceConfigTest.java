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

	String datasourceConfigXml = "<datasource>\n" + 
			"	\n" + 
			"	<file active=\"true\" >\n" + 
			"		<sourceReader>org.fastcatsearch.datasource.reader.FastcatSearchCollectFileParser\"</sourceReader>\n" + 
			"		<sourceModifier></sourceModifier>\n" + 
			"		\n" + 
			"		<fullFilePath>collection/sample/testData/full</fullFilePath>\n" + 
			"		<incFilePath>collection/sample/testData/inc</incFilePath>\n" + 
			"		<fileEncoding>utf-8</fileEncoding>\n" + 
			"	</file>\n" + 
			"	\n" + 
			"	<file active=\"true\" >\n" + 
			"		<sourceReader>org.fastcatsearch.datasource.reader.FastcatSearchCollectFileParser\"</sourceReader>\n" + 
			"		<sourceModifier></sourceModifier>\n" + 
			"		\n" + 
			"		\n" + 
			"	</file>\n" + 
			"	\n" + 
			"	<db active=\"false\" >" + 
			"	\n" + 
			"		<sourceReader>org.fastcatsearch.datasource.reader.DBReader\"</sourceReader>\n" + 
			"		<sourceModifier></sourceModifier>\n" + 
			"	\n" + 
			"		<jdbc-driver>org.gjt.mm.mysql.Driver</jdbc-driver>\n" + 
			"		<jdbc-url>jdbc:mysql://127.0.0.1:3306/test</jdbc-url>\n" + 
			"		<user>root</user>\n" + 
			"		<password></password>\n" + 
			"		<fetchsize>1000</fetchsize>\n" + 
			"		<bulksize>100</bulksize>\n" + 
			"		<query>\n" + 
			"			<before-inc></before-inc>\n" + 
			"			<before-full></before-full>\n" + 
			"			<after-inc></after-inc>\n" + 
			"			<after-full></after-full>\n" + 
			"			<full-query></full-query>\n" + 
			"			<inc-query></inc-query>\n" + 
			"			<update-id></update-id>\n" + 
			"			<full-bakcup-file></full-bakcup-file>\n" + 
			"			<inc-bakcup-file></inc-bakcup-file>\n" + 
			"			<bakcup-file-encoding>utf-8</bakcup-file-encoding>\n" + 
			"		</query>\n" + 
			"	</db>\n" +
			"	\n" + 
			"</datasource>";
	@Test
	public void testRead() throws IOException, JAXBException {
		InputStream is = new ByteArrayInputStream(datasourceConfigXml.getBytes());
		DataSourceConfig dataSourceConfig = JAXBConfigs.readConfig(is, DataSourceConfig.class);
		List<DBSourceConfig> sourceList = dataSourceConfig.getDBSourceConfigList();
		for(DBSourceConfig config : sourceList){
			System.out.println(config);
			System.out.println(config.isActive());
		}
		List<FileSourceConfig> fileSourceList = dataSourceConfig.getFileSourceConfigList();
		for(FileSourceConfig config : fileSourceList){
			System.out.println(config);
			System.out.println(config.isActive());
		}
	}

}
