package org.fastcatsearch.settings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;


public class Settings {
	private static final int K = 1024;
	private static final int M = K * K;
	private static final int G = K * K * K;
	
	Map<String, Object> map;
	
	public Settings(){
		map = new HashMap<String, Object>();
	}
	public Settings(Map<String, Object> map) {
		this.map = map;
	}
	public Settings getSubSettings(String... keys) {
		return getSubSettings(false, keys);
	}
	public Settings getCopiedSubSettings(String... keys) {
		return getSubSettings(true, keys);
	}
	public synchronized Settings getSubSettings(boolean copy, String... keys) {
		Map<String, Object> workMap = map;
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			Object value = workMap.get(key);
			if(value == null){
				return null;
			}
			if(value instanceof Map){
				workMap = (Map<String, Object>) value; 
			}else{
				return null;
			}
		}
		if(copy){
			return new Settings(workMap);
		}else{
			return new Settings(new HashMap<String, Object>(workMap));
		}
	}

	public int getInt(String... keys) {
		return getInt(-1, keys);
	}

	public long getLong(String keys) {
		return getLong(-1, keys);
	}

	public float getFloat(String keys) {
		return getFloat(-1, keys);
	}

	public double getDouble(String keys) {
		return getDouble(-1, keys);
	}

	public boolean getBoolean(String keys) {
		return getBoolean(false, keys);
	}
	public int getInt(int defaultValue, String... keys){
		String value = getString(keys);
		if(value == null){
			return defaultValue;
		}else{
			try{
				return Integer.parseInt(value);
			}catch(NumberFormatException e){
				return defaultValue;
			}
		}
	}
	public long getLong(long defaultValue, String... keys){
		String value = getString(keys);
		if(value == null){
			return defaultValue;
		}else{
			try{
				return Long.parseLong(value);
			}catch(NumberFormatException e){
				return defaultValue;
			}
		}
	}
	public float getFloat(float defaultValue, String... keys){
		String value = getString(keys);
		if(value == null){
			return defaultValue;
		}else{
			try{
				return Float.parseFloat(value);
			}catch(NumberFormatException e){
				return defaultValue;
			}
		}
	}
	public double getDouble(double defaultValue, String... keys){
		String value = getString(keys);
		if(value == null){
			return defaultValue;
		}else{
			try{
				return Double.parseDouble(value);
			}catch(NumberFormatException e){
				return defaultValue;
			}
		}
	}
	public boolean getBoolean(boolean defaultValue, String... keys){
		String value = getString(keys);
		if(value == null){
			return defaultValue;
		}else{
			return Boolean.parseBoolean(value);
		}
	}
	
	public String getString(String... keys){
		Object value = getValue(keys);
		if(value != null){
			return value.toString();
		}else{
			return null;
		}
	}
	
	public List<Object> getList(String... keys){
		Object value =  getValue(keys);
		if(value instanceof List){
			return (List<Object>) value;
		}else{
			return null;
		}
	}
	
	
	public synchronized Object getValue(String... keys){
		Map<String, Object> workMap = this.map;
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			Object value = workMap.get(key);
			if(value == null){
				return null;
			}
			if(value instanceof Map){
				workMap = (Map<String, Object>) value;
			}else{
				return value;
			}
		}
		
		return null;
	}
	
	public synchronized String toString(){
		DumperOptions options = new DumperOptions();
		options.setWidth(50);
		options.setIndent(4);
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		Yaml yaml = new Yaml(options);
		
        return yaml.dump(map);
	}
	
	
	public long getByteSize(long defaultValue, String... keys){
		String str = getString(keys);
		if(str == null)
			return defaultValue;
		
		str = str.trim();
		int len = str.length();
		try{
			if(len > 0){
				char suffix = str.charAt(len - 1);
				if(suffix == 'g' || suffix == 'G'){
					return Long.parseLong(str.substring(0, len - 1).trim()) * G;
				}else if(suffix == 'm' || suffix == 'M'){
					return Long.parseLong(str.substring(0, len - 1).trim()) * M;
				}else if(suffix == 'k' || suffix == 'K'){
					return Long.parseLong(str.substring(0, len - 1).trim()) * K;
				}else if(suffix == 'b' || suffix == 'B'){
					return Long.parseLong(str.substring(0, len - 1).trim());
				}else{
					return Long.parseLong(str);
				}
			}else{
				return defaultValue;
			}
		}catch(NumberFormatException e){
			
			return defaultValue;
		}
	}
	public synchronized void putValueKey(Object newValue, String... keys) {
		Map<String, Object> workMap = this.map;
		
		for (int i = 0; i < keys.length; i++) {
			boolean isLeaf = (i == keys.length - 1);
			String key = keys[i];
			if(isLeaf){
				workMap.put(key, newValue);
				return;
			}
			
			Object value = workMap.get(key);
			if(value == null || !(value instanceof Map)){
				//키가 없다면 하위맵을 만들어준다.
				Map<String, Object> newWorkMap = new HashMap<String, Object>();
				workMap.put(key, newWorkMap);
				workMap = newWorkMap; 
			}else{
				workMap = (Map<String, Object>) value;
			}
		}
	}

	
	
}
