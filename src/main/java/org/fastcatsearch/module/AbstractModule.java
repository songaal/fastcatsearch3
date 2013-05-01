package org.fastcatsearch.module;

import org.fastcatsearch.env.Environment;
import org.fastcatsearch.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractModule {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractModule.class);

	protected Environment environment;
	protected Settings settings;
	protected boolean isLoaded;

	public AbstractModule(Environment environment, Settings settings) {
		this.environment = environment;
		this.settings = settings;
	}

	public boolean load() throws ModuleException {
		if (doLoad()) {
			logger.info("Load module {}", getClass().getSimpleName());
			isLoaded = true;
			return true;

		} else {
			return false;
		}

	}

	public boolean unload() throws ModuleException {
		if (!isLoaded) {
			logger.info("Module is not loaded. {}", getClass().getSimpleName());
			return false;
		}
		if (doUnload()) {
			logger.info("Unload module {}", getClass().getSimpleName());
			isLoaded = true;
			return true;

		} else {
			return false;
		}
	}

	protected abstract boolean doLoad() throws ModuleException;

	protected abstract boolean doUnload() throws ModuleException;

	public Settings settings() {
		return settings;
	}
}
