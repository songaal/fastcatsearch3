package org.fastcatsearch.http.action.service.indexing;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minidev.json.parser.ParseException;
import org.fastcatsearch.util.JSONParser;
import org.fastcatsearch.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by swsong on 2016. 1. 10..
 */
public class JSONRequestReader {

    private static Logger logger = LoggerFactory.getLogger(JSONRequestReader.class);

    private BufferedReader reader;
    private JSONParser jsonParser;

    public JSONRequestReader() {
        jsonParser = new JSONParser();
    }

    public JSONRequestReader(String source) {
        reader = new BufferedReader(new StringReader(source), 1024 * 1024);
        jsonParser = new JSONParser();
    }

    public MapDocument readAsMapDocument() throws IOException {

        String line = reader.readLine();
        if(line == null) {
            return null;
        }
        line = line.trim();
        if (line.length() == 0) {
            return null;
        }

        char type = line.charAt(0);
        String document = line.substring(2);
        try {
            return new MapDocument(type, jsonParser.parse(document));
        } catch (ParseException e) {
            if (document.length() > 1000) {
                document = document.substring(0, 1000) + "...";
            }
            logger.error("error while convert json to map : " + document, e);
        }
        return null;
    }

    public void close() {

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
                jsonParser.parse(line);
                result.add(line);
            } catch (ParseException e) {
                if (line.length() > 1000) {
                    line = line.substring(0, 1000) + "...";
                }
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
                result.add(new MapDocument(type, jsonParser.parse(document)));
            } catch (ParseException e) {
                logger.error("error while convert json to map : " + document, e);
            }
        }
        return result;
    }

}
