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

public class CustomDictionary extends SourceDictionary {
	private static Logger logger = LoggerFactory.getLogger(MapDictionary.class);

	private Map<Object, Object[]> map;
	
	public CustomDictionary() {
		this(false);
	}
	public CustomDictionary(boolean ignoreCase) {
		super(ignoreCase);
	}
	public CustomDictionary(File file, boolean ignoreCase) {
		super(ignoreCase);
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addEntry(String keyword, Object[] values) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void addSourceLineEntry(String line) {
		// TODO Auto-generated method stub
		
	}


}
