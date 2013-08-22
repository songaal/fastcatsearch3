package org.fastcatsearch.util;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IndexFilePaths {
	private static final Logger logger = LoggerFactory.getLogger(IndexFilePaths.class);
	
	private File root;
	private String id;
	private static String dataRoot = "data";
	private static String indexPrefix = "index";
	
	public IndexFilePaths(File root, String id) {
		this.root = new File(root, id);
		this.id = id;
	}

	public File file() {
		return root;
	}

	public String getId(){
		return id;
	}
	

	public File file(String... dirs) {
		File file = root;
		for (int i = 0; i < dirs.length; i++) {
			file = new File(file, dirs[i]);
		}
		return file;
	}
	
//	private String dataPath() {
//		return dataPath(0);
//	}

	private String dataPath(Object dataSequence) {
		return indexPrefix + dataSequence.toString();
	}
	
	public File dataFile() {
		return file(dataRoot);
	}
	
	public File indexDirFile(Object dataSequence) {
		return file(dataRoot, id, dataPath(dataSequence));
	}

	public File segmentFile(Object dataSequence, Object segmentId) {
		return file(dataRoot, id, dataPath(dataSequence), segmentId.toString());
	}
	
	public File revisionFile(Object dataSequence, Object segmentId, Object revisionNumber) {
		return file(dataRoot, id, dataPath(dataSequence), segmentId.toString(), revisionNumber.toString());
	}
	
}
