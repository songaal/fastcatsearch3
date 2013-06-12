package org.fastcatsearch.ir.setting;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

public class JAXBSchemaSettingTest {

	@Test
	public void testSettingMarshall() throws JAXBException {
		SchemaSetting setting = new SchemaSetting();
		
		//add fieldsetting
		List<FieldSetting> fieldSettingList = new ArrayList<FieldSetting>();
		FieldSetting fieldSetting = new FieldSetting("title", "제목",  FieldSetting.Type.ACHAR);
		
		fieldSettingList.add(fieldSetting);
		setting.setFieldSettingList(fieldSettingList);
		
		
		
		setting.setPrimaryKeySettingList(new ArrayList<PrimaryKeySetting>());
		setting.getPrimaryKeySettingList().add(new PrimaryKeySetting("title"));
		setting.getPrimaryKeySettingList().add(new PrimaryKeySetting("category"));
		List<RefSetting> indexFieldSettingList = new ArrayList<RefSetting>();
		RefSetting indexFieldSetting = new RefSetting("title");
		indexFieldSettingList.add(indexFieldSetting);
		
		List<IndexSetting> indexSettingList = new ArrayList<IndexSetting>();
		IndexSetting indexSetting = new IndexSetting("title_index", indexFieldSettingList, "korean","org.fastcatsearch.ir.analysis.KoreanTokenizer");
		indexSettingList.add(indexSetting);
		setting.setIndexSettingList(indexSettingList);
		
		List<FieldIndexSetting> fieldIndexSettingList = new ArrayList<FieldIndexSetting>();
		FieldIndexSetting fieldIndexSetting = new FieldIndexSetting("title", "title field index");
		fieldIndexSetting.setRefList(new ArrayList<RefSetting>());
		fieldIndexSetting.getRefList().add(new RefSetting("title"));
		fieldIndexSetting.getRefList().add(new RefSetting("content"));
		fieldIndexSettingList.add(fieldIndexSetting);
		setting.setFieldIndexSettingList(fieldIndexSettingList);
		
		
		List<GroupIndexSetting> groupSettingList = new ArrayList<GroupIndexSetting>();
		GroupIndexSetting groupIndexSetting = new GroupIndexSetting("category", "category_group");
		groupIndexSetting.setRefList(new ArrayList<RefSetting>());
		groupIndexSetting.getRefList().add(new RefSetting("category"));
		groupIndexSetting.getRefList().add(new RefSetting("tags"));
		groupSettingList.add(groupIndexSetting);
		setting.setGroupIndexSettingList(groupSettingList);
		
		List<AnalyzerSetting> analyzerSettingList = new ArrayList<AnalyzerSetting>();
		analyzerSettingList.add(new AnalyzerSetting("korean_index","korean_index", 10, 100, "com.fastcatsearch.plugin.analysis.korean.StandardKoreanAnalyzer"));
		analyzerSettingList.add(new AnalyzerSetting("korean_query","korean_query", 10, 100, "com.fastcatsearch.plugin.analysis.korean.StandardKoreanAnalyzer"));
		setting.setAnalyzerSettingList(analyzerSettingList);
		
		
		JAXBContext context = JAXBContext.newInstance(setting.getClass());
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(setting, System.out);
	}
	
	@Test
	public void testSettingUnmarshall() throws JAXBException, IOException {
		InputStream is = getClass().getResourceAsStream("schema.xml");
		
		JAXBContext context = JAXBContext.newInstance(SchemaSetting.class);
		
		Unmarshaller unmarshaller = context.createUnmarshaller();

		SchemaSetting setting = (SchemaSetting) unmarshaller.unmarshal(is);
		
		is.close();
		
		System.out.println(setting);
	}

}
