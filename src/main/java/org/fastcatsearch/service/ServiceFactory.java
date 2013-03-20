package org.fastcatsearch.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fastcatsearch.env.Environment;
import org.fastcatsearch.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceFactory {
	private final Logger logger = LoggerFactory.getLogger(ServiceFactory.class);
	
	private static ServiceFactory instance;
	private Environment environment;
	private Map<Class<?>, AbstractService> serviceMap;
	
	public static ServiceFactory getInstance(){
		return instance;
	}
	
	public void asSingleton(){
		instance = this;
	}
	
	public ServiceFactory(Environment environment){
		this.environment = environment;
		serviceMap = new ConcurrentHashMap<Class<?>, AbstractService>();
	}
	
	public <T extends AbstractService> T createService(String serviceName, Class<T> serviceClass){
		try {
			Constructor<T> construct = serviceClass.getConstructor(Environment.class, Settings.class);
			T t = construct.newInstance(environment, environment.settingHandler().getSettings());
			serviceMap.put(serviceClass, t);
			return t;
		} catch (Exception e) {
			logger.error("can not make instance of class <{}>", serviceClass.getName());
		}
		return null;
	}
	
	
	public <T extends AbstractService> T getService(Class<T> serviceClass) {
		return (T) serviceMap.get(serviceClass);
	}

}
