package org.fastcatsearch.notification;

import org.fastcatsearch.env.Environment;
import org.fastcatsearch.module.AbstractModule;
import org.fastcatsearch.module.ModuleException;
import org.fastcatsearch.notification.message.Notification;
import org.fastcatsearch.settings.Settings;

public class EmailNotificationModule extends AbstractModule {

	public EmailNotificationModule(Environment environment, Settings settings) {
		super(environment, settings);
	}

	@Override
	protected boolean doLoad() throws ModuleException {
		return false;
	}

	@Override
	protected boolean doUnload() throws ModuleException {
		return false;
	}

	public void sendEmail(Notification notification){
		
	}
}
