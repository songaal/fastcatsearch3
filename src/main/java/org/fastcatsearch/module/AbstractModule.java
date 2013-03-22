package org.fastcatsearch.module;

import org.fastcatsearch.env.Environment;
import org.fastcatsearch.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractModule {
	
	protected static final Logger logger = LoggerFactory.getLogger(AbstractModule.class);
	
	protected Environment environment;
	protected Settings settings;
	private boolean isLoaded;
	
	public AbstractModule(Environment environment, Settings settings){
		this.environment = environment;
		this.settings = settings;
	}
	
	public boolean load(){
		if(doLoad()){
			logger.info("Load module {}", getClass().getSimpleName());
			isLoaded = true;
			return true;
			
		}else{
			return false;
		}
		
	}
	public boolean unload(){
		if(doUnload()){
			logger.info("Unload module {}", getClass().getSimpleName());
			isLoaded = true;
			return true;
			
		}else{
			return false;
		}
	}
	
	protected abstract boolean doLoad();
	protected abstract boolean doUnload();
	
	
	public Settings settings(){
		return settings;
	}
}
