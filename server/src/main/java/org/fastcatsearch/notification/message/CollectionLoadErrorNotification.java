package org.fastcatsearch.notification.message;

import java.io.IOException;

import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.transport.vo.StreamableThrowable;

public class CollectionLoadErrorNotification extends Notification {
	
	private String collectionId;
	private Throwable e;
	
	public CollectionLoadErrorNotification(){ }
	
	public CollectionLoadErrorNotification(String collectionId, Throwable e) {
		super("MSG-02000");
		this.collectionId = collectionId;
		this.e = e;
	}

	public String getCollectionId(){
		return collectionId;
	}
	
	public Throwable getCause(){
		return e;
	}
	
	@Override
	public void readFrom(DataInput input) throws IOException {
		super.readFrom(input);
		collectionId = input.readString();
		StreamableThrowable streamableThrowable = new StreamableThrowable();
		streamableThrowable.readFrom(input);
		this.e = streamableThrowable.getThrowable();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		super.writeTo(output);
		output.writeString(collectionId);
		new StreamableThrowable(e).writeTo(output);
	}

	@Override
	public String toMessageString() {
		String code = e.getStackTrace()[0].toString();
		if(e.getCause() != null) {
			code = code + "\n Cause : " + e.getCause().getStackTrace()[0].toString();
		}
		return getFormattedMessage(origin.id(), collectionId, code);
	}

}
