package org.fastcatsearch.job.result;

import java.io.IOException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class BasicStringResult implements Streamable {
	
	private String result;
	
	public BasicStringResult() { }
	
	public String getResult() {
		return result;
	}
	
	public void setResult(String result) {
		this.result = result;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		result = input.readString();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(result);
	}
}
