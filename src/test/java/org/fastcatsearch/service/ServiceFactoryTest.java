package org.fastcatsearch.service;

import org.fastcatsearch.common.FastcatSearchTest;
import org.fastcatsearch.control.JobService;
import org.junit.Test;
import static org.junit.Assert.*;

public class ServiceFactoryTest extends FastcatSearchTest {

	@Test
	public void test() {
		ServiceManager serviceFactory = new ServiceManager(environment);
		serviceFactory.asSingleton();
		ServiceManager serviceFactory2 = ServiceManager.getInstance();
		assertEquals(serviceFactory, serviceFactory2);
		
		
		JobService expected = serviceFactory.createService("jobController", JobService.class);
		expected.asSingleton();
		
		JobService actual = serviceFactory.getService(JobService.class);
		assertEquals(expected, actual);
		
		JobService actual2 = JobService.getInstance();
		assertEquals(expected, actual2);
		assertEquals(actual, actual2);
	}

}
