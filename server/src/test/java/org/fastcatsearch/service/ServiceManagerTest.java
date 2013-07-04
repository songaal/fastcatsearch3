package org.fastcatsearch.service;

import org.fastcatsearch.common.FastcatSearchTest;
import org.fastcatsearch.control.JobService;
import org.junit.Test;
import static org.junit.Assert.*;

public class ServiceManagerTest extends FastcatSearchTest {

	@Test
	public void test() {
		ServiceManager serviceManager = new ServiceManager(environment);
		serviceManager.asSingleton();
		ServiceManager serviceFactory2 = ServiceManager.getInstance();
		assertEquals(serviceManager, serviceFactory2);
		
		JobService expected = serviceManager.createService("jobService", JobService.class);
		
		JobService actual = serviceManager.getService(JobService.class);
		assertEquals(expected, actual);
		
		JobService actual2 = JobService.getInstance();
		assertEquals(expected, actual2);
		assertEquals(actual, actual2);
	}

}
