package org.fastcatsearch.transport.vo;

import java.io.IOException;
import java.util.Map;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.query.HighlightInfo;
import org.fastcatsearch.ir.query.InternalSearchResult;

public class StreamableShardSearchResult implements Streamable {
	private InternalSearchResult shardSearchResult;

	public StreamableShardSearchResult(){ } 

	public StreamableShardSearchResult(InternalSearchResult shardSearchResult) {
		this.shardSearchResult = shardSearchResult;
	}

	public InternalSearchResult getInternalSearchResult() {
		return shardSearchResult;
	}
	
	@Override
	public void readFrom(DataInput input) throws IOException {
		String collectionId = null;
		if (input.readBoolean()) {
			collectionId = input.readString().intern();
		}
		int shardId = input.readInt();
		int totalCount = input.readInt();
		StreamableHitElement sHitElement = new StreamableHitElement();
		sHitElement.readFrom(input);
		int count = sHitElement.count();
		
		StreamableGroupsData sGroupData = new StreamableGroupsData();
		if(input.readBoolean()){
			sGroupData.readFrom(input);
		}
		HighlightInfo highlightInfo = null;
		if(input.readBoolean()){
			highlightInfo = new HighlightInfo((Map<String, String>) input.readGenericValue(), (Map<String, String>) input.readGenericValue());
		}
		this.shardSearchResult = new InternalSearchResult(collectionId, shardId, sHitElement.getHitElementList(), count, totalCount,
				sGroupData.groupData(), highlightInfo);

	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
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
		if(shardSearchResult.getGroupsData() != null){
			output.writeBoolean(true);
			new StreamableGroupsData(shardSearchResult.getGroupsData()).writeTo(output);
		}else{
			output.writeBoolean(false);
		}
		if(shardSearchResult.getHighlightInfo() != null){
			output.writeBoolean(true);
			output.writeGenericValue(shardSearchResult.getHighlightInfo().fieldAnalyzerMap());
			output.writeGenericValue(shardSearchResult.getHighlightInfo().fieldQueryMap());
		}else{
			output.writeBoolean(false);
		}
	}

}
