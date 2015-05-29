package org.fastcatsearch.datasource.reader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * @author Sang Wook, Song
 */
public class JSONFileTest {

	@Test
	public void load() {
		try {
			String path = "/Users/swsong/TEST_HOME/taobao-sample.json";
			File f = new File(path);
			JsonFactory jsonFactory = new JsonFactory();
			ObjectMapper mapper = new ObjectMapper(jsonFactory);
			TypeReference<List<HashMap<String, Object>>> typeRef = new TypeReference<List<HashMap<String, Object>>>() {
			};
			Object docs = mapper.readValue(f, typeRef);

//			HashMap<String, Object> map = (HashMap<String, Object>) docs;
			List<HashMap<String, Object>> items = (List<HashMap<String, Object>>) docs;
			//System.out.println(items.getClass().getCanonicalName());
			for (HashMap<String, Object> o : items) {
				System.out.println(o.get("nid"));
				System.out.println(o.get("category"));
				System.out.println(o.get("title"));
				System.out.println(o.get("pic_url"));
				System.out.println(o.get("detail_url"));
				System.out.println(o.get("view_price"));
				System.out.println(o.get("nick"));
				System.out.println("--------------");
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
