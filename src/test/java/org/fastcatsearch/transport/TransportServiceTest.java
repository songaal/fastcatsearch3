package org.fastcatsearch.transport;

import java.net.InetSocketAddress;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.control.JobController;
import org.fastcatsearch.ir.config.IRConfig;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.job.TestJob;
import org.fastcatsearch.service.ServiceException;
import org.fastcatsearch.transport.TransportService;
import org.fastcatsearch.transport.common.ResultFuture;
import org.junit.Test;

public class TransportServiceTest {

	public static void main(String[] args) throws ServiceException, TransportException {
		IRSettings.setHome("testHome/fastcatsearch");
		JobController jobController = JobController.getInstance();
		jobController.setUseJobScheduler(false);
		jobController.start();
		new TransportServiceTest().test1();
	}
	public void test1() throws ServiceException, TransportException {
//		new Thread(){
//			@Override
//			public void run(){
//				try {
//					Thread.sleep(30000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}.start();
		
		
		
		IRConfig config = IRSettings.getConfig();
		IRConfig config2 = IRSettings.getConfig(true);
		config2.getProperties().put("node_port", "9200");
		
		TransportService transportService1 = new TransportService(config);
		TransportService transportService2 = new TransportService(config2);
		transportService1.start();
		transportService2.start();
		
		Node node1 = new Node("node-1", "node-1", new InetSocketAddress("localhost", 9100));
		Node node2 = new Node("node-2", "node-2", new InetSocketAddress("localhost", 9200));
		transportService1.connectToNode(node2);
		transportService2.connectToNode(node1);
		
		TestJob request = new TestJob("transport-test");
		
		ResultFuture future = transportService1.sendRequest(node2, request);
		Object obj = future.take();
		
		
		
//		transportService1.shutdown();
//		transportService2.shutdown();
	}

}
