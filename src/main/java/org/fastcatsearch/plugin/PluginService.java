package org.fastcatsearch.plugin;

import java.util.List;

import org.fastcatsearch.env.Environment;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceException;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;

public class PluginService extends AbstractService {

	public PluginService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
	} 

	@Override
	public void asSingleton() {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean doStart() throws ServiceException {
		//플러그인을 검색하여 
		//무작위로 시작한다.
		List<Plugin> pluginList = null;
		
		
		return false;
	}

	@Override
	protected boolean doStop() throws ServiceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean doClose() throws ServiceException {
		// TODO Auto-generated method stub
		return false;
	}

}
