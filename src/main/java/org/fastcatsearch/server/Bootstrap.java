package org.fastcatsearch.server;

import java.util.concurrent.CountDownLatch;

import org.fastcatsearch.control.JobController;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.service.ServiceFactory;

public class Bootstrap {
	
	private static volatile Thread keepAliveThread;
    private static volatile CountDownLatch keepAliveLatch;
	private static Bootstrap bootstrap;
	
	public static void main(String[] args) {
		bootstrap = new Bootstrap();
		bootstrap.init(args);
		bootstrap.start();
		
		
	}
	public void init(String[] args) {
		//검색엔진으로 전달되는 args를 받아서 셋팅해준다.
		//대부분 -D옵션을 통해 전달받으므로 아직까지는 셋팅할 내용은 없다.
	}
	
	public void start(){
		String homeDirPath = "";
		Environment environment = new Environment(homeDirPath);
		ServiceFactory serviceFactory = new ServiceFactory(environment);
		serviceFactory.asSingleton();
		
		JobController jobController = serviceFactory.createService("service", JobController.class);
		jobController.asSingleton();
		
		keepAliveLatch = new CountDownLatch(1);
		
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                keepAliveLatch.countDown();
            }
        });

        keepAliveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    keepAliveLatch.await();
                } catch (InterruptedException e) {
                    // bail out
                }
            }
        }, "elasticsearch[keepAlive]");
        keepAliveThread.setDaemon(false);
        keepAliveThread.start();
	}
	
	public void stop() {
		
	}
}
