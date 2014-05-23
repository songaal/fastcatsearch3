package org.fastcatsearch.transport;

import java.io.File;
import java.io.FilenameFilter;
import java.net.InetSocketAddress;
import java.util.Collection;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.control.JobExecutor;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.TestJob;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.transport.common.SendFileResultFuture;
import org.fastcatsearch.util.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransportModuleTest {
	private static Logger logger = LoggerFactory.getLogger(TransportModuleTest.class);
	
	JobExecutor executor = new JobExecutor() {
		
		
		@Override
		public ResultFuture offer(Job job) {
			job.setJobExecutor(this);
			job.run();
			return new ResultFuture();
		}

		@Override
		public int runningJobSize() {
			return 0;
		}

		@Override
		public int inQueueJobSize() {
			return 0;
		}

		@Override
		public void result(Job job, Object result, boolean isSuccess) {
			
		}

	};
	
	public static void main(String[] args) throws FastcatSearchException, TransportException {
		String sourceDirPath = args[0];
		String targetDirPath = args[1];
		Environment environment = new Environment(targetDirPath);
//		new TransportServiceTest().testSendMessage(environment);
		new TransportModuleTest().testSendDir(environment, sourceDirPath, targetDirPath);
	}
	
	public void testSendMessage(Environment environment) throws FastcatSearchException, TransportException {
		Settings settings = new Settings();
		Settings settings2 = new Settings();
		
		TransportModule transportService1 = new TransportModule(environment, settings, 9100, executor);
		TransportModule transportService2 = new TransportModule(environment, settings2, 9200, executor);
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

	public void testSendDir(Environment environment, String sourceDirPath, String targetDirPath) throws FastcatSearchException, TransportException {
		Settings settings = new Settings();
		Settings settings2 = new Settings();
		
		TransportModule transportService1 = new TransportModule(environment, settings, 9100, executor);
		TransportModule transportService2 = new TransportModule(environment, settings2, 9200, executor);
		transportService1.load();
		transportService2.load();
		
		Node node1 = new Node("node-1", "", "localhost", 9100);
		Node node2 = new Node("node-2", "", "localhost", 9200);
		node1.setActive();
		node1.setEnabled();
		node2.setActive();
		node2.setEnabled();
		//미리 접속이 안되었을 경우를 가정.
//		transportService1.connectToNode(node2);
//		transportService2.connectToNode(node1);
		
		
		File f = new File(sourceDirPath);
		System.out.println("sourceDirPath > " + sourceDirPath);
		Collection<File> fileList = FileUtils.listFiles(f, null, true);
		int totalCount = fileList.size();
		System.out.println("fileList > " + fileList.size());
		int i = 1;
		for(File sf : fileList) {
			String name = sf.getName();
			System.out.println("File " + i + " / "  + totalCount);
			sendFile(environment, sf, new File(name), transportService1, node2);
			i++;
		}
	}
	public void sendFile(Environment environment, File sourceFile, File targetFile, TransportModule transportService, Node node) throws FastcatSearchException, TransportException {
		
		SendFileResultFuture future = transportService.sendFile(node, sourceFile, targetFile);
		Object obj = future.take();
		logger.debug("result >> {}", obj);
		
	}
	@Test
	public void test3(){
		File home = new File(".");
		String filePath = "collection/AdbeRdr11000_ko_KR.dmg";
		File file = new File(home, filePath);
		logger.debug("home : {}", home.getPath());
		logger.debug("filepath : {}", file.getPath());
	}
	
}
