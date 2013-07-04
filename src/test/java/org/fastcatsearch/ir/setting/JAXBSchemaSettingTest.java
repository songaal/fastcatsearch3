package org.fastcatsearch.ir.setting;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.fastcatsearch.ir.settings.AnalyzerSetting;
import org.fastcatsearch.ir.settings.FieldIndexSetting;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.GroupIndexSetting;
import org.fastcatsearch.ir.settings.IndexSetting;
import org.fastcatsearch.ir.settings.PkRefSetting;
import org.fastcatsearch.ir.settings.PrimaryKeySetting;
import org.fastcatsearch.ir.settings.RefSetting;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.junit.Test;

public class JAXBSchemaSettingTest {

	@Test
	public void testSettingMarshall() throws JAXBException {
		SchemaSetting setting = new SchemaSetting();
		
		//add fieldsetting
		List<FieldSetting> fieldSettingList = new ArrayList<FieldSetting>();
		FieldSetting fieldSetting = new FieldSetting("title", "제목",  FieldSetting.Type.ASTRING);
		
		fieldSettingList.add(fieldSetting);
		setting.setFieldSettingList(fieldSettingList);
		
		
		PrimaryKeySetting primaryKeySetting = new PrimaryKeySetting("id");
		primaryKeySetting.setFieldList(new ArrayList<PkRefSetting>());
		primaryKeySetting.getFieldList().add(new PkRefSetting("id"));
		primaryKeySetting.getFieldList().add(new PkRefSetting("price"));
		setting.setPrimaryKeySetting(primaryKeySetting);
		
		List<RefSetting> indexFieldSettingList = new ArrayList<RefSetting>();
		RefSetting indexFieldSetting = new RefSetting("title");
		indexFieldSettingList.add(indexFieldSetting);
		
		List<IndexSetting> indexSettingList = new ArrayList<IndexSetting>();
		IndexSetting indexSetting = new IndexSetting("title_index","korean","korean");
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
		InputStream is = new FileInputStream("src/test/resources/schema.xml");
		InputStreamReader r = new InputStreamReader(is);
		CharBuffer buf = CharBuffer.allocate(1024 * 1024);
		r.read(buf);
		r.close();
		buf.flip();
		System.out.println(new String(buf.array(), 0, buf.limit()));
		
		
		is = new FileInputStream("src/test/resources/schema.xml");
		
		JAXBContext context = JAXBContext.newInstance(SchemaSetting.class);
		
		Unmarshaller unmarshaller = context.createUnmarshaller();

		SchemaSetting setting = (SchemaSetting) unmarshaller.unmarshal(is);
		
		is.close();
		
		System.out.println(setting);
		System.out.println(setting.getFieldSettingList().get(0).getId());
		System.out.println(setting.getAnalyzerSettingList().size());
	}

}
