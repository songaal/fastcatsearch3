package org.fastcatsearch.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.fastcatsearch.alert.ClusterAlertService;
import org.fastcatsearch.common.ThreadPoolFactory;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.http.action.HttpAction;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.util.ClassScanner;
import org.fastcatsearch.util.DynamicClassLoader;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class HttpRequestService extends AbstractService implements HttpServerAdapter {

	private HttpTransportModule transportModule;
	private HttpServiceController serviceController;
	private HttpSessionManager httpSessionManager;
	
	public HttpRequestService(Environment environment, Settings settings, ServiceManager serviceManager) throws FastcatSearchException {
		super(environment, settings, serviceManager);
	}

	@Override
	protected boolean doStart() throws FastcatSearchException {
		int servicePort = environment.settingManager().getIdSettings().getInt("servicePort");
		transportModule = new HttpTransportModule(environment, settings, servicePort);
		transportModule.httpServerAdapter(this);
		ExecutorService executorService = ThreadPoolFactory.newCachedDaemonThreadPool("http-execute-pool", settings.getInt("execute_pool_size", 300));
		httpSessionManager = new HttpSessionManager(settings.getInt("session_expire_hour", 1));
		serviceController = new HttpServiceController(executorService, httpSessionManager);
		if (!transportModule.load()) {
			throw new FastcatSearchException("can not load transport module!");
		}

		Map<String, HttpAction> actionMap = new HashMap<String, HttpAction>();

		if (environment.isMasterNode()) {
			// master노드에서만 실행되는 액션들 등록..
			String[] actionBasePackageList = settings.getStringArray("master-action-base-package", ",");
			if (actionBasePackageList != null) {
				scanActions(actionMap, actionBasePackageList);
			}
		}
		String[] actionBasePackageList = settings.getStringArray("action-base-package", ",");
		if (actionBasePackageList != null) {
			scanActions(actionMap, actionBasePackageList);
		}

		serviceController.setActionMap(actionMap);
		return true;
	}

	private void scanActions(Map<String, HttpAction> actionMap, String[] actionBasePackageList) {
		for (String actionBasePackage : actionBasePackageList) {
            logger.info("======================================================");
            logger.info("======= Scan Action {} =========", actionBasePackage);
            logger.info("======================================================");
			scanActions(actionMap, actionBasePackage);
		}
	}

	// 하위패키지까지 모두 포함되도록 한다.
	private void scanActions(final Map<String, HttpAction> actionMap, String actionBasePackage) {
		ClassScanner<HttpAction> scanner = new ClassScanner<HttpAction>() {
			@Override
			public HttpAction done(String ename, String pkg, Object param) {
				registerAction(actionMap, ename);
				return null;
			}
		};
		try {
			scanner.scanClass(actionBasePackage, null);
		} catch (IOException e) {
			logger.error("", e);
			ClusterAlertService.getInstance().alert(e);
		}
	}
	
	public void registerAction(String className, String pathPrefix) {
		registerAction(serviceController.getActionMap(), className, pathPrefix);
	}
	
	private void registerAction(Map<String, HttpAction> actionMap, String className) {
		registerAction(actionMap, className, null);
	}
	private void registerAction(Map<String, HttpAction> actionMap, String className, String pathPrefix) {
		if(className == null){
			logger.warn("Cannot register action class name >> {} : {}", className, pathPrefix);
			return;
		}

		try {
			Class<?> actionClass = DynamicClassLoader.loadClass(className);
			// logger.debug("className > {} => {}",className , actionClass);
			// actionClass 가 serviceAction을 상속받은 경우만 등록.
			if (actionClass != null) {
				if (ServiceAction.class.isAssignableFrom(actionClass)) {
					HttpAction actionObj = null;
					ActionMapping actionMapping = actionClass.getAnnotation(ActionMapping.class);
					// annotation이 존재할 경우만 사용.
					if (actionMapping != null) {
						String path = actionMapping.value();
						if(pathPrefix != null){
							path = pathPrefix + path;
						}
						ActionMethod[] method = actionMapping.method();
						actionObj = (HttpAction) actionClass.newInstance();
						if (actionObj != null) {
							actionObj.setMethod(method);
							actionObj.setEnvironement(environment);
							
							// 권한 필요한 액션일 경우.
							if (actionObj instanceof AuthAction) {
								AuthAction authAction = (AuthAction) actionObj;
								authAction.setAuthority(actionMapping.authority(), actionMapping.authorityLevel());
							}

                            for(ActionMethod actionMethod : method) {
                                /*
                                * path 앞에 Method를 붙여준다.
                                * */
                                String actionPath = actionMethod.name().toUpperCase() + " " + path;
                                if(actionMap.containsKey(actionPath)) {
                                    continue;
                                }
                                actionMap.put(actionPath, actionObj);
                                if (actionObj instanceof AuthAction) {
                                    logger.debug("Register {}, action={}, authority={}, authorityLevel={}", actionPath, actionObj, actionMapping.authority(), actionMapping.authorityLevel());
                                } else {
                                    logger.debug("Register {}, action={}", actionPath, actionObj);
                                }
                            }

						}

					}
				}
			}
		} catch (InstantiationException e) {
			logger.error("action load error! " + className, e);
		} catch (IllegalAccessException e) {
			logger.error("action load error! " + className, e);
		}
	}

	@Override
	protected boolean doStop() throws FastcatSearchException {
		transportModule.doUnload();
		httpSessionManager.close();
		return true;
	}

	@Override
	protected boolean doClose() throws FastcatSearchException {
		serviceController = null;
		transportModule = null;
		httpSessionManager = null;
		return true;
	}

	@Override
	public void dispatchRequest(HttpRequest request, HttpChannel channel) {
		serviceController.dispatchRequest(request, channel);
	}

}
