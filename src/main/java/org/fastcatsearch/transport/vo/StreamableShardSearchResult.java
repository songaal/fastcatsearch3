package org.fastcatsearch.transport.vo;

import java.io.IOException;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.common.io.Streamable;
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
		// TODO Auto-generated method stub

		//GroupData와 HitElement의 직렬화는 각각 StreamableGroupData과 StreamableHitElement을 이용한다.
		
	}

	@Override
	public void writeTo(StreamOutput output) throws IOException {
		// TODO Auto-generated method stub

	}

}
