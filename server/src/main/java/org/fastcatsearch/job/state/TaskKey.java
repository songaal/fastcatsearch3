package org.fastcatsearch.job.state;

import java.io.IOException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public abstract class TaskKey implements Streamable {
	protected String key;

	public TaskKey() {
	}

	public TaskKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

//	public TaskState createState() {
//		return createState(false);
//	}
//
//	public abstract TaskState createState(boolean isScheduled);

	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		return key.equals(((TaskKey) other).key);
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		key = input.readString();

	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(key);
	}

	public abstract String getSummary();

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + key + "]";
    }
}
