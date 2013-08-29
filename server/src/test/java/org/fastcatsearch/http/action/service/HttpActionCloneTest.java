package org.fastcatsearch.http.action.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.http.impl.DefaultHttpRequestFactory;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.service.TestAction;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.junit.Test;

public class HttpActionCloneTest {

	int count = 1000000000;
	String type = "xml";
	
	@Test
	public void testNew() {
		long st = System.nanoTime();
		for (int i = 0; i < count; i++) {
			TestAction action = new TestAction(type);
		}
		printTime("Test New", st);
	}
	
	@Test
	public void testContructor() throws SecurityException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Constructor<TestAction> constructor = (Constructor<TestAction>) Class.forName("org.fastcatsearch.http.service.action.TestAction").getConstructor(String.class);
		long st = System.nanoTime();
		for (int i = 0; i < count; i++) {
			constructor.newInstance(type);
		}
		printTime("Test Contructor", st);
	}
	
	@Test
	public void testClone() throws CloneNotSupportedException {
		long st = System.nanoTime();
		TestAction action = new TestAction(type);
		ActionRequest r = new ActionRequest("", new DefaultHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.POST, "aaa"));
		action.init(r, null, null, null);
		for (int i = 0; i < count; i++) {
			TestAction a2= (TestAction) action.clone();
		}
		printTime("Test Clone", st);
	}
	
	private void printTime(String string, long st) {
		System.out.println(">>"+string+", "+(System.nanoTime() - st)/1000000);
	}
}
