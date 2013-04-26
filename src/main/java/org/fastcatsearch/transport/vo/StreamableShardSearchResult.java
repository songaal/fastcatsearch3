package org.fastcatsearch.transport.vo;

import java.io.IOException;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.query.ShardSearchResult;

public class StreamableShardSearchResult implements Streamable {
	private ShardSearchResult shardSearchResult;

	public ShardSearchResult shardSearchResult() {
		return shardSearchResult;
	}

	public StreamableShardSearchResult(ShardSearchResult shardSearchResult) {
		this.shardSearchResult = shardSearchResult;
	}

	@Override
	public void readFrom(StreamInput input) throws IOException {
		String collectionId = null;
		if (input.readBoolean()) {
			collectionId = input.readString().intern();
		}
		int shardId = input.readInt();
		int count = input.readInt();
		int totalCount = input.readInt();

		StreamableHitElement sHitElement = new StreamableHitElement();
		sHitElement.readFrom(input);

		StreamableGroupData sGroupData = new StreamableGroupData();
		sGroupData.readFrom(input);

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
		output.write(shardSearchResult.shardId());
		output.writeInt(shardSearchResult.getCount());
		output.writeInt(shardSearchResult.getTotalCount());
		new StreamableHitElement(shardSearchResult.getHitElementList()).writeTo(output);
		new StreamableGroupData(shardSearchResult.getGroupData()).writeTo(output);
	}

}
