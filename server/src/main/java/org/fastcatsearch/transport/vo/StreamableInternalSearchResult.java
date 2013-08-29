package org.fastcatsearch.transport.vo;

import java.io.IOException;
import java.util.Map;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.query.HighlightInfo;
import org.fastcatsearch.ir.query.InternalSearchResult;

public class StreamableInternalSearchResult implements Streamable {
	private InternalSearchResult internalSearchResult;

	public StreamableInternalSearchResult(){ } 

	public StreamableInternalSearchResult(InternalSearchResult internalSearchResult) {
		this.internalSearchResult = internalSearchResult;
	}

	public InternalSearchResult getInternalSearchResult() {
		return internalSearchResult;
	}
	
	@Override
	public void readFrom(DataInput input) throws IOException {
		String collectionId = null;
		if (input.readBoolean()) {
			collectionId = input.readString();
		}
		String shardId = null;
		if (input.readBoolean()) {
			shardId = input.readString();
		}
		
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
		this.internalSearchResult = new InternalSearchResult(collectionId, shardId, sHitElement.getHitElementList(), count, totalCount,
				sGroupData.groupData(), highlightInfo);

	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		String collectionId = internalSearchResult.collectionId();
		if (collectionId == null) {
			output.writeBoolean(false);
		} else {
			output.writeBoolean(true);
			output.writeString(collectionId);
		}
		String shardId = internalSearchResult.shardId();
		if (shardId == null) {
			output.writeBoolean(false);
		} else {
			output.writeBoolean(true);
			output.writeString(shardId);
		}
		output.writeInt(internalSearchResult.getTotalCount());
		new StreamableHitElement(internalSearchResult.getHitElementList(), internalSearchResult.getCount()).writeTo(output);
		if(internalSearchResult.getGroupsData() != null){
			output.writeBoolean(true);
			new StreamableGroupsData(internalSearchResult.getGroupsData()).writeTo(output);
		}else{
			output.writeBoolean(false);
		}
		if(internalSearchResult.getHighlightInfo() != null){
			output.writeBoolean(true);
			output.writeGenericValue(internalSearchResult.getHighlightInfo().fieldAnalyzerMap());
			output.writeGenericValue(internalSearchResult.getHighlightInfo().fieldQueryMap());
		}else{
			output.writeBoolean(false);
		}
	}

}
