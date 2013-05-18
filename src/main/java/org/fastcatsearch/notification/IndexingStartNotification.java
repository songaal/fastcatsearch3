package org.fastcatsearch.notification;

import java.io.IOException;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.notification.message.Notification;

public class IndexingStartNotification extends Notification {
	
	private String collection;
	private String indexingType;
	private long startTime;
	private boolean isScheduled;

	public IndexingStartNotification(String collection, String indexingType, long startTime, boolean isScheduled) {
		this.collection = collection;
		this.indexingType = indexingType;
		this.startTime = startTime;
		this.isScheduled = isScheduled;
	}

	@Override
	public void readFrom(StreamInput input) throws IOException {
		collection = input.readString();
		indexingType = input.readString();
		startTime = input.readLong();
		isScheduled = input.readBoolean();
	}

	@Override
	public void writeTo(StreamOutput output) throws IOException {
		output.writeString(collection);
		output.writeString(indexingType);
		output.writeLong(startTime);
		output.writeBoolean(isScheduled);
	}

	@Override
	public String toString() {
		return "색인시작됨 : collection["+collection+"] type[" + indexingType+ "] 자동["+isScheduled+"]";
	}

	@Override
	public String toMessageString() {
		// TODO Auto-generated method stub
		return null;
	}

}
