package org.fastcatsearch.data;

import org.fastcatsearch.env.Environment;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceException;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;

public class DataService extends AbstractService {

	
	private static DataService instance;
	
	public DataService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
	}

	@Override
	public void asSingleton() {
		instance = this;
	}

	@Override
	protected boolean doStart() throws ServiceException {
		return true;
	}

	@Override
	protected boolean doStop() throws ServiceException {
		return true;
	}

	@Override
	protected boolean doClose() throws ServiceException {
		return true;
	}

}
