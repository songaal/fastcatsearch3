package org.fastcatsearch.notification;

import java.io.IOException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.notification.message.Notification;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.DynamicClassLoader;

public class NotificationJob extends Job implements Streamable {
	private static final long serialVersionUID = 1084526563289625615L;
	private Notification notification;

	public NotificationJob(){ }
	
	public NotificationJob(Notification notification) {
		this.notification = notification;
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {
		NotificationService notificationService = ServiceManager.getInstance().getService(NotificationService.class);
		notificationService.handleNotification(notification);
		return new JobResult(true);
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		String className = input.readString();
		notification = DynamicClassLoader.loadObject(className, Notification.class);
		notification.readFrom(input);
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(notification.getClass().getName());
		notification.writeTo(output);
	}

}
