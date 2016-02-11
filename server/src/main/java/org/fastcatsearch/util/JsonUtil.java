package org.fastcatsearch.util;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.minidev.json.JSONAwareEx;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.writer.JsonReader;
import net.minidev.json.writer.JsonReaderI;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minidev.json.parser.JSONParser.MODE_JSON_SIMPLE;

/**
 * Created by swsong on 2015. 8. 16..
 */
public class JsonUtil {

    public static final SimpleModule module = new SimpleModule();
    static {
        module.addKeyDeserializer(String.class, new KeyDeserializer() {
            @Override
            public Object deserializeKey(String key, DeserializationContext ctxt)
            throws IOException
            {
                return key.toLowerCase();
            }
        });
    }

    public static Map<String, Object> json2Object(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<HashMap<String, Object>> typeReference = new TypeReference<HashMap<String, Object>>() { };
        return objectMapper.readValue(json, typeReference);
    }

    public static Map<String, Object> json2ObjectWithLowercaseKey(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);
        TypeReference<HashMap<String, Object>> typeReference = new TypeReference<HashMap<String, Object>>() { };
        return objectMapper.readValue(json, typeReference);
    }

    public static <T> T json2Object(String json, Class<T> clazz) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Object o = objectMapper.readValue(json, clazz);
        return (T) o;
    }

    static String dateFormat = "yyyy-MM-dd hh:mm:ss";
    public static String object2String(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return mapper.writer(sdf).withDefaultPrettyPrinter().writeValueAsString(object);
    }

    public static JsonNode toJsonNode(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(json);
    }

}
