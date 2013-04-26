package org.fastcatsearch.transport.vo;

import java.io.IOException;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.search.HitElement;

public class StreamableHitElement implements Streamable {

	private HitElement[] hitElements;

	public StreamableHitElement() {
	}

	public StreamableHitElement(HitElement[] hitElements) {
		this.hitElements = hitElements;
	}

	@Override
	public void readFrom(StreamInput input) throws IOException {
		int hitElementSize = input.readInt();
		this.hitElements = new HitElement[hitElementSize];
		for (int hitElementInx = 0; hitElementInx < hitElements.length; hitElementInx++) {
			int docNo = input.readInt();
			int score = input.readInt();
			String collection = input.readString();
			int shardId = input.readInt();
			HitElement hitElement = new HitElement(docNo, score);
			int rankDataSize = input.readInt();
			byte[] rankData = new byte[rankDataSize];
			for (int rankDataInx = 0; rankDataInx < rankData.length; rankDataInx++) {
				rankData[rankDataInx] = input.readByte();
			}
			hitElement.collection(collection);
			hitElement.shardId(shardId);
			hitElement.dataOffset(input.readInt());
			hitElement.dataLen(input.readInt());
		}
	}

	@Override
	public void writeTo(StreamOutput output) throws IOException {

		output.writeInt(hitElements.length);
		for (int hitElementInx = 0; hitElementInx < hitElements.length; hitElementInx++) {
			HitElement hitElement = hitElements[hitElementInx];
			byte[] rankData = hitElement.rankdata();
			output.writeString(hitElement.collection());
			output.writeInt(hitElement.shardid());
			output.writeInt(hitElement.docNo());
			output.writeInt(hitElement.score());
			output.writeInt(rankData.length);
			for (int rankDataInx = 0; rankDataInx < rankData.length; rankDataInx++) {
				output.writeByte(rankData[rankDataInx]);
			}
			output.writeInt(hitElement.dataOffset());
			output.writeInt(hitElement.dataLen());
		}
	}

	public HitElement[] getHitElementList() {
		return hitElements;
	}
}