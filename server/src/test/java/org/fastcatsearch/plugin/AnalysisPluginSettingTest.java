package org.fastcatsearch.plugin;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;


public class AnalysisPluginSettingTest {

	@Test
	public void test() throws JAXBException, IOException {
		JAXBContext analysisJc = JAXBContext.newInstance(PluginSetting.class);
		Unmarshaller analysisUnmarshaller = analysisJc.createUnmarshaller();
		String pluginConfigFile = "/Users/swsong/TEST_HOME/fastcatsearch2_simple/node1/plugin/analysis/korean/plugin.xml";
		InputStream is = new FileInputStream(pluginConfigFile);
		PluginSetting setting = (PluginSetting) analysisUnmarshaller.unmarshal(is);
		is.close();
		
		System.out.println(setting);
	}

}
