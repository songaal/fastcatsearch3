package org.fastcatsearch.settings;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.fastcatsearch.settings.SearchPageSettings.SearchCategorySetting;
import org.fastcatsearch.util.JAXBConfigs;
import org.junit.Test;

public class SearchPageSettingsTest {

	@Test
	public void testWrite() throws JAXBException {
		SearchPageSettings s = new SearchPageSettings();
		List<SearchCategorySetting> searchCategorySettingList = new ArrayList<SearchCategorySetting>();
		SearchCategorySetting cs = new SearchCategorySetting();
		cs.setBodyField("<span>$body</span>");
		searchCategorySettingList.add(cs);
		s.setSearchCategorySettingList(searchCategorySettingList);
		
		Writer writer = new StringWriter();
		
		JAXBConfigs.writeRawConfig(writer, s, SearchPageSettings.class);
		
		System.out.println(writer.toString());
	}
	
	@Test
	public void testRead() throws JAXBException {
		String data = "<span color=\"text-danger $id\">\n\n$body</span>";
		System.out.println(">>>>" + data.replaceAll("\\$body",">>>>>"));
		String fieldIdPattern = "\\$[a-zA-Z_-]+";
		Pattern patt = Pattern.compile(fieldIdPattern);
		Matcher matcher = patt.matcher(data);
		int i = 0;
		while(matcher.find()){
			String g = matcher.group();
			System.out.println(i++ + " > " + g.substring(1));
		}
		
		data = StringEscapeUtils.escapeHtml4(data);
		String xml = "<search-page>" +
    "<search-category-list>" +
    "    <search-category>" +
    "        <body-field>" +data+ "</body-field>" +
    //"        <body-field>&lt;span&gt;$body&lt;/span&gt;</body-field>" +
    "    </search-category>" +
    "</search-category-list>" +
    "<search-list-size>0</search-list-size>" +
    "<total-search-list-size>0</total-search-list-size>" +
    "</search-page>";
		
		
		Reader reader = new StringReader(xml);
		
		SearchPageSettings searchPageSettings = JAXBConfigs.readConfig(reader, SearchPageSettings.class);
		for(SearchCategorySetting cs : searchPageSettings.getSearchCategorySettingList()){
			System.out.println(cs.getBodyField());
		}
		
	}

}
