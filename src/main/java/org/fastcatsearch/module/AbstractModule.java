package org.fastcatsearch.module;

import org.fastcatsearch.env.Environment;
import org.fastcatsearch.settings.Settings;

public abstract class AbstractModule {
	protected Environment environment;
	protected Settings settings;
	
	public AbstractModule(Environment environment, Settings settings){
		this.environment = environment;
		this.settings = settings;
	}
	
	public abstract boolean load();
	public abstract boolean unload();
	
	public Settings settings(){
		return settings;
	}
}
