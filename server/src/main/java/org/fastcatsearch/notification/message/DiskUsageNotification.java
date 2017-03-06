package org.fastcatsearch.notification.message;

import java.io.IOException;

import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class DiskUsageNotification extends Notification {
	
	private int diskUsage;
	
	public DiskUsageNotification(){ }
	
	public DiskUsageNotification(int diskUsage){
		super("MSG-00101");
		this.diskUsage = diskUsage;
	}
	
	
	@Override
	public void readFrom(DataInput input) throws IOException {
		super.readFrom(input);
		diskUsage = input.readInt();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		super.writeTo(output);
		output.writeInt(diskUsage);
	}

	@Override
	public String toMessageString() {
		return getFormattedMessage(origin.id(), String.valueOf(diskUsage));
	}

}
