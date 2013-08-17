package org.fastcatsearch.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.fastcatsearch.common.ThreadPoolFactory;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.service.action.HttpAction;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.util.DynamicClassLoader;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class HttpRequestService extends AbstractService implements HttpServerAdapter {

	private HttpTransportModule transportModule;
	private final HttpServiceController serviceController;
	
	public HttpRequestService(Environment environment, Settings settings, ServiceManager serviceManager) throws FastcatSearchException {
		super(environment, settings, serviceManager);
		transportModule = new HttpTransportModule(environment, settings);
		transportModule.httpServerAdapter(this);
		ExecutorService executorService = ThreadPoolFactory.newUnlimitedCachedDaemonThreadPool("http-execute-pool"); 
		serviceController = new HttpServiceController(executorService);
		if(!transportModule.load()){
			throw new FastcatSearchException("can not load transport module!");
		}
		
		
		List<Settings> actionList = settings.getSettingList("action_list");
		Map<String, Class<HttpAction>> actionMap = new HashMap<String, Class<HttpAction>>();
		for (Settings action : actionList) {
			String uri = action.getString("uri");
			String actionClassName = action.getString("action");
			Class<HttpAction> actionObj = (Class<HttpAction>) DynamicClassLoader.loadClass(actionClassName);
			actionMap.put(uri,  actionObj);
		}
		serviceController.setActionMap(actionMap);
		
	}
	
	@Override
	protected boolean doStart() throws FastcatSearchException {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	protected boolean doStop() throws FastcatSearchException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean doClose() throws FastcatSearchException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dispatchRequest(HttpRequest request, HttpChannel channel) {
		 serviceController.dispatchRequest(request, channel);		
	}

}
