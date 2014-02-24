package org.fastcatsearch.ir.dictionary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.store.InputStreamDataInput;
import org.apache.lucene.store.OutputStreamDataOutput;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomDictionary extends SourceDictionary {
	private static Logger logger = LoggerFactory.getLogger(MapDictionary.class);
	
	public static final byte TYPE_CHAR = 'c';
	public static final byte TYPE_INTEGER = 'i';
	
	private Map<Object, Object[]> map;
	
	public CustomDictionary() {
		this(false);
	}
	public CustomDictionary(boolean ignoreCase) {
		super(ignoreCase);
		map = new HashMap<Object, Object[]>();
	}
	public CustomDictionary(File file, boolean ignoreCase) {
		super(ignoreCase);
		if (!file.exists()) {
			map = new HashMap<Object, Object[]>();
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
	
	public CustomDictionary(InputStream is, boolean ignoreCase) {
		super(ignoreCase);
		try {
			readFrom(is);
		} catch (IOException e) {
			logger.error("",e);
		}
	}
	public Map<Object, Object[]> getUnmodifiableMap() {
		return Collections.unmodifiableMap(map);
	}

	public Map<Object, Object[]> map() {
		return map;
	}

	public void setMap(Map<Object, Object[]> map) {
		this.map = map;
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		DataOutput output = (DataOutput) new OutputStreamDataOutput(out);
		Iterator<Object> keySet = map.keySet().iterator();
		// write size of map
		output.writeVInt(map.size());
		// write key and value map
		for (; keySet.hasNext();) {
			// write key
			Object keyObject = keySet.next();
			
			if(keyObject instanceof CharVector) {
				CharVector key = (CharVector)keyObject;
				output.writeByte(TYPE_CHAR);
				output.writeUString(key.array(), key.start(), key.length());
			} else if(keyObject instanceof Integer) {
				Integer key = (Integer) keyObject;
				output.writeByte(TYPE_INTEGER);
				output.writeInt(key);
			}
			
			// write values
			Object[] values = map.get(keyObject);
			output.writeVInt(values.length);
			for (Object valueObject : values) {
				if(valueObject instanceof String) {
					CharVector value = new CharVector((String) valueObject);
					output.writeByte(TYPE_CHAR);
					output.writeUString(value.array(), value.start(), value.length());
				} else if(valueObject instanceof CharVector) {
					CharVector value = (CharVector) valueObject;
					output.writeByte(TYPE_CHAR);
					output.writeUString(value.array(), value.start(), value.length());
				} else if(valueObject instanceof Integer) {
					Integer value = (Integer) valueObject;
					output.writeByte(TYPE_INTEGER);
					output.writeInt(value);
				}
			}
		}
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		DataInput input = new InputStreamDataInput(in);
		
		map = new HashMap<Object, Object[]>();
		int size = input.readVInt();
		for (int entryInx = 0; entryInx < size; entryInx++) {
			byte type = input.readByte();
			
			Object key = null;
			if(type==TYPE_CHAR) {
				key = new CharVector(input.readUString());
			} else if(type==TYPE_INTEGER) {
				key = input.readInt();
			}

			int valueLength = input.readVInt();

			Object[] values = new Object[valueLength];

			for (int valueInx = 0; valueInx < valueLength; valueInx++) {
				type = input.readByte();
				if(type==TYPE_CHAR) {
					values[valueInx] = new CharVector(input.readUString());
				} else if(type==TYPE_INTEGER) {
					values[valueInx] = input.readInt();
				}
			}
			map.put(key, values);
		}
	}

	@Override
	public void addEntry(String keyword, Object[] values) {
		if (keyword == null || keyword.length() == 0) {
			return;
		}
		CharVector cv = new CharVector(keyword);
		map.put(cv, values);
	}
	
	public void addEntry(Object keyword, Object[] values) {
		if ( keyword == null) {
			return;
		}
		map.put(keyword, values);
	}
	
	@Override
	public void addSourceLineEntry(String line) {
		String[] kv = line.split("\t");
		if (kv.length == 1) {
			String value = kv[0].trim();
			addEntry(null, new Object[] { value });
		} else if (kv.length == 2) {
			String keyword = kv[0].trim();
			String value = kv[1].trim();
			addEntry(keyword, new Object[] { value });
		}
		
	}

	@Override
	public void reload(Object object) throws IllegalArgumentException {
		if(object != null && object instanceof CustomDictionary){
			CustomDictionary customDictionary = (CustomDictionary) object;
			this.map = customDictionary.map();
			
		}else{
			throw new IllegalArgumentException("Reload dictionary argument error. argument = " + object);
		}
	}
}
