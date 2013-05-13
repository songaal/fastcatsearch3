package org.fastcatsearch.plugin;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

public class PluginSettingTest {

	@Test
	public void testProperty() throws JAXBException {

		Map<String, String> properties = new HashMap<String, String>();
		properties.put("aaa", "1111");
		properties.put("bbb", "2222");
		PluginSetting setting = new PluginSetting();
		setting.setProperties(properties);

		JAXBContext jc = JAXBContext.newInstance(PluginSetting.class);
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
				+ "	\n"
				+ "	<db>\n"
				+ "		<dao-list>\n"
				+ "			<dao name=\"SynonymDictionary\">org.fastcatsearch.db.dao.SetDictionary</dao>\n"
				+ "			<dao name=\"UserDictionary\">org.fastcatsearch.db.dao.SetDictionary</dao>\n"
				+ "			<dao name=\"StopDictionary\">org.fastcatsearch.db.dao.SetDictionary</dao>\n"
				+ "		</dao-list>\n"
				+ "	</db>\n" + "</plugin>";
		JAXBContext jc = JAXBContext.newInstance(PluginSetting.class);

		Unmarshaller unmarshaller = jc.createUnmarshaller();

		PluginSetting setting = (PluginSetting) unmarshaller.unmarshal(new StringReader(xml));

		System.out.println(setting.getWeb().getUser().getServlet().getPath());
		System.out.println(setting.getDB().getDAOList().size());

		StringWriter writer = new StringWriter();
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(setting, writer);

		System.out.println(writer.toString());
	}

}
