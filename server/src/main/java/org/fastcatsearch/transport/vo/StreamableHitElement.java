package org.fastcatsearch.transport.vo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.query.RowExplanation;
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

	/*
	 * collectionId와 shardId는 broker에서만 필요하므로 전송하지 않는다.
	 * 브로커에서 직접만들어 사용한다.
	 * */
	@Override
	public void readFrom(DataInput input) throws IOException {
		count = input.readInt();
		this.hitElements = new HitElement[count];
		for (int hitElementInx = 0; hitElementInx < count; hitElementInx++) {
			int segmentSequence = input.readInt();
			int docNo = input.readInt();
			int score = input.readInt();
			int rankDataSize = input.readInt();
//			logger.debug("read dataLength = {},{}", dataOffset, dataLength);
			BytesRef[] rankData = new BytesRef[rankDataSize];
			for (int rankDataInx = 0; rankDataInx < rankDataSize; rankDataInx++) {
				rankData[rankDataInx] = new BytesRef(input.readVInt());
				input.readBytes(rankData[rankDataInx]);
			}
			int explanationSize = input.readVInt();
			List<RowExplanation> explanations = null;
			if (explanationSize > 0) {
				explanations = new ArrayList<RowExplanation>();
				for (int i = 0; i < explanationSize; i++) {
					explanations.add(new RowExplanation(input.readString(), input.readVInt(), input.readString()));
				}
			}
			
			hitElements[hitElementInx] = new HitElement(segmentSequence, docNo, score, rankData, explanations);
		}
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		
		output.writeInt(count);
		for (int hitElementInx = 0; hitElementInx < count; hitElementInx++) {
			HitElement hitElement = hitElements[hitElementInx];
			BytesRef[] rankData = hitElement.rankData();
			output.writeInt(hitElement.segmentSequence());
			output.writeInt(hitElement.docNo());
			output.writeInt(hitElement.score());
			output.writeInt(hitElement.rankDataSize());
			for (int i = 0; i < hitElement.rankDataSize(); i++) {
				output.writeVInt(rankData[i].length());
				output.writeBytes(rankData[i]);
			}
			if(hitElement.rowExplanations() != null){
				output.writeVInt(hitElement.rowExplanations().size());
				for(RowExplanation exp : hitElement.rowExplanations()){
					output.writeString(exp.getId());
					output.writeVInt(exp.getScore());
					output.writeString(exp.getDescription());
				}
				
			}else{
				output.writeVInt(0);
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