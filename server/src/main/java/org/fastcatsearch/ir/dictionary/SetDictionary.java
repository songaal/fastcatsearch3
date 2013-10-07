package org.fastcatsearch.ir.dictionary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
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

public class SetDictionary extends SourceDictionary implements ReadableDictionary {
	
	private Set<CharVector> set;

	public SetDictionary(boolean ignoreCase) {
		super(ignoreCase);
		set = new HashSet<CharVector>();
	}

	public SetDictionary(Set<CharVector> set, boolean ignoreCase) {
		super(ignoreCase);
		this.set = set;
	}

	public SetDictionary(File file) {
		super(true);
		if(!file.exists()){
			set = new HashSet<CharVector>();
			logger.error("사전파일이 존재하지 않습니다. file={}", file.getAbsolutePath());
			return;
		}
		InputStream is;
		try {
			is = new FileInputStream(file);
			readFrom(is);
			is.close();
		} catch (IOException e) {
			logger.error("", e);
		}
	}

	public SetDictionary(InputStream is, boolean ignoreCase){
		super(ignoreCase);
		try {
			readFrom(is);
		} catch (IOException e) {
			logger.error("", e);
		}
	}
	
	public void addEntry(String keyword){
		addEntry(keyword);
	}
	@Override
	public void addEntry(String keyword, String[] ingnore) {
		keyword = keyword.trim();
		if (keyword.length() > 0) {
			if(ignoreCase){
				keyword = keyword.toUpperCase();
			}
			set.add(new CharVector(keyword));
		}
	}

	public Set<CharVector> getSet() {
		return Collections.unmodifiableSet(set);
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		
		DataOutput output = new OutputStreamDataOutput(out);
		Iterator<CharVector> valueIter = set.iterator();
		//write size of set
		
		output.writeInt(set.size());
		//write values
		for(;valueIter.hasNext();) {
			CharVector value = valueIter.next();
			output.writeString(value.toString());
		}
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		
		DataInput input = new InputStreamDataInput(in);
		set = new HashSet<CharVector>();
		int size = input.readInt();
		
		for(int entryInx=0;entryInx < size; entryInx++) {
			set.add(new CharVector(input.readString()));
		}
	}

	@Override
	public List<CharVector> find(CharVector token) {
		if(set.contains(token)) {
			Arrays.asList(new CharVector[] { token });
		}
		return null;
	}

	@Override
	public int size() {
		return set.size();
	}

	@Override
	public void addSourceLineEntry(String line) {
		addEntry(line, null);
	}

	@Override
	public void addMapEntry(Map<String, Object> vo) {
		addEntry((String) vo.get("keyword"), null);
	}
}
