package org.fastcatsearch.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class MapAdapter extends XmlAdapter<MapAdapter.AdaptedMap, Map<String, String>> {
    
    public static class AdaptedMap {
         
        public List<Entry> entry = new ArrayList<Entry>();
  
    }
     
    public static class Entry {
         
        public String key;
         
        public String value;
   
    }
 
    @Override
    public Map<String, String> unmarshal(AdaptedMap adaptedMap) throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        for(Entry entry : adaptedMap.entry) {
            map.put(entry.key, entry.value);
        }
        return map;
    }
 
    @Override
    public AdaptedMap marshal(Map<String, String> map) throws Exception {
        AdaptedMap adaptedMap = new AdaptedMap();
        for(Map.Entry<String, String> mapEntry : map.entrySet()) {
            Entry entry = new Entry();
            entry.key = mapEntry.getKey();
            entry.value = mapEntry.getValue();
            adaptedMap.entry.add(entry);
        }
        return adaptedMap;
    }
 
}
