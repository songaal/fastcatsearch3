package org.fastcatsearch.ir.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlType
public class AdaptedProperties {
	
	public List<Property> property = new ArrayList<Property>();
	
	public static class Property {
		private String key;
		private String value;
	
		public Property(){
		}
		
		public Property(String key, String value){
			this.key = key;
			this.value = value;
		}
		
		@XmlAttribute(required = true)
		public String getKey() {
			return key;
		}
	
		public void setKey(String key) {
			this.key = key;
		}
	
		@XmlValue
		public String getValue() {
			return value;
		}
	
		public void setValue(String value) {
			this.value = value;
		}
	}
	
}
