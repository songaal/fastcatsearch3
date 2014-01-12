package org.fastcatsearch.job.state;

import java.io.IOException;

import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class IndexingTaskKey extends TaskKey {

	private String collectionId;
	private String indexingType;
		
	public IndexingTaskKey(){
	}
	
	public IndexingTaskKey(String collectionId, IndexingType type) {
		super("IndexingTaskKey>" + collectionId + ">" + type.toString());
		this.collectionId = collectionId;
		this.indexingType = type.toString();
	}

	@Override
	public TaskState createState() {
		return new IndexingTaskState();
	}

	public String collectionId(){
		return collectionId;
	}
	
	public String indexingType(){
		return indexingType;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		super.readFrom(input);
		collectionId = input.readString();
		indexingType = input.readString();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		super.writeTo(output);
		output.writeString(collectionId);
		output.writeString(indexingType);
	}
	
	@Override
	public String getSummary(){
		return indexingType + " indexing "+collectionId;
	}
}
