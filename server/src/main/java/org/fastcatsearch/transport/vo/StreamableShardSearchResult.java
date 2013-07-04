package org.fastcatsearch.transport.vo;

import java.io.IOException;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.query.ShardSearchResult;

public class StreamableShardSearchResult implements Streamable {
	private ShardSearchResult shardSearchResult;

	public StreamableShardSearchResult(){ } 

	public StreamableShardSearchResult(ShardSearchResult shardSearchResult) {
		this.shardSearchResult = shardSearchResult;
	}

	public ShardSearchResult shardSearchResult() {
		return shardSearchResult;
	}
	
	@Override
	public void readFrom(StreamInput input) throws IOException {
		String collectionId = null;
		if (input.readBoolean()) {
			collectionId = input.readString().intern();
		}
		int shardId = input.readInt();
		int totalCount = input.readInt();
		StreamableHitElement sHitElement = new StreamableHitElement();
		sHitElement.readFrom(input);
		int count = sHitElement.count();
		
		StreamableGroupData sGroupData = new StreamableGroupData();
		if(input.readBoolean()){
			sGroupData.readFrom(input);
		}
		
		this.shardSearchResult = new ShardSearchResult(collectionId, shardId, sHitElement.getHitElementList(), count, totalCount,
				sGroupData.groupData());

	}

	@Override
	public void writeTo(StreamOutput output) throws IOException {
		String collectionId = shardSearchResult.collectionId();
		if (collectionId == null) {
			output.writeBoolean(false);
		} else {
			output.writeBoolean(true);
			output.writeString(collectionId);
		}
		output.writeInt(shardSearchResult.shardId());
		output.writeInt(shardSearchResult.getTotalCount());
		new StreamableHitElement(shardSearchResult.getHitElementList(), shardSearchResult.getCount()).writeTo(output);
		if(shardSearchResult.getGroupData() != null){
			output.writeBoolean(true);
			new StreamableGroupData(shardSearchResult.getGroupData()).writeTo(output);
		}else{
			output.writeBoolean(false);
		}
	}

}
