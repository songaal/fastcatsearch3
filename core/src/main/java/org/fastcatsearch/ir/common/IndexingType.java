package org.fastcatsearch.ir.common;

public enum IndexingType {
	FULL_INDEXING("FULL"), ADD_INDEXING("ADD");
	
	private String name;

	private IndexingType(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;

	}

}
