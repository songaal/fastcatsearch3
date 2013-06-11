package org.fastcatsearch.ir.setting;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.fastcatsearch.ir.setting.IndexSetting.IndexField;
import org.junit.Test;

public class JAXBSchemaSettingTest {

	@Test
	public void testSettingMarshall() throws JAXBException {
		SchemaSetting setting = new SchemaSetting();
		
		//add fieldsetting
		List<FieldSetting> fieldSettingList = new ArrayList<FieldSetting>();
		FieldSetting fieldSetting = new FieldSetting("title", FieldSetting.Type.ACHAR);
		
		fieldSettingList.add(fieldSetting);
		setting.setFieldSettingList(fieldSettingList);
		
		List<IndexField> indexFieldSettingList = new ArrayList<IndexField>();
		IndexField indexFieldSetting = new IndexField("title");
		indexFieldSettingList.add(indexFieldSetting);
		
		List<IndexSetting> indexSettingList = new ArrayList<IndexSetting>();
		IndexSetting indexSetting = new IndexSetting("korean", indexFieldSettingList, "korean","org.fastcatsearch.ir.analysis.KoreanTokenizer");
		indexSettingList.add(indexSetting);
		setting.setIndexSettingList(indexSettingList);
		
//		List<SortSetting> sortSettingList = new ArrayList<SortSetting>();
//		SortSetting sortSetting = new SortSetting("title", fieldSetting, 4, 4 );
//		sortSettingList.add(sortSetting);
//		setting.setSortSettingList(sortSettingList);
//		
//		List<ColumnSetting> columnSettingList = new ArrayList<ColumnSetting>();
//		ColumnSetting columnSetting = new ColumnSetting("title", fieldSetting, false );
//		columnSettingList.add(columnSetting);
//		setting.setColumnSettingList(columnSettingList);
		
		List<GroupIndexSetting> groupSettingList = new ArrayList<GroupIndexSetting>();
		GroupIndexSetting groupSetting = new GroupIndexSetting("title", "title_group", fieldSetting);
		groupSettingList.add(groupSetting);
		setting.setGroupSettingList(groupSettingList);
		
//		List<FilterSetting> filterSettingList = new ArrayList<FilterSetting>();
//		FilterSetting filterSetting = new FilterSetting("title", fieldSetting);
//		filterSettingList.add(filterSetting);
//		setting.setFilterSettingList(filterSettingList);
		
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
