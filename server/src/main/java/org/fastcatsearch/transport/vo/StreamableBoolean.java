package org.fastcatsearch.transport.vo;

import java.io.IOException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class StreamableBoolean implements Streamable {

	private boolean bool;

	public StreamableBoolean() {
	}

	public StreamableBoolean(boolean bool) {
		this.bool = bool;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		bool = input.readBoolean();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeBoolean(bool);
	}

	@Override
	public String toString() {
		return bool + "";
	}
}
