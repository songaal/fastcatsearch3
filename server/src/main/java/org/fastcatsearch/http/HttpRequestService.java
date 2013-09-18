package org.fastcatsearch.http;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.common.ThreadPoolFactory;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.http.action.HttpAction;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.PluginSetting;
import org.fastcatsearch.plugin.PluginSetting.Action;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.servlet.WebServiceHttpServlet;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.util.DynamicClassLoader;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.resource.Resource;
import org.mortbay.xml.XmlConfiguration;

public class HttpRequestService extends AbstractService implements HttpServerAdapter {

	private HttpTransportModule transportModule;
	private HttpServiceController serviceController;

	public HttpRequestService(Environment environment, Settings settings, ServiceManager serviceManager) throws FastcatSearchException {
		super(environment, settings, serviceManager);
	}

	@Override
	protected boolean doStart() throws FastcatSearchException {
		transportModule = new HttpTransportModule(environment, settings);
		transportModule.httpServerAdapter(this);
		ExecutorService executorService = ThreadPoolFactory.newUnlimitedCachedDaemonThreadPool("http-execute-pool");
		HttpSessionManager httpSessionManager = new HttpSessionManager();
		httpSessionManager.setExpireTimeInHour(settings.getInt("session_expire_hour", 24)); //24시간.
		serviceController = new HttpServiceController(executorService, httpSessionManager);
		if (!transportModule.load()) {
			throw new FastcatSearchException("can not load transport module!");
		}

		List<Settings> actionList = settings.getSettingList("action_list");
		Map<String, HttpAction> actionMap = new HashMap<String, HttpAction>();
		for (Settings action : actionList) {
			String path = action.getString("path");
			String actionClassName = action.getString("action");
			addtoMap(actionMap, path, actionClassName);
		}
		
		////// plugin action
		PluginService pluginService = serviceManager.getService(PluginService.class);
		for (Plugin plugin : pluginService.getPlugins()) {
			PluginSetting pluginSetting = plugin.getPluginSetting();
			for(Action action : pluginSetting.getActionList()){
				addtoMap(actionMap, action.getPath(), action.getClassName());
			}
		}
		
		////////////
		serviceController.setActionMap(actionMap);
		return true;
	}

	private void addtoMap(Map<String, HttpAction> actionMap, String path, String actionClassName){
		HttpAction actionObj = DynamicClassLoader.loadObject(actionClassName, HttpAction.class);

		try {
			logger.debug("ACTION path={}, action={}", path, actionObj);
			if (actionObj != null) {
				actionMap.put(path, actionObj);
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}
	@Override
	protected boolean doStop() throws FastcatSearchException {
		transportModule.doUnload();
		return true;
	}

	@Override
	protected boolean doClose() throws FastcatSearchException {
		serviceController = null;
		transportModule = null;
		return true;
	}

	@Override
	public void dispatchRequest(HttpRequest request, HttpChannel channel) {
		serviceController.dispatchRequest(request, channel);
	}

}
