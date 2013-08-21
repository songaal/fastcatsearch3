package org.fastcatsearch.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class QueryMap extends HashMap<String, String> implements Streamable {

	private static final long serialVersionUID = 6450718773572982562L;
	private String queryString;
	
	public QueryMap(){
	}
	
	public QueryMap(Map<String, String> map) {
		super(map);
	}

	public String queryString(){
		if(queryString == null){
			queryString = "";
			int i = 0;
			for(Map.Entry<String, String> entry : entrySet()){
				if(i++ > 0){
					queryString += "&";
				}
				queryString += entry.toString();
			}
		}
		
		return queryString;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		putAll((Map<String, String>) input.readGenericValue());
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeGenericValue(this);
	}

}
