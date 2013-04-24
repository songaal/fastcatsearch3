package org.fastcatsearch.settings;

import static org.junit.Assert.assertEquals;

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
	
	@Test
	public void testOverrideMap() throws IOException {
		
		Map<String, Object> serviceSettings = new HashMap<String, Object>();
		serviceSettings.put("node", 123);
		Map<String, Object> el1 = new HashMap<String, Object>();
		el1.put("name", "ids");
		el1.put("size", 5);
		Map<String, Object> el2 = new HashMap<String, Object>();
		el2.put("name", "id3");
		el2.put("size", 5);
		Map<String, Object> childMap = new HashMap<String, Object>();
		childMap.put("childName", "hahaha");
		childMap.put("shard", "8");
		el2.put("data", childMap);
		
		serviceSettings.put("el1", el1);
		serviceSettings.put("el2", el2);
		Settings s1 = new Settings(serviceSettings);
		
		Map<String, Object> serviceSettings2 = new HashMap<String, Object>();
		serviceSettings2.put("node", 123);
		Map<String, Object> el12 = new HashMap<String, Object>();
		el12.put("name", "id");
		el12.put("size", 25);
		el12.put("byout", "ttl");
		Map<String, Object> el22 = new HashMap<String, Object>();
		el22.put("name", "id4-mod");
		el22.put("size", 55);
		el22.put("additionKey", "-abc");
		Map<String, Object> childMap2 = new HashMap<String, Object>();
		childMap2.put("childName", "hahaha-mod");
		childMap2.put("shard", "8");
		childMap2.put("replica", "0");
		el22.put("data", childMap2);

		
		Map<String, Object> el23 = new HashMap<String, Object>();
		el23.put("name", "id43");
		
		serviceSettings2.put("el1", el12);
		serviceSettings2.put("el2", el22);
		serviceSettings2.put("el3", el23);
		Settings s2 = new Settings(serviceSettings2);
		System.out.println("원본 : " + s1.map);
		
		Settings s1_ = s1.overrideSettings(s2);
		System.out.println("수정 : " + s2.map);
		System.out.println("결과 : " + s1_.map);
		
		System.out.println(s1_.getValue("el2"));

		System.out.println("===============");
		System.out.println(s1_);
		assertEquals(s1_.getString("el2.name"), "id4-mod");
		
		
	}

}
