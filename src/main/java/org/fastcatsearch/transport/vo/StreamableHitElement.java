package org.fastcatsearch.transport.vo;

import java.io.IOException;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.search.HitElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamableHitElement implements Streamable {

	public static Logger logger = LoggerFactory.getLogger(StreamableHitElement.class);
	
	private HitElement[] hitElements;
	private int count;

	public StreamableHitElement() {
	}

	public StreamableHitElement(HitElement[] hitElements, int count) {
		this.hitElements = hitElements;
		this.count = count;
	}

	@Override
	public void readFrom(StreamInput input) throws IOException {
		count = input.readInt();
		this.hitElements = new HitElement[count];
		for (int hitElementInx = 0; hitElementInx < count; hitElementInx++) {
			int docNo = input.readInt();
			float score = input.readFloat();
			int dataOffset = input.readInt();
			int dataLength = input.readInt();
//			logger.debug("read dataLength = {},{}", dataOffset, dataLength);
			byte[] rankData = null;
			if(dataLength > 0){
				rankData = new byte[dataLength];
				for (int rankDataInx = dataOffset; rankDataInx < dataOffset + dataLength; rankDataInx++) {
					rankData[rankDataInx] = input.readByte();
				}
			}
			hitElements[hitElementInx] = new HitElement(docNo, score, rankData, dataOffset, dataLength);
		}
	}

	@Override
	public void writeTo(StreamOutput output) throws IOException {
		
		output.writeInt(count);
		for (int hitElementInx = 0; hitElementInx < count; hitElementInx++) {
			HitElement hitElement = hitElements[hitElementInx];
			byte[] rankData = hitElement.rankdata();
			output.writeInt(hitElement.docNo());
			output.writeFloat(hitElement.score());
			//새로운 배열에는 0부터 기록했기때문에 offset을 0으로 해준다.
			output.writeInt(0);
			output.writeInt(hitElement.dataLen());
//			logger.debug("write dataLength = 0,{}", hitElement.dataLen());
			if(hitElement.dataLen() > 0){
				final int offset = hitElement.dataOffset();
				for (int i = 0; i < hitElement.dataLen(); i++) {
					output.writeByte(rankData[offset + i]);
				}
			}
		}
	}

	public HitElement[] getHitElementList() {
		return hitElements;
	}

	public int count() {
		return count;
	}
}