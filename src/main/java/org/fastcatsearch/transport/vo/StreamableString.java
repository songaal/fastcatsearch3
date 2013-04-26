package org.fastcatsearch.transport.vo;

import java.io.IOException;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.common.io.Streamable;

public class StreamableString implements Streamable {
	private String str;

	public StreamableString() {
	}

	public StreamableString(String str) {
		this.str = str;
	}

	@Override
	public void readFrom(StreamInput input) throws IOException {
		str = input.readString();
	}

	@Override
	public void writeTo(StreamOutput output) throws IOException {
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
