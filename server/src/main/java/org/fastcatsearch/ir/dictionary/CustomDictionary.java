package org.fastcatsearch.ir.dictionary;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.ir.io.CharVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomDictionary extends SourceDictionary implements ReadableDictionary {
	
	public CustomDictionary() {
	}
	public CustomDictionary(File file) {
		
	}
	private static Logger logger = LoggerFactory.getLogger(MapDictionary.class);

	private Map<Object, Object[]> map;
	

	@Override
	public void writeTo(OutputStream out) throws IOException {
		// TODO Auto-generated method stub

	}

//	@Override
//	public List find(CharVector token) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public int size() {
//		// TODO Auto-generated method stub
//		return 0;
//	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addEntry(String keyword, Object[] values, boolean ignoreCase, boolean[] valuesIgnoreCase) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addSourceLineEntry(String line, boolean ignoreCase, boolean[] valuesIgnoreCase) {
		// TODO Auto-generated method stub
		
	}


}
