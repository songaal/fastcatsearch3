package org.fastcatsearch.service;

import org.fastcatsearch.common.FastcatSearchTest;
import org.fastcatsearch.control.JobController;
import org.junit.Test;
import static org.junit.Assert.*;

public class ServiceFactoryTest extends FastcatSearchTest {

	@Test
	public void test() {
		ServiceFactory serviceFactory = new ServiceFactory(environment);
		serviceFactory.asSingleton();
		ServiceFactory serviceFactory2 = ServiceFactory.getInstance();
		assertEquals(serviceFactory, serviceFactory2);
		
		
		JobController expected = serviceFactory.createService("jobController", JobController.class);
		expected.asSingleton();
		
		JobController actual = serviceFactory.getService(JobController.class);
		assertEquals(expected, actual);
		
		JobController actual2 = actual.getInstance();
		assertEquals(expected, actual2);
		assertEquals(actual, actual2);
	}

}
