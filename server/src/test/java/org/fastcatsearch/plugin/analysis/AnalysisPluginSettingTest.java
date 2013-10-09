package org.fastcatsearch.plugin.analysis;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.fastcatsearch.plugin.PluginSetting;
import org.junit.Test;


public class AnalysisPluginSettingTest {

	@Test
	public void test() throws JAXBException, IOException {
		JAXBContext analysisJc = JAXBContext.newInstance(AnalysisPluginSetting.class);
		Unmarshaller analysisUnmarshaller = analysisJc.createUnmarshaller();
		String pluginConfigFile = "/Users/swsong/TEST_HOME/fastcatsearch2_simple/node1/plugin/analysis/korean/plugin.xml";
		InputStream is = new FileInputStream(pluginConfigFile);
		AnalysisPluginSetting setting = (AnalysisPluginSetting) analysisUnmarshaller.unmarshal(is);
		is.close();
		
		System.out.println(setting);
	}

}
