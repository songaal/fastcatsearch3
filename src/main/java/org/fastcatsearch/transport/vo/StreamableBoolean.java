package org.fastcatsearch.transport.vo;

import java.io.IOException;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.common.io.Streamable;

public class StreamableBoolean implements Streamable {

	private boolean bool;
	public StreamableBoolean(){ }
	
	public StreamableBoolean(boolean bool){
		this.bool = bool;
	}
	@Override
	public void readFrom(StreamInput input) throws IOException {
		bool = input.readBoolean();
	}

	@Override
	public void writeTo(StreamOutput output) throws IOException {
		output.writeBoolean(bool);
	}

	@Override
	public String toString(){
		return bool+"";
	}
}
