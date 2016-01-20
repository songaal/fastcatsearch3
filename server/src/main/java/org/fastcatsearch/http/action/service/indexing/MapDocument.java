package org.fastcatsearch.http.action.service.indexing;

import com.google.gson.JsonElement;

import java.util.Map;

/**
 * Created by swsong on 2016. 1. 14..
 */
public class MapDocument {
    private char type;
    private Map<String, Object> sourceMap;

    public MapDocument(char type, Map<String, Object> sourceMap) {
        this.type = type;
        this.sourceMap = sourceMap;
    }

    public char getType() {
        return type;
    }

    public Map<String, Object> getSourceMap() {
        return sourceMap;
    }
}
