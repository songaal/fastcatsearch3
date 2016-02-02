package org.fastcatsearch.job.state;

import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

import java.io.IOException;

public class IndexingTaskKey extends TaskKey {

	private String collectionId;

	public IndexingTaskKey(){
	}
	
	public IndexingTaskKey(String collectionId) {
		super("IndexingTaskKey>" + collectionId);
		this.collectionId = collectionId;
	}

	public String collectionId(){
		return collectionId;
	}
	
	@Override
	public void readFrom(DataInput input) throws IOException {
		super.readFrom(input);
		collectionId = input.readString();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		super.writeTo(output);
		output.writeString(collectionId);
	}
	
	@Override
	public String getSummary(){
		return "indexing "+collectionId;
	}
}
