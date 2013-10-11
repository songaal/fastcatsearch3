package org.fastcatsearch.plugin;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

public class PluginSettingTest {

	@Test
	public void testProperty() throws JAXBException {

		DefaultPluginSetting setting = new DefaultPluginSetting();

		JAXBContext jc = JAXBContext.newInstance(DefaultPluginSetting.class);
		StringWriter writer = new StringWriter();
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(setting, writer);

		System.out.println(writer.toString());
	}

	@Test
	public void testReadAndWrite() throws JAXBException {

		String xml = "<plugin id=\"Product\" namespace=\"Analysis\" class=\"com.fastcatsearch.ir.analysis.product.ProductAnalyzerPlugin\">\n"
				+ "	<properties>\n"
				+ "		<property key=\"synonym.dict.path\">dict/synonym.dict</property>\n"
				+ "		<property key=\"user.dict.path\">dict/user.dict</property>\n"
				+ "		<property key=\"stop.dict.path\">dict/stop.dict</property>\n"
				+ "	</properties>\n"
				+ "	\n"
				+ "	<web>\n"
				+ "		<user>\n"
				+ "			<menu ref=\"dictionary\" categoryLabel=\"상품명사전\"/>\n"
				+ "			<servlet path=\"/analyzer/dic/product\">com.fastcatsearch.ir.analysis.product.servlet.SynonymDictionaryServlet</servlet>\n"
				+ "		</user>\n"
				+ "		<admin>\n"
				+ "			<menu ref=\"analyzer\" categoryLabel=\"상품명분석기\"/>\n"
				+ "			<servlet></servlet>\n"
				+ "		</admin>\n"
				+ "	</web>\n"
				+ "	<analyzer-list>\n"
				+ "		<analyzer id=\"KoreanAnalyzer\" name=\"한국어분석기\">com.fastcatsearch.plugin.analysis.ko.standard.StandardKoreanAnalyzer</analyzer>\n" 
				+ "	</analyzer-list>"
				+ "	\n"
				+ "	<db>\n"
				+ "		<dao-list>\n"
				+ "			<dao name=\"SynonymDictionary\">org.fastcatsearch.db.dao.SetDictionary</dao>\n"
				+ "			<dao name=\"UserDictionary\">org.fastcatsearch.db.dao.SetDictionary</dao>\n"
				+ "			<dao name=\"StopDictionary\">org.fastcatsearch.db.dao.SetDictionary</dao>\n"
				+ "		</dao-list>\n"
				+ "	</db>\n" + "</plugin>";
		
		System.out.println(xml);
		JAXBContext jc = JAXBContext.newInstance(PluginSetting.class);

		Unmarshaller unmarshaller = jc.createUnmarshaller();

		PluginSetting setting = (PluginSetting) unmarshaller.unmarshal(new StringReader(xml));

		System.out.println("getAnalyzerList >> "+setting.getActionList().size());

		StringWriter writer = new StringWriter();
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(setting, writer);

		System.out.println(writer.toString());
	}

}
