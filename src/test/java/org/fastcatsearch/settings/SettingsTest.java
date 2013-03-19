package org.fastcatsearch.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

public class SettingsTest {

	@Test
	public void testCustomMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("id", "123-343");
		Map<String, Object> serviceSettings = new HashMap<String, Object>();
		serviceSettings.put("node", 123);
		serviceSettings.put("name", "dbservice");
		
		map.put("service", serviceSettings);
		
		Settings settings = new Settings(map);
		System.out.println(settings.toString());
		
		System.out.println("---");
		String value = settings.getString("service", "node");
		System.out.println(value);
		System.out.println("---");
		value = settings.getString("service");
		System.out.println(value);
		System.out.println("---");
		Settings subSettings = settings.getSubSettings("service");
		System.out.println(subSettings.toString());
		
		
	}
	
	
	@Test
	public void testLoadYamlSetting() throws IOException {
		InputStream input = new FileInputStream(new File("src/test/resources/test.yaml"));
        Yaml yaml = new Yaml();
        Map<String, Object> data = (Map<String, Object>) yaml.load(input);
        input.close();
        Settings settings = new Settings(data);
		System.out.println(settings);
	}
	
	@Test
	public void testUseYamlSetting() throws IOException {
		InputStream input = new FileInputStream(new File("src/test/resources/test.yaml"));
        Yaml yaml = new Yaml();
        Map<String, Object> data = (Map<String, Object>) yaml.load(input);
        input.close();
        Settings settings = new Settings(data);
        System.out.println(settings);
        System.out.println(settings.getString("service", "server", "host"));
        System.out.println(settings.getString("module"));
        System.out.println(settings.getString("module", "transport", "node_list"));
        Settings transportSettings = settings.getSubSettings("module", "transport");
        System.out.println("sub=>\n"+transportSettings);
        
        List<Object> nodeList = transportSettings.getList("node_list");
        System.out.println(nodeList);
	}
	

}
