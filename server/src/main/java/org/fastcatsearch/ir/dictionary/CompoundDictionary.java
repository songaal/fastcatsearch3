package org.fastcatsearch.ir.dictionary;

import org.apache.lucene.store.InputStreamDataInput;
import org.apache.lucene.store.OutputStreamDataOutput;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.util.CharVectorHashSet;
import org.fastcatsearch.ir.util.CharVectorUtils;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.ColumnSetting;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

public class CompoundDictionary extends MapDictionary {

	private Set<CharVector> mainWordSet;
	private Set<CharVector> wordSet;

	public CompoundDictionary(){
		this(false);
	}
	public CompoundDictionary(boolean isIgnoreCase) {
		super(isIgnoreCase);
		if(mainWordSet == null) {
			mainWordSet = new CharVectorHashSet(isIgnoreCase);
		}
		if(wordSet == null) {
			wordSet = new CharVectorHashSet(isIgnoreCase);
		}
	}

	public CompoundDictionary(File file, boolean isIgnoreCase) {
		super(file, isIgnoreCase);
		if(mainWordSet == null) {
			mainWordSet = new CharVectorHashSet(isIgnoreCase);
		}
		if(wordSet == null) {
			wordSet = new CharVectorHashSet(isIgnoreCase);
		}
	}

	public CompoundDictionary(InputStream is, boolean isIgnoreCase) {
		super(is, isIgnoreCase);
		if(mainWordSet == null) {
			mainWordSet = new CharVectorHashSet(isIgnoreCase);
		}
		if(wordSet == null) {
			wordSet = new CharVectorHashSet(isIgnoreCase);
		}
	}

	public Set<CharVector> getWordSet() {
		return wordSet;
	}
	
	public void setWordSet(Set<CharVector> wordSet) {
		this.wordSet = wordSet;
	}

	public Set<CharVector> getMainWordSet() {
		return mainWordSet;
	}

	public void setMainWordSet(Set<CharVector> mainWordSet) {
		this.mainWordSet = mainWordSet;
	}

	public Set<CharVector> getUnmodifiableWordSet() {
		return Collections.unmodifiableSet(wordSet);
	}
	public Set<CharVector> getUnmodifiableMainWordSet() {
		return Collections.unmodifiableSet(mainWordSet);
	}

	@Override
	public void addEntry(String keyword, Object[] values, List<ColumnSetting> columnSettingList) {

		ArrayList<CharVector> list = new ArrayList<CharVector>(4);

		CharVector mainWord = null;
		if (keyword == null) {
			logger.error("Compound main keyword is null.");
			return;
		}
		if (values == null) {
			logger.error("Compound dictionary value is null.");
			return;
		}
		if (values.length == 0) {
			logger.error("Compound dictionary value is empty.");
			return;
		}
		keyword = keyword.trim();
		if (keyword.length() == 0) {
			logger.error("Compound main keyword is empty.");
			return;
		}
		mainWord = new CharVector(keyword);
		mainWordSet.add(mainWord);

		// 0번째에 복합명사들이 컴마 단위로 모두 입력되어 있으므로 [0]만 확인하면 된다.
		String valueString = values[0].toString();
		String[] nouns = valueString.split(",");
		for (int k = 0; k < nouns.length; k++) {
			String noun = nouns[k].trim();
			if (noun.length() > 0) {
				CharVector word = new CharVector(noun);
				list.add(word);
				wordSet.add(word);
			}
		}

		CharVector[] value = new CharVector[list.size()];
		for (int j = 0; j < value.length; j++) {
			CharVector word = list.get(j);
			value[j] = word;
		}
		if (value.length > 0) {
			map.put(mainWord, value);
		}
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		super.writeTo(out);
		DataOutput output = new OutputStreamDataOutput(out);
		// write size of synonyms
		output.writeVInt(mainWordSet.size());
		// write synonyms
		Iterator<CharVector> mainWordIter = mainWordSet.iterator();
		while (mainWordIter.hasNext()) {
			CharVector value = mainWordIter.next();
			output.writeUString(value.array(), value.start(), value.length());
		}
		// write size of synonyms
		output.writeVInt(wordSet.size());
		// write synonyms
		Iterator<CharVector> wordIter = wordSet.iterator();
		while (wordIter.hasNext()) {
			CharVector value = wordIter.next();
			output.writeUString(value.array(), value.start(), value.length());
		}
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		super.readFrom(in);
		DataInput input = new InputStreamDataInput(in);
		mainWordSet = new CharVectorHashSet(ignoreCase);
		int mainWordSize = input.readVInt();
		for (int entryInx = 0; entryInx < mainWordSize; entryInx++) {
			mainWordSet.add(new CharVector(input.readUString()));
		}
		wordSet = new CharVectorHashSet(ignoreCase);
		int size = input.readVInt();
		for (int entryInx = 0; entryInx < size; entryInx++) {
			wordSet.add(new CharVector(input.readUString()));
		}
	}
	
	@Override
	public void reload(Object object) throws IllegalArgumentException {
		if(object != null && object instanceof CompoundDictionary){
			super.reload(object);
			CompoundDictionary compoundDictionary = (CompoundDictionary) object;
			this.mainWordSet = compoundDictionary.getMainWordSet();
			this.wordSet = compoundDictionary.getWordSet();
			
		}else{
			throw new IllegalArgumentException("Reload dictionary argument error. argument = " + object);
		}
	}
}
