package org.fastcatsearch.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GsonTest {

	@Test
	public void testGson() {
		List<Object> list = new ArrayList<Object>();
		Map<String, Object> el1 = new HashMap<String, Object>();
		el1.put("name", "id");
		el1.put("size", 5);
		list.add(el1);
		Map<String, Object> el2 = new HashMap<String, Object>();
		el2.put("name", "id2");
		el1.put("size", 10);
		list.add(el2);
		Map<String, Object> el3 = new HashMap<String, Object>();
		el2.put("name", "id3");
		el1.put("size", new int[]{1,2,3,4,5});
		el1.put("list", new String[]{"a","b","c"});
		list.add(el3);
		
		Gson gson = new Gson();
		String jsonStr = gson.toJson(list);
		System.out.println(jsonStr);
		
		JsonParser parser = new JsonParser();
		System.out.println("--");
		JsonElement el = parser.parse(jsonStr);
		JsonArray arr = el.getAsJsonArray();
		JsonObject obj = arr.get(0).getAsJsonObject();
		System.out.println("obj >> "+obj);
		JsonArray arr2 = obj.get("list").getAsJsonArray();
		System.out.println("el >> "+arr2);
		for(JsonElement elem : arr2) {
			System.out.println(">>"+elem.getAsString());
		}

	}

}
