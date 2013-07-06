package org.fastcatsearch.transport.vo;

import java.io.IOException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class StreamableString implements Streamable {
	private String str;

	public StreamableString() {
	}

	public StreamableString(String str) {
		this.str = str;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		str = input.readString();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(str);
	}

	public String value() {
		return str;
	}

	@Override
	public String toString() {
		return str;
	}
}
