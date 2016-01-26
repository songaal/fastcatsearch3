package org.fastcatsearch.util;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * Created by swsong on 2015. 6. 24..
 */
public class JSONTest {
    @Test
    public void testSpecialString() throws JSONException {
        String str = "“3D프린터로 아이디어 형상을 현실로 실현하다”";
        String value2 = JSONObject.valueToString(str);

        System.out.println(str);
        System.out.println(value2);
    }

    @Test
    public void testCustomJSONWriter() throws JSONException {
        StringWriter sw = new StringWriter();
        CustomJSONWriter w = new CustomJSONWriter(sw);

        String str = "“3D프린터로 아이디어 형상을 현실로 실현하다”";
        w.object()
                .key("unicode").value(str)
                .key("no-unicode").value(str, false)
                .endObject();

        System.out.println(sw.toString());

    }

    @Test
    public void test1() throws IOException {
        String json = "{\"Abc\":\"123\", \"DEF\":\"123\", \"ghF\":\"123\"}";
        Map<String, Object> map = JsonUtil.json2ObjectWithLowercaseKey(json);
        for(String key : map.keySet()) {
            System.out.println(key);
        }
    }

}
