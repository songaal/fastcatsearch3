package org.fastcatsearch.transport;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.control.JobExecutor;
import org.fastcatsearch.control.JobResult;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.ir.config.IRConfig;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.TestJob;
import org.fastcatsearch.service.ServiceException;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.transport.common.ResultFuture;
import org.fastcatsearch.transport.common.SendFileResultFuture;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransportModuleTest {
	private static Logger logger = LoggerFactory.getLogger(TransportModuleTest.class);
	
	JobExecutor executor = new JobExecutor() {
		
		@Override
		public void result(long jobId, Job job, Object result, boolean isSuccess,
				long st, long et) {
			
		}
		
		@Override
		public JobResult offer(Job job) {
			job.setJobExecutor(this);
			job.run();
			return null;
		}
	};
	
	public static void main(String[] args) throws ServiceException, TransportException {
		IRSettings.setHome("testHome/fastcatsearch");
		Environment environment = new Environment("testHome/fastcatsearch");
//		new TransportServiceTest().testSendMessage(environment);
		new TransportModuleTest().testSendFile(environment);
	}
	
	public void testSendMessage(Environment environment) throws ServiceException, TransportException {
		Settings settings = new Settings();
		settings.put("node_port", 9100);
		Settings settings2 = new Settings();
		settings2.put("node_port", 9200);
		
		TransportModule transportService1 = new TransportModule(environment, settings, executor);
		TransportModule transportService2 = new TransportModule(environment, settings2, executor);
		transportService1.load();
		transportService2.load();
		
		Node node1 = new Node("node-1", new InetSocketAddress("localhost", 9100));
		Node node2 = new Node("node-2", new InetSocketAddress("localhost", 9200));
		//미리 접속이 안되었을 경우를 가정.
//		transportService1.connectToNode(node2);
//		transportService2.connectToNode(node1);
		
		TestJob request = new TestJob("transport-test");
		
		ResultFuture future = transportService1.sendRequest(node2, request);
		Object obj = future.take();
		logger.debug("result >> {}", obj);
		
		
		transportService1.unload();
		transportService2.unload();
	}

	
	public void testSendFile(Environment environment) throws ServiceException, TransportException {
		Settings settings = new Settings();
		settings.put("node_port", 9100);
		Settings settings2 = new Settings();
		settings2.put("node_port", 9200);
		
		
		TransportModule transportService1 = new TransportModule(environment, settings, executor);
		TransportModule transportService2 = new TransportModule(environment, settings2, executor);
		transportService1.load();
		transportService2.load();
		
		Node node1 = new Node("node-1", "localhost", 9100);
		Node node2 = new Node("node-2", "localhost", 9200);
		//미리 접속이 안되었을 경우를 가정.
//		transportService1.connectToNode(node2);
//		transportService2.connectToNode(node1);
		
		String filePath = "/Users/swsong/Downloads/git-1.8.1.3-intel-universal-snow-leopard.dmg";
		File sourceFile = new File(filePath);
		File tartgetFile = new File("collection/a.dmg");
		SendFileResultFuture future = transportService1.sendFile(node2, sourceFile, tartgetFile);
		Object obj = future.take();
		logger.debug("result >> {}", obj);
		
	}
	@Test
	public void test3(){
		IRSettings.setHome("testHome/fastcatsearch");
		File home = new File(IRSettings.HOME);
		String filePath = "collection/AdbeRdr11000_ko_KR.dmg";
		File file = new File(home, filePath);
		logger.debug("home : {}", home.getPath());
		logger.debug("filepath : {}", file.getPath());
	}
	
}
