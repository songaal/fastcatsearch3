package org.fastcatsearch.http.action.service.indexing;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.fastcatsearch.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by swsong on 2016. 1. 10..
 */
public class JSONRequestReader {

    private static Logger logger = LoggerFactory.getLogger(JSONRequestReader.class);

    public JSONRequestReader() {
    }

    public List<String> readJsonList(String requestBody) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        String line = null;
        List<String> result = new ArrayList<String>();
        while ((line = reader.readLine()) != null) {
            if (line == null) {
                throw new IOException("EOF");
            }
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            try {
                //에러가 없다면 넣는다.
                new JsonParser().parse(line);
                result.add(line);
            } catch (JsonSyntaxException e) {
                logger.error("error while convert text to json : " + line, e);
            }
        }
        return result;
    }

    public List<MapDocument> readMapDocuments(String requestBody) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        String line = null;
        List<MapDocument> result = new ArrayList<MapDocument>();
        while ((line = reader.readLine()) != null) {
            if (line == null) {
                throw new IOException("EOF");
            }
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            //에러가 없다면 넣는다.
            char type = line.charAt(0);
            String document = line.substring(2);
            try {
                result.add(new MapDocument(type, JsonUtil.json2Object(document)));
            } catch (IOException e) {
                logger.error("error while convert json to map : " + document, e);
            }
        }
        return result;
    }

}
