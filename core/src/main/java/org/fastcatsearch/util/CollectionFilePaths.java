package org.fastcatsearch.util;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CollectionFilePaths {
	private static final Logger logger = LoggerFactory.getLogger(CollectionFilePaths.class);
	
	private File root;
	private String collectionId;
	private String shardId;
	private static String shardDataRoot = "data";
	
	public CollectionFilePaths(File collectionRoot, String collectionId) {
		this(collectionRoot, collectionId, collectionId);
	}
	
	public CollectionFilePaths(File collectionRoot, String collectionId, String shardId) {
		this.root = new File(collectionRoot, collectionId);
		this.collectionId = collectionId;
		this.shardId = shardId;
	}

	public File file() {
		return root;
	}

	public String collectionId(){
		return collectionId;
	}
	
	public String shardId(){
		return shardId;
	}

	public File file(String... dirs) {
		File file = root;
		for (int i = 0; i < dirs.length; i++) {
			file = new File(file, dirs[i]);
		}
		return file;
	}
	
	private String dataPath() {
		return dataPath(0);
	}

	private String dataPath(Object dataSequence) {
		return "data" + dataSequence.toString();
	}
	
	private File dataFile() {
		return dataFile(0);
	}
	
	public File dataFile(Object dataSequence) {
		return file(shardDataRoot, shardId, dataPath(dataSequence));
	}

	public File segmentFile(Object dataSequence, Object segmentId) {
		return file(shardDataRoot, shardId, dataPath(dataSequence), segmentId.toString());
	}
	
	public File revisionFile(Object dataSequence, Object segmentId, Object revisionNumber) {
		return file(shardDataRoot, shardId, dataPath(dataSequence), segmentId.toString(), revisionNumber.toString());
	}
	
}
