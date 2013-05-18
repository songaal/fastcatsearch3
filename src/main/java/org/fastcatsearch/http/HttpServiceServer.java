package org.fastcatsearch.http;

import java.util.concurrent.ExecutorService;

import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class HttpServiceServer extends AbstractService implements HttpServerAdapter {

	HttpTransportModule transportModule;
	private final ServiceController serviceController;
	
	public HttpServiceServer(Environment environment, Settings settings, ServiceManager serviceManager) throws FastcatSearchException {
		super(environment, settings, serviceManager);
		transportModule = new HttpTransportModule(environment, settings);
		transportModule.httpServerAdapter(this);
		ExecutorService executorService = null;
		serviceController = new ServiceController(executorService);
		if(!transportModule.load()){
			throw new FastcatSearchException("can not load transport module!");
		}
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
