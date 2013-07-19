package org.fastcatsearch.util;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CollectionFilePaths {
	private static final Logger logger = LoggerFactory.getLogger(CollectionFilePaths.class);
	
	private File root;
	private String collectionId;
	
	public CollectionFilePaths(File collectionRoot, String collectionId) {
		this.root = new File(collectionRoot, collectionId);
		this.collectionId = collectionId;
	}

	public File file() {
		return root;
	}

	public String collectionId(){
		return collectionId;
	}

	public File file(String... dirs) {
		File file = root;
		for (int i = 0; i < dirs.length; i++) {
			file = new File(file, dirs[i]);
		}
		return file;
	}
	
	public String dataPath() {
		return dataPath(0);
	}

	public String dataPath(Object dataSequence) {
		return "data" + dataSequence.toString();
	}
	
	public File dataFile() {
		return dataFile(0);
	}
	
	public File dataFile(Object dataSequence) {
		return file(dataPath(dataSequence));
	}

	public File segmentFile(Object dataSequence, Object segmentNumber) {
		return file(dataPath(dataSequence), segmentNumber.toString());
	}
	
	public File revisionFile(Object dataSequence, Object segmentNumber, Object revisionNumber) {
		return file(dataPath(dataSequence), segmentNumber.toString(), revisionNumber.toString());
	}
	
}
