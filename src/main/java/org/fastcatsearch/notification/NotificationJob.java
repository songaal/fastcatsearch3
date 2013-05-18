package org.fastcatsearch.notification;

import java.io.IOException;

import org.fastcatsearch.common.DynamicClassLoader;
import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.job.StreamableJob;
import org.fastcatsearch.notification.message.Notification;
import org.fastcatsearch.service.ServiceManager;

public class NotificationJob extends StreamableJob {
	private static final long serialVersionUID = 1084526563289625615L;
	private Notification notification;

	public NotificationJob(Notification notification) {
		this.notification = notification;
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {
		NotificationService notificationService = ServiceManager.getInstance().getService(NotificationService.class);
		notificationService.notify(notification);
		return new JobResult(true);
	}

	@Override
	public void readFrom(StreamInput input) throws IOException {
		String className = input.readString();
		notification = DynamicClassLoader.loadObject(className, Notification.class);
	}

	@Override
	public void writeTo(StreamOutput output) throws IOException {
		output.writeString(notification.getClass().getName());
		notification.writeTo(output);
	}

}
