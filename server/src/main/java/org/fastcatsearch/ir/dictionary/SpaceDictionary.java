package org.fastcatsearch.ir.dictionary;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.lucene.store.InputStreamDataInput;
import org.apache.lucene.store.OutputStreamDataOutput;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.util.CharVectorHashSet;

public class SpaceDictionary extends MapDictionary {

	private Set<CharVector> wordSet;

	public SpaceDictionary() {
		this(false);
	}
	public SpaceDictionary(boolean ignoreCase) {
		super(ignoreCase);
		wordSet = new CharVectorHashSet(ignoreCase);
	}

	public SpaceDictionary(File file, boolean ignoreCase) {
		super(file, ignoreCase);
	}

	public SpaceDictionary(InputStream is, boolean ignoreCase) {
		super(is, ignoreCase);
	}

	public Set<CharVector> getWordSet() {
		return wordSet;
	}
	
	public void setWordSet(Set<CharVector> wordSet) {
		this.wordSet = wordSet;
	}
	
	public Set<CharVector> getUnmodifiableWordSet() {
		return Collections.unmodifiableSet(wordSet);
	}

	@Override
	public void addEntry(String keyword, Object[] values) {
		CharVector[] value = makeValue(keyword);
		for(CharVector word : value){
			wordSet.add(word);
		}
		CharVector key = makeKey(value);
		map.put(key, value);
		//key는 붙여쓰기 오류일 경우도 있으므로, wordSet에 추가하지 않는다.
		
	}
	
	private CharVector[] makeValue(String word) {
		String[] list = word.split(",");
		CharVector[] value = new CharVector[list.length];
		for(int i=0;i < list.length;i++){
			value[i] = new CharVector(list[i].trim());
		}
		return value;
	}
	
	private CharVector makeKey(CharVector[] value) {
		String key = "";
		for(CharVector cv : value){
			key += cv.toString();
		}
		return new CharVector(key);
	}
	

	@Override
	public void writeTo(OutputStream out) throws IOException {
		super.writeTo(out);
		DataOutput output = new OutputStreamDataOutput(out);
		// write size of synonyms
		output.writeVInt(wordSet.size());

		// write synonyms
		Iterator<CharVector> synonymIter = wordSet.iterator();
		for (; synonymIter.hasNext();) {
			CharVector value = synonymIter.next();
			output.writeUString(value.array, value.start, value.length);
		}
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		super.readFrom(in);
		DataInput input = new InputStreamDataInput(in);
		wordSet = new CharVectorHashSet(ignoreCase);
		int size = input.readVInt();
		for (int entryInx = 0; entryInx < size; entryInx++) {
			wordSet.add(new CharVector(input.readUString()));
		}
	}
	
	@Override
	public void reload(Object object) throws IllegalArgumentException {
		if(object != null && object instanceof SpaceDictionary){
			super.reload(object);
			SpaceDictionary spaceDictionary = (SpaceDictionary) object;
			this.wordSet = spaceDictionary.getWordSet();
			
		}else{
			throw new IllegalArgumentException("Reload dictionary argument error. argument = " + object);
		}
	}
}
