package org.fastcatsearch.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class OldSettings implements Cloneable {
	private static Logger logger = LoggerFactory.getLogger(OldSettings.class);

	private static final int K = 1024;
	private static final int M = K * K;
	private static final int G = K * K * K;

	Map<String, Object> map;

	public OldSettings() {
		map = new HashMap<String, Object>();
	}

	public OldSettings(Map<String, Object> map) {
		this.map = map;
	}

	public OldSettings overrideSettings(OldSettings settings) {
		return overrideMap(this.map, settings.map);
	}

	/**
	 * sourceMap으로 targetMap의 데이터를 overriding 한다.<br>
	 * map의 value 항목이 primitive type과 Map 데이터인 경우에만 override 된다.<br>
	 * 
	 * @param sourceMap
	 *            override하려는 대상 Map
	 * @param targetMap
	 *            복사하려는 Map
	 * @return OldSettings 파일.
	 * @Author coreawin
	 * @since 2013.03-23
	 */
	private OldSettings overrideMap(Map<String, Object> sourceMap, Map<String, Object> targetMap) {
		Set<Map.Entry<String, Object>> entrySet = targetMap.entrySet();
		for (Entry<String, Object> e : entrySet) {
			String key = e.getKey();
			Object v = e.getValue();
			if (v instanceof Map) {
				if (sourceMap.containsKey(key)) {
					Object sv = sourceMap.get(key);
					Map<String, Object> osv = null;
					if ((sv instanceof Map) == false) {
						osv = new HashMap<String, Object>();
					} else {
						osv = (Map<String, Object>) sv;
					}
					return overrideMap(osv, (Map<String, Object>) v);
				} else {
					sourceMap.put(key, v);
				}
			} else {
				sourceMap.put(key, v);
			}
		}
		return this;
	}

	public OldSettings getSubSettings(String key) {
		return getSubSettings(key, false);
	}

	public OldSettings getCopiedSubSettings(String key) {
		return getSubSettings(key, true);
	}

	public synchronized OldSettings getSubSettings(String key, boolean deepCopy) {
		String[] keys = key.split("\\.");
		Map<String, Object> workMap = map;
		for (int i = 0; i < keys.length; i++) {
			Object value = workMap.get(keys[i]);
			if (value == null) {
				// 하위 요소가 없으면 빈 객체를 넘겨준다.
				return new OldSettings();
			}
			if (value instanceof Map) {
				workMap = (Map<String, Object>) value;
			} else {
				return null;
			}
		}
		if (deepCopy) {
			return new OldSettings(workMap);
		} else {
			return new OldSettings(new HashMap<String, Object>(workMap));
		}
	}

	public int getInt(String key) {
		return getInt(key, -1);
	}

	public long getLong(String key) {
		return getLong(key, -1);
	}

	public float getFloat(String key) {
		return getFloat(key, -1);
	}

	public double getDouble(String key) {
		return getDouble(key, -1);
	}

	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	public String getString(String key) {
		return getString(key, null);
	}

	public int getInt(String key, int defaultValue) {
		String value = getString(key);
		if (value == null) {
			return defaultValue;
		} else {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}
	}

	public long getLong(String key, long defaultValue) {
		String value = getString(key);
		if (value == null) {
			return defaultValue;
		} else {
			try {
				return Long.parseLong(value);
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}
	}

	public float getFloat(String key, float defaultValue) {
		String value = getString(key);
		if (value == null) {
			return defaultValue;
		} else {
			try {
				return Float.parseFloat(value);
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}
	}

	public double getDouble(String key, double defaultValue) {
		String value = getString(key);
		if (value == null) {
			return defaultValue;
		} else {
			try {
				return Double.parseDouble(value);
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		String value = getString(key);
		if (value == null) {
			return defaultValue;
		} else {
			return Boolean.parseBoolean(value);
		}
	}

	public String getString(String key, String defaultValue) {
		Object value = getValue(key);
		if (value != null) {
			return value.toString();
		} else {
			return defaultValue;
		}
	}

	public List<Object> getList(String key) {
		Object value = getValue(key);
		if (value instanceof List) {
			return (List<Object>) value;
		} else {
			return null;
		}
	}

	public <T> List<T> getList(String key, Class<T> t) {
		Object value = getValue(key);
		if (value instanceof List) {

			List<Object> list = (List<Object>) value;

			List<T> result = new ArrayList<T>();
			for (int i = 0; i < list.size(); i++) {
				result.add((T) list.get(i));
			}
			return result;
		} else {
			return null;
		}
	}

	public List<OldSettings> getSettingList(String key) {
		Object value = getValue(key);
		if (value instanceof List) {

			List<Object> list = (List<Object>) value;

			List<OldSettings> result = new ArrayList<OldSettings>();
			for (int i = 0; i < list.size(); i++) {
				Object maybeMap = list.get(i);
				if (maybeMap instanceof Map) {
					result.add(new OldSettings((Map<String, Object>) maybeMap));
				} else {
					return null;
				}
			}
			return result;
		} else {
			return null;
		}
	}

	public synchronized Object getValue(String key) {
		String[] keys = key.split("\\.");
		Map<String, Object> workMap = this.map;
		for (int i = 0; i < keys.length; i++) {
			Object value = workMap.get(keys[i]);
			if (value == null) {
				return null;
			}
			if (value instanceof Map) {
				workMap = (Map<String, Object>) value;
			} else {
				return value;
			}
		}

		return null;
	}

	public synchronized String toString() {
		DumperOptions options = new DumperOptions();
		options.setWidth(50);
		options.setIndent(4);
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		Yaml yaml = new Yaml(options);

		return yaml.dump(map);
	}

	public synchronized String getString() {
		Yaml yaml = new Yaml();
		return yaml.dump(map);
	}

	public long getByteSize(String key, long defaultValue) {
		String str = getString(key);
		if (str == null)
			return defaultValue;

		str = str.trim();
		int len = str.length();
		try {
			if (len > 0) {
				char suffix = str.charAt(len - 1);
				if (suffix == 'g' || suffix == 'G') {
					return Long.parseLong(str.substring(0, len - 1).trim()) * G;
				} else if (suffix == 'm' || suffix == 'M') {
					return Long.parseLong(str.substring(0, len - 1).trim()) * M;
				} else if (suffix == 'k' || suffix == 'K') {
					return Long.parseLong(str.substring(0, len - 1).trim()) * K;
				} else if (suffix == 'b' || suffix == 'B') {
					return Long.parseLong(str.substring(0, len - 1).trim());
				} else {
					return Long.parseLong(str);
				}
			} else {
				return defaultValue;
			}
		} catch (NumberFormatException e) {

			return defaultValue;
		}
	}

	public synchronized void put(String key, Object newValue) {
		String[] keys = key.split("\\.");
		Map<String, Object> workMap = this.map;

		for (int i = 0; i < keys.length; i++) {
			boolean isLeaf = (i == keys.length - 1);
			String nodeKey = keys[i];
			if (isLeaf) {
				workMap.put(nodeKey, newValue);
				return;
			}

			Object value = workMap.get(nodeKey);
			if (value == null || !(value instanceof Map)) {
				// 키가 없다면 하위맵을 만들어준다.
				Map<String, Object> newWorkMap = new HashMap<String, Object>();
				workMap.put(nodeKey, newWorkMap);
				workMap = newWorkMap;
			} else {
				workMap = (Map<String, Object>) value;
			}
		}
	}

}
