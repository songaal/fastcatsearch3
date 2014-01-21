package org.fastcatsearch.util;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONWrappedResultWriter {
	
	private JSONObject jsonObj;
	private ResponseWriter writer;
	
	public JSONWrappedResultWriter(JSONObject jsonObj, ResponseWriter writer) {
		this.jsonObj = jsonObj;
		this.writer = writer;
	}
	
	public void wrap() throws JSONException {
		
		this.wrap(jsonObj, writer);
	}
	
	private void wrap(JSONObject jsonObj, ResponseWriter writer) throws JSONException {
		@SuppressWarnings("unchecked")
		Set<String> keySet = jsonObj.keySet();
		
		writer.object();
		
		for( String key : keySet ) {
			
			writer.key(key);
			
			Object value = (Object)jsonObj.opt(key);
			
			if(value instanceof JSONArray) {
				
				this.wrap((JSONArray)value, writer);
				
			} else if(value instanceof JSONObject) {
				
				this.wrap((JSONObject)value, writer);
				
			} else {
				writer.value(value);
			}
		}
		
		writer.endObject();
	}
	
	private void wrap(JSONArray jsonArray, ResponseWriter writer) throws JSONException {
		
		writer.array("array");
		
		for(int inx=0; inx < jsonArray.length(); inx++ ) {
			
			Object value = jsonArray.opt(inx);
			
			if(value instanceof JSONArray) {
				
				this.wrap((JSONArray)value, writer);
				
			} else if(value instanceof JSONObject) {
				
				this.wrap((JSONObject)value, writer);
			} else {
				writer.value(value);
			}
		}
		
		writer.endArray();
	}
}
