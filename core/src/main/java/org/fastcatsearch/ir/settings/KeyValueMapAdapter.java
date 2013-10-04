package org.fastcatsearch.ir.settings;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.fastcatsearch.ir.config.AdaptedProperties;
import org.fastcatsearch.ir.config.AdaptedProperties.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyValueMapAdapter extends XmlAdapter<AdaptedProperties, Map<String, String>> {

	protected static final Logger logger = LoggerFactory.getLogger(KeyValueMapAdapter.class);
	
	@Override
    public Map<String, String> unmarshal(AdaptedProperties adaptedProperties) throws Exception {
		if(null == adaptedProperties) {
            return null;
        }
		
		Map<String, String> map = new HashMap<String, String>(adaptedProperties.property.size());
        for(Property property : adaptedProperties.property) {
            map.put(property.getKey(), property.getValue());
        }
        return map;
    }

    @Override
    public AdaptedProperties marshal(Map<String, String> map) throws Exception {
    	if(map == null) {
            return null;
        }
        AdaptedProperties adaptedProperties = new AdaptedProperties();
        for(Entry<String,String> entry : map.entrySet()) {
            adaptedProperties.property.add(new Property(entry.getKey(), entry.getValue()));
        }
        return adaptedProperties;
    }

}
