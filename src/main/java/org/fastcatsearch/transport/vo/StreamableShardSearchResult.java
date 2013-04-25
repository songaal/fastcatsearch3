package org.fastcatsearch.transport.vo;

import java.io.IOException;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.group.GroupData;
import org.fastcatsearch.ir.query.ShardSearchResult;

public class StreamableShardSearchResult implements Streamable {
	private ShardSearchResult shardSearchResult;
	
	public StreamableShardSearchResult(ShardSearchResult shardSearchResult){
		this.shardSearchResult = shardSearchResult;
	}
	
	public ShardSearchResult shardSearchResult(){
		return shardSearchResult;
	}
	
	@Override
	public void readFrom(StreamInput input) throws IOException {
		int count = input.readInt();
		int totalCount = input.readInt();
		
		StreamableHitElement sHitElement = new StreamableHitElement();
		sHitElement.readFrom(input);
		
		StreamableGroupData sGroupData = new StreamableGroupData();
		sGroupData.readFrom(input);
		
		this.shardSearchResult = new ShardSearchResult(sHitElement.getHitElementList(), 
				count, totalCount, sGroupData.groupData());
		
		
	}

	@Override
	public void writeTo(StreamOutput output) throws IOException {
		output.writeInt(shardSearchResult.getCount());
		output.writeInt(shardSearchResult.getTotalCount());
		new StreamableHitElement(shardSearchResult.getHitElementList()).writeTo(output);
		new StreamableGroupData(shardSearchResult.getGroupData()).writeTo(output);
	}

}
