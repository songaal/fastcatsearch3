package org.fastcatsearch.transport;

import java.io.File;
import java.net.InetSocketAddress;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.control.JobController;
import org.fastcatsearch.ir.config.IRConfig;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.job.TestJob;
import org.fastcatsearch.service.ServiceException;
import org.fastcatsearch.transport.common.ResultFuture;
import org.fastcatsearch.transport.common.SendFileResultFuture;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransportServiceTest {
	private static Logger logger = LoggerFactory.getLogger(TransportServiceTest.class);
	
	public static void main(String[] args) throws ServiceException, TransportException {
		IRSettings.setHome("testHome/fastcatsearch");
		JobController jobController = JobController.getInstance();
		jobController.setUseJobScheduler(false);
		jobController.start();
//		new TransportServiceTest().testSendMessage();
		new TransportServiceTest().testSendFile();
	}
	
	public void testSendMessage() throws ServiceException, TransportException {
		IRConfig config = IRSettings.getConfig();
		IRConfig config2 = IRSettings.getConfig(true);
		config2.getProperties().put("node_port", "9200");
		
		TransportService transportService1 = new TransportService(config);
		TransportService transportService2 = new TransportService(config2);
		transportService1.start();
		transportService2.start();
		
		Node node1 = new Node("node-1", "node-1", new InetSocketAddress("localhost", 9100));
		Node node2 = new Node("node-2", "node-2", new InetSocketAddress("localhost", 9200));
		//미리 접속이 안되었을 경우를 가정.
//		transportService1.connectToNode(node2);
//		transportService2.connectToNode(node1);
		
		TestJob request = new TestJob("transport-test");
		
		ResultFuture future = transportService1.sendRequest(node2, request);
		Object obj = future.take();
		logger.debug("result >> {}", obj);
		
		
		transportService1.shutdown();
		transportService2.shutdown();
	}

	
	public void testSendFile() throws ServiceException, TransportException {
		IRConfig config = IRSettings.getConfig();
		IRConfig config2 = IRSettings.getConfig(true);
		config2.getProperties().put("node_port", "9200");
		
		TransportService transportService1 = new TransportService(config);
		TransportService transportService2 = new TransportService(config2);
		transportService1.start();
		transportService2.start();
		
		Node node1 = new Node("node-1", "node-1", new InetSocketAddress("localhost", 9100));
		Node node2 = new Node("node-2", "node-2", new InetSocketAddress("localhost", 9200));
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
