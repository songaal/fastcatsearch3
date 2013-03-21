package org.fastcatsearch.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
        System.out.println(settings.getString("service.server.host"));
        System.out.println(settings.getString("module"));
        System.out.println(settings.getString("module.transport.node_list"));
        Settings transportSettings = settings.getSubSettings("module.transport");
        System.out.println("sub=>\n"+transportSettings);
        
        List<Object> nodeList = transportSettings.getList("node_list");
        for (int i = 0; i < nodeList.size(); i++) {
        	Map map = (Map) nodeList.get(i);
        	System.out.println(map);
		}
        System.out.println(nodeList);
	}
	
	@Test
	public void testModifyYamlSetting() throws IOException {
		InputStream input = new FileInputStream(new File("src/test/resources/test.yaml"));
        Yaml yaml = new Yaml();
        Map<String, Object> data = (Map<String, Object>) yaml.load(input);
        input.close();
        Settings settings = new Settings(data);
        System.out.println(settings);
        System.out.println(settings.getString("service.server.host"));
        System.out.println(settings.getString("module"));
        System.out.println(settings.getString("module.transport.node_list"));
        Settings transportSettings = settings.getSubSettings("module.transport");
        transportSettings.put("service.server.host", "fastcatsearch.org");
        transportSettings.put("tcp.delay", 1000);
        transportSettings.put("tcp.option", "nocache");
        System.out.println("---");
        System.out.println("sub=>\n"+transportSettings);
        System.out.println("---");
        List<Object> nodeList = transportSettings.getList("node_list");
        System.out.println(nodeList);
	}
	
	@Test
	public void testMapListSetting() throws IOException {
		Map<String, Object> serviceSettings = new HashMap<String, Object>();
		serviceSettings.put("node", 123);
		serviceSettings.put("name", "dbservice");
		List<Object> list = new ArrayList<Object>();
		Map<String, Object> el1 = new HashMap<String, Object>();
		el1.put("name", "id");
		el1.put("size", 5);
		list.add(el1);
		Map<String, Object> el2 = new HashMap<String, Object>();
		el2.put("name", "id2");
		el2.put("size", 10);
		list.add(el2);
		Map<String, Object> el3 = new HashMap<String, Object>();
		el3.put("name", "id3");
		el3.put("size", new Integer[]{1,2,3,4,5});
		el3.put("list", new String[]{"a","b","c"});
		list.add(el3);
		
		serviceSettings.put("field_list", list);
		
		Settings settings = new Settings(serviceSettings);
		System.out.println(settings.toString());
		
		System.out.println("---");
		List<Settings> list2 = settings.getSettingList("field_list");
		for (int i = 0; i < list2.size(); i++) {
			Settings s = list2.get(i);
			System.out.println("setting-"+i+" >> "+ list2.get(i));
			System.out.println("#names:"+s.getString("name"));
			System.out.println("#size:"+s.getInt("size", 10000));
			
		}
		
		
	}

}
