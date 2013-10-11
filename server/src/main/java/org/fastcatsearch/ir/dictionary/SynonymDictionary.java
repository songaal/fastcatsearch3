package org.fastcatsearch.ir.dictionary;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.lucene.store.InputStreamDataInput;
import org.apache.lucene.store.OutputStreamDataOutput;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class SynonymDictionary extends MapDictionary {

	private Set<CharVector> wordSet;
	
	public SynonymDictionary() {
		wordSet = new HashSet<CharVector>();
	}

	public SynonymDictionary(File file) {
		super(file);
		wordSet = new HashSet<CharVector>();
	}
	
	public SynonymDictionary(InputStream is) {
		super(is);
	}
	
	public Set<CharVector> getWordSet() {
		return Collections.unmodifiableSet(wordSet);
	}
	
	//key가 null일수 있다. 양방향의 경우.
	@Override
	public void addEntry(String keyword, Object[] values, boolean ignoreCase, boolean[] valuesIgnoreCase) {
		
		ArrayList<CharVector> list = new ArrayList<CharVector>(4);
		
		CharVector mainWord = null;
		if(keyword != null){
			keyword = keyword.trim();
			if(keyword.length() > 0){
				if(ignoreCase){
					keyword = keyword.toUpperCase();
				}
				
				mainWord = new CharVector(keyword);
				wordSet.add(mainWord);
			}
		}
		
		if(values == null || values.length == 0){
			return;
		}
		
		String valueString = values[0].toString();
		if(valuesIgnoreCase[0]){
			valueString = valueString.toUpperCase();
		}
		String[] synonyms = valueString.split(",");
		
		for (int k = 0; k < synonyms.length; k++) {
			String synonym = synonyms[k].trim();
			if (synonym.length() > 0) {
				CharVector word = new CharVector(synonym);
				list.add(word);
				wordSet.add(word);
			}
		}

		if (mainWord == null) {
			// 양방향.
			for (int j = 0; j < list.size(); j++) {
				CharVector key = list.get(j);
				CharVector[] value = new CharVector[list.size() - 1];
				int idx = 0;
				for (int k = 0; k < list.size(); k++) {
					CharVector val = list.get(k);
					if (!key.equals(val)) {
						// 다른것만 value로 넣는다.
						value[idx++] = val;
					}
				}
				//유사어사전 데이터에 대표단어와 동일한 단어가 여러개 있을경우, 최종리스트는 더 적어지게 되므로 전체 array 길이를 줄여준다. 
				if(idx < value.length){
					value = Arrays.copyOf(value, idx);
				}
				map.put(key, value);
//				logger.debug("유사어 양방향 {} >> {} {}", key, value[0], value[1]);
			}
			
		} else {
			// 단방향.
			CharVector[] value = new CharVector[list.size()];
			int idx = 0;
			for (int j = 0; j < value.length; j++) {
				CharVector word = list.get(j);
				if (!mainWord.equals(word)) {
					// 다른것만 value로 넣는다.
					value[idx++] = word;
				}
			}
			if(idx < value.length){
				value = Arrays.copyOf(value, idx);
			}
			map.put(mainWord, value);
		}
	}
	@Override
	public void writeTo(OutputStream out) throws IOException {
		super.writeTo(out);
		DataOutput output = new OutputStreamDataOutput(out);
		//write size of synonyms 
		output.writeVInt(wordSet.size());
		
		//write synonyms
		Iterator<CharVector> synonymIter = wordSet.iterator();
		for(;synonymIter.hasNext();) {
			CharVector value = synonymIter.next();
			output.writeUString(value.array, value.start, value.length);
		}
	}
	
	@Override
	public void readFrom(InputStream in) throws IOException {
		super.readFrom(in);
		DataInput input = new InputStreamDataInput(in);
		wordSet = new HashSet<CharVector>();
		int size = input.readVInt();
		for(int entryInx=0;entryInx < size; entryInx++) {
			wordSet.add(new CharVector(input.readUString()));
		}
	}
}
