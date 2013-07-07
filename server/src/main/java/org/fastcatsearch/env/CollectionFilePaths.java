package org.fastcatsearch.env;

import java.io.File;


public class CollectionFilePaths extends Path {
	
	public CollectionFilePaths(File collectionRoot, String collectionId) {
		super(collectionRoot, collectionId);
	}

	public Path home() {
		return this;
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
	
}
