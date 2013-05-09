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
	public void test() throws JAXBException {
		
		String xml = "<plugin name=\"product_analyzer\" class=\"com.fastcatsearch.ir.analysis.product.ProductAnalyzerPlugin\">\n" + 
				"	<params>\n" + 
				"		<param key=\"load\">true</param>\n" + 
				"		<param key=\"path\">dic/product.dic</param>\n" + 
				"	</params>\n" + 
				"	<web>\n" + 
				"		<user>\n" + 
				"			<menu>\n" + 
				"				<submenu id=\"product\" label=\"1\">\n" + 
				"					<leaf id=\"synonym\" label=\"1\"/>\n" + 
				"					<leaf id=\"userword\" label=\"1\"/>\n" + 
				"					<leaf id=\"stopword\" label=\"1\"/>\n" + 
				"					<leaf id=\"system\" label=\"1\"/>\n" + 
				"				</submenu>\n" + 
				"			</menu>\n" + 
				"			<servlet path=\"/analyzer/dic/product\">com.fastcatsearch.ir.analysis.product.servlet.SynonymDictionaryServlet</servlet>\n" + 
				"		</user>\n" + 
				"		<admin>\n" + 
				"			<menu></menu>\n" + 
				"			<servlet></servlet>\n" + 
				"		</admin>\n" + 
				"	</web>\n" + 
				"	\n" + 
				"	<db>\n" + 
				"		<dao-list>\n" + 
				"			<dao>com.fastcatsearch.db.ProductSynonymDic</dao>\n" + 
				"			<dao>com.fastcatsearch.db.ProductSynonymDic2</dao>\n" + 
				"		</dao-list>	\n" + 
				"	</db>\n" + 
				"</plugin>"; 
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
