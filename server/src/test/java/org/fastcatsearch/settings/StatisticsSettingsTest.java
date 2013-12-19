package org.fastcatsearch.settings;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.fastcatsearch.settings.StatisticsSettings.Category;
import org.fastcatsearch.util.JAXBConfigs;
import org.junit.Test;

public class StatisticsSettingsTest {

	@Test
	public void test() throws JAXBException {
		StatisticsSettings s = new StatisticsSettings();
		List<Category> categoryList = new ArrayList<Category>();
		categoryList.add(new Category("total", "통합검색", true, true, false));
		s.setCategoryList(categoryList);
		
		StringWriter writer = new StringWriter();
		JAXBConfigs.writeRawConfig(writer, s, StatisticsSettings.class);
		
		System.out.println(writer);
		
		String source = writer.toString();
		
		Reader reader = new StringReader(source);
		
		StatisticsSettings s2 = JAXBConfigs.readConfig(reader, StatisticsSettings.class);
		List<Category> categoryList2 = s2.getCategoryList();
		for(Category category : categoryList2){
			System.out.println(category);
		}
	}

}
