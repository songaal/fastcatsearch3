package org.fastcatsearch.notification.message;

import java.io.IOException;

import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.transport.vo.StreamableThrowable;

public class OutOfMemoryNotification extends Notification {
	
	private Throwable e;
	
	public OutOfMemoryNotification(){ }
	
	public OutOfMemoryNotification(Throwable e) {
		super("MSG-00100");
		this.e = e;
	}

	public Throwable getCause(){
		return e;
	}
	
	@Override
	public void readFrom(DataInput input) throws IOException {
		super.readFrom(input);
		StreamableThrowable streamableThrowable = new StreamableThrowable();
		streamableThrowable.readFrom(input);
		this.e = streamableThrowable.getThrowable();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		super.writeTo(output);
		new StreamableThrowable(e).writeTo(output);
	}

	@Override
	public String toMessageString() {
		return getFormattedMessage(origin.id(), e.getStackTrace()[0].toString());
	}

}
