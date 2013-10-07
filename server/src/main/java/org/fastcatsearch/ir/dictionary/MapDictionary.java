package org.fastcatsearch.ir.dictionary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.store.InputStreamDataInput;
import org.apache.lucene.store.OutputStreamDataOutput;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
/*
 * map 범용 사전. 
 * keyword, value 의 필드를 가지고 있다.
 * 
 * 
 * */
public class MapDictionary extends SourceDictionary implements ReadableDictionary {

	protected Map<CharVector, CharVector[]> map;
	

	public MapDictionary(boolean ignoreCase) {
		super(ignoreCase);
		map = new HashMap<CharVector, CharVector[]>();
		
	}

	public MapDictionary(Map<CharVector, CharVector[]> map, boolean ignoreCase) {
		super(ignoreCase);
		this.map = map;
	}

	public MapDictionary(File file) {
		super(true);
		if(!file.exists()){
			map = new HashMap<CharVector, CharVector[]>();
			logger.error("사전파일이 존재하지 않습니다. file={}", file.getAbsolutePath());
			return;
		}
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			readFrom(is);
			is.close();
		} catch (IOException e) {
			logger.error("", e);
		}
	}

	public MapDictionary(InputStream is, boolean ignoreCase) {
		super(ignoreCase);
		try {
			readFrom(is);
		} catch (IOException e) {
			logger.error("", e);
		}
	}

	
	@Override
	public void addEntry(String keyword, String[] values) {
		if(keyword == null || keyword.length() == 0) {
			return;
		}
		
		if(ignoreCase){
			keyword = keyword.toUpperCase();
		}
		
		CharVector[] list = new CharVector[values.length];
		for (int i = 0; i < values.length; i++) {
			String value = values[i];
			if(ignoreCase){
				value = value.toUpperCase();
			}
			list[i] = new CharVector(value);
		}
		map.put(new CharVector(keyword), list);
	}

	public Map<CharVector, CharVector[]> getMap() {
		return Collections.unmodifiableMap(map);
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		
		DataOutput output = new OutputStreamDataOutput(out);
		Iterator<CharVector> keySet = map.keySet().iterator();
		//write size of map
		output.writeVInt(map.size());
		//write key and value map
		for(;keySet.hasNext();) {
			//write key
			CharVector key = keySet.next();
			output.writeUString(key.array, key.start, key.length);
			//write values
			CharVector[] values = map.get(key);
			output.writeVInt(values.length);
			for(CharVector value : values) {
				output.writeUString(value.array, value.start, value.length);
			}
		}
		
		
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		DataInput input = new InputStreamDataInput(in);
		
		map = new HashMap<CharVector, CharVector[]>();
		
		int size = input.readVInt();

		for(int entryInx=0;entryInx < size; entryInx++) {
			CharVector key = new CharVector(input.readUString());
			
			int valueLength = input.readVInt();
			
			CharVector[] values = new CharVector[valueLength];
			
			for(int valueInx=0; valueInx < valueLength; valueInx++) {
				values[valueInx] = new CharVector(input.readUString());
			}
			map.put(key, values);
		}
		
		
	}

	@Override
	public List<CharVector> find(CharVector token) {
		if(map.containsKey(token)) {
			return Arrays.asList(map.get(token));
		}
		return null;
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public void addSourceLineEntry(String line) {
		String[] kv= line.split("\t");
		if(kv.length == 1){
			String value = kv[0].trim();
			addEntry(null, new String[]{ value });
		}else if(kv.length == 2){
			String keyword = kv[0].trim();
			String value = kv[1].trim();
			addEntry(keyword, new String[]{ value });
		}
	}

	@Override
	public void addMapEntry(Map<String, Object> vo) {
		addEntry((String) vo.get("keyword"), new String[]{ (String) vo.get("value") });
	}
}