package org.fastcatsearch.http.action.service.indexing;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by swsong on 2016. 1. 10..
 */
public class JSONRequestReader {

    public JSONRequestReader() { }

    public List<HashMap<String, Object>> readJsonList(String requestBody) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        String line = null;
        List<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();
        while((line = reader.readLine()) != null) {
            if (line == null) {
                throw new IOException("EOF");
            }
            line = line.trim();
            if(line.length() == 0) {
                continue;
            }

            JsonFactory jsonFactory = new JsonFactory();
            ObjectMapper mapper = new ObjectMapper(jsonFactory);
            TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
            };
            result.add(mapper.<HashMap<String, Object>>readValue(line, typeRef));
        }
        return result;
    }
}
