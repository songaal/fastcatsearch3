package org.fastcatsearch.transport.vo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class StreamableThrowable implements Streamable {
	private Throwable e;

	public StreamableThrowable() {
	}

	public StreamableThrowable(Throwable e) {
		this.e = e;
	}

	public Throwable getThrowable() {
		return e;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		try {
			e = (Throwable) new ObjectInputStream(input).readObject();
		} catch (ClassNotFoundException ignore) {

		}
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		new ObjectOutputStream(output).writeObject(e);
	}

}
