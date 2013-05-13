package org.fastcatsearch.ir.dictionary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.fastcatsearch.ir.io.CharVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashSetDictionary extends SourceDictionary implements ReadableDictionary {
	private static Logger logger = LoggerFactory.getLogger(HashSetDictionary.class);
	
	private Set<CharVector> set;

	public HashSetDictionary() {
		set = new HashSet<CharVector>();
	}

	public HashSetDictionary(Set<CharVector> set) {
		this.set = set;
	}

	public HashSetDictionary(File file) {
		InputStream is;
		try {
			is = new FileInputStream(file);
			readFrom(is);
			is.close();
		} catch (IOException e) {
			logger.error("", e);
		}
	}

	public HashSetDictionary(InputStream is){
		try {
			readFrom(is);
		} catch (IOException e) {
			logger.error("", e);
		}
	}
	
	@Override
	public void addEntry(String line) {
		line = line.trim();

		if (line.length() > 0) {
			set.add(new CharVector(line));
		}
	}

	public Set<CharVector> getSet() {
		return Collections.unmodifiableSet(set);
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(out);
		oos.writeObject(set);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		ObjectInputStream ois = new ObjectInputStream(in);
		try {
			set = (Set<CharVector>) ois.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}	
	}

}
