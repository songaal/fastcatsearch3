package org.fastcatsearch.module;

import org.fastcatsearch.settings.Settings;

public abstract class AbstractModule {
	protected Settings settings;
	public AbstractModule(Settings settings){
		this.settings = settings;
	}
	public abstract void start();
	public abstract void stop();
	
}
