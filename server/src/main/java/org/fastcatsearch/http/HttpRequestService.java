package org.fastcatsearch.http;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.fastcatsearch.common.ThreadPoolFactory;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.http.action.HttpAction;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.PluginSetting;
import org.fastcatsearch.plugin.PluginSetting.Action;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.util.DynamicClassLoader;
import org.jboss.netty.handler.codec.http.HttpRequest;

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
		httpSessionManager.setExpireTimeInHour(settings.getInt("session_expire_hour", 24)); // 24시간.
		serviceController = new HttpServiceController(executorService, httpSessionManager);
		if (!transportModule.load()) {
			throw new FastcatSearchException("can not load transport module!");
		}

		Map<String, HttpAction> actionMap = new HashMap<String, HttpAction>();
		List<String> actionBasePackage = settings.getList("action-base-package", String.class);
		scanActions(actionMap, actionBasePackage);

		// List<Settings> actionList = settings.getSettingList("action_list");
		// for (Settings action : actionList) {
		// String path = action.getString("path");
		// String actionClassName = action.getString("action");
		// addtoMap(actionMap, path, actionClassName);
		// }

		// //// plugin action
		PluginService pluginService = serviceManager.getService(PluginService.class);
		for (Plugin plugin : pluginService.getPlugins()) {
			PluginSetting pluginSetting = plugin.getPluginSetting();
			for (Action action : pluginSetting.getActionList()) {
				// addtoMap(actionMap, action.getPath(), action.getClassName());
				registerAction(actionMap, action.getClassName(), false);
			}
		}

		// //////////
		serviceController.setActionMap(actionMap);
		return true;
	}

	private void scanActions(Map<String, HttpAction> actionMap, List<String> actionBasePackageList) {
		for(String actionBasePackage : actionBasePackageList){
			scanActions(actionMap, actionBasePackage);
		}
	}
	
	private void scanActions(Map<String, HttpAction> actionMap, String actionBasePackage) {
		String path = actionBasePackage.replace(".", "/");
		if (!path.endsWith("/")) {
			path = path + "/";
		}
		try {
			String findPath = path;// +"**/*.class";
			Enumeration<URL> em = DynamicClassLoader.getResources(findPath);
			logger.debug("findPath >> {}, {}",findPath, em);
			while (em.hasMoreElements()) {
				String urlstr = em.nextElement().toString();
				logger.debug("urlstr >> {}", urlstr);
				if (urlstr.startsWith("jar:file:")) {
					String jpath = urlstr.substring(9);
					int st = jpath.indexOf("!/");
					jpath = jpath.substring(0, st);
					JarFile jf = new JarFile(jpath);
					Enumeration<JarEntry> jee = jf.entries();
					while (jee.hasMoreElements()) {
						JarEntry je = jee.nextElement();
						String ename = je.getName();
						registerAction(actionMap, ename, true);
					}
				} else if (urlstr.startsWith("file:")) {
					logger.debug("urlstr >> {}", urlstr);
					File file = new File(urlstr.substring(5));
					File[] dir = file.listFiles();
					for (int i = 0; i < dir.length; i++) {
						registerAction(actionMap, path + dir[i].getName(), true);
					}
				}
			}
		} catch (IOException e) {
			logger.error("action load error!", e);
		}
	}

	private void registerAction(Map<String, HttpAction> actionMap, String className, boolean isFile) {
		if (isFile) {
			if (className.endsWith(".class")) {
				className = className.substring(0, className.length() - 6);
				className = className.replaceAll("/", ".");
			} else {
				// file인데 class로 끝나지 않으면 무시.
				return;
			}
		}

		try {
			Class<?> actionClass = DynamicClassLoader.loadClass(className);
//			logger.debug("className > {} => {}",className , actionClass);
			// actionClass 가 serviceAction을 상속받은 경우만 등록.
			if (actionClass != null) {
				if (ServiceAction.class.isAssignableFrom(actionClass)) {
					HttpAction actionObj = null;
					ActionMapping actionMapping = actionClass.getAnnotation(ActionMapping.class);
					// annotation이 존재할 경우만 사용.
					if (actionMapping != null) {
						String path = actionMapping.value();
						ActionMethod[] method = actionMapping.method();
						actionObj = (HttpAction) actionClass.newInstance();
						actionObj.setMethod(method);
						try {
							logger.debug("ACTION path={}, action={}, method={}", path, actionObj, method);
							if (actionObj != null) {
								actionMap.put(path, actionObj);
							}
						} catch (Exception e) {
							logger.error("", e);
						}

					}
				}
			}
		} catch (InstantiationException e) {
			logger.error("action load error!", e);
		} catch (IllegalAccessException e) {
			logger.error("action load error!", e);
		}
	}

	// private void addtoMap(Map<String, HttpAction> actionMap, String path, String actionClassName){
	// HttpAction actionObj = DynamicClassLoader.loadObject(actionClassName, HttpAction.class);
	//
	// try {
	// logger.debug("ACTION path={}, action={}", path, actionObj);
	// if (actionObj != null) {
	// actionMap.put(path, actionObj);
	// }
	// } catch (Exception e) {
	// logger.error("", e);
	// }
	// }
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
