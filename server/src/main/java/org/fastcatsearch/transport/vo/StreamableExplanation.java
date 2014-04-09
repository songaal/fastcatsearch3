package org.fastcatsearch.transport.vo;

import java.io.IOException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.search.ClauseExplanation;
import org.fastcatsearch.ir.search.Explanation;

public class StreamableExplanation implements Streamable {
	
	private Explanation explanation;
	
	public StreamableExplanation(Explanation explanation){
		this.explanation = explanation;
	}
	
	public Explanation explanation() {
		return explanation;
	}
	
	@Override
	public void readFrom(DataInput input) throws IOException {
		explanation = new Explanation();
		int segmentId = input.readVInt();
		explanation.setSegmentId(segmentId);
		if(input.readBoolean()){
			ClauseExplanation clauseExplanation =  new ClauseExplanation();
			
		}
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {

	}

}
