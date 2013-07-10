package org.fastcatsearch.env;

import java.io.File;


public class CollectionFilePaths extends Path {
	private String collectionId;
	
	public CollectionFilePaths(File collectionRoot, String collectionId) {
		super(collectionRoot, collectionId);
		this.collectionId = collectionId;
	}

	public Path home() {
		return this;
	}

	public String collectionId(){
		return collectionId;
	}
	
	public Path dataPath() {
		return dataPath(0);
	}

	public Path dataPath(Object dataSequence) {
		return path("data" + dataSequence.toString());
	}

	public Path segmentPath(Object dataSequence, Object segmentNumber) {
		return dataPath(dataSequence).path(segmentNumber.toString());
	}
	
	public Path revisionPath(Object dataSequence, Object segmentNumber, Object revisionNumber) {
		return dataPath(dataSequence).path(segmentNumber.toString(), revisionNumber.toString());
	}
	
}
