package org.fastcatsearch.http;

import java.util.concurrent.ExecutorService;

import org.fastcatsearch.control.JobExecutor;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceException;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.transport.TransportModule;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class HttpServiceServer extends AbstractService implements HttpServerAdapter {

	HttpTransportModule transportModule;
	private final ServiceController serviceController;
	
	public HttpServiceServer(Environment environment, Settings settings, ServiceManager serviceManager) throws ServiceException {
		super(environment, settings, serviceManager);
		transportModule = new HttpTransportModule(environment, settings);
		transportModule.httpServerAdapter(this);
		ExecutorService executorService = null;
		serviceController = new ServiceController(executorService);
		if(!transportModule.load()){
			throw new ServiceException("can not load transport module!");
		}
	}

	@Override
	public void asSingleton() {
		// TODO Auto-generated method stub

	}
	
	@Override
	protected boolean doStart() throws ServiceException {
		// TODO Auto-generated method stub
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

	@Override
	public void dispatchRequest(HttpRequest request, HttpChannel channel) {
		 serviceController.dispatchRequest(request, channel);		
	}

}
