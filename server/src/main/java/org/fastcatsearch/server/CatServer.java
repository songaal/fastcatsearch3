/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.server;

import org.fastcatsearch.alert.ClusterAlertService;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.db.DBService;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.http.HttpRequestService;
import org.fastcatsearch.ir.CollectionQueryCountService;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.job.state.TaskStateService;
import org.fastcatsearch.management.SystemWatchService;
import org.fastcatsearch.notification.NotificationService;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.processlogger.ProcessLoggerService;
import org.fastcatsearch.service.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.CountDownLatch;

public class CatServer {

	private ServiceManager serviceManager;

	public static long startTime;
	public static CatServer instance;
	private static Logger logger;
	private boolean isRunning;

	private Thread shutdownHook;
	protected boolean keepAlive;

	private String serverHome;
	private static volatile Thread keepAliveThread;
	private static volatile CountDownLatch keepAliveLatch;
	private FileLock fileLock;
	private File lockFile;

	public static void main(String... args) throws FastcatSearchException {
		if (args.length < 1) {
			usage();
			return;
		}

		CatServer server = new CatServer(args[0]);
		if (server.load(args)) {
			server.start();
		}
	}

	public void setKeepAlive(boolean b) {
		keepAlive = b;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public static CatServer getInstance() {
		return instance;
	}

	public CatServer() {
	}

	public CatServer(String serverHome) {
		this.serverHome = serverHome;
	}

	public boolean load() {
		return load(null);
	}

	public boolean load(String[] args) {

		boolean isConfig = false;

		// load 파라미터는 없을수도 있다.
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				if (isConfig) {
					isConfig = false;
				} else if (args[i].equals("-config")) {
					isConfig = true;
				} else if (args[i].equals("-help")) {
					usage();
					return false;
				}
			}
		}

		setKeepAlive(true);
		return true;

	}

	protected static void usage() {

		System.out.println("usage: java " + CatServer.class.getName() + " [ -help -config ]" + " {HomePath}");

	}

	public void start() throws FastcatSearchException {
		// 초기화 및 서비스시작을 start로 옮김.
		// 초기화로직이 init에 존재할 경우, 관리도구에서 검색엔진을 재시작할때, init을 호출하지 않으므로, 초기화를 건너뛰게
		// 됨.
		instance = this;

		if (serverHome == null) {
			System.err.println("Warning! No argument for \"{HomePath}\".");
			usage();
			System.exit(1);
		}

		File f = new File(serverHome);
		if (!f.exists()) {
			System.err.println("Warning! HomePath \"" + serverHome + "\" is not exist!");
			usage();
			System.exit(1);
		}

		if (fileLock == null) {
			lockFile = new File(serverHome, ".lock");
			try {
				FileChannel channel = new RandomAccessFile(lockFile, "rw").getChannel();
				fileLock = channel.tryLock();
			} catch (IOException e) {
				System.err.println("Error! Cannot create lock file \"" + lockFile.getAbsolutePath() + "\".");
				System.exit(1);
			}

			if (fileLock == null) {
				System.err.println("Error! Another instance of CatServer is running at home path = " + serverHome);
				System.exit(1);
			}
		}
		Environment environment = new Environment(serverHome).init();
		logger = LoggerFactory.getLogger(CatServer.class);
		logger.info("File lock > {}", lockFile.getAbsolutePath());

		this.serviceManager = new ServiceManager(environment);
		serviceManager.asSingleton();

		PluginService pluginService = serviceManager.createService("plugin", PluginService.class);

		DBService dbService = serviceManager.createService("db", DBService.class);
		dbService.asSingleton();
		JobService jobService = serviceManager.createService("job", JobService.class);
		jobService.asSingleton();
		IRService irService = serviceManager.createService("ir", IRService.class);
		irService.setAnalyzerFactoryManager(pluginService);
		SystemWatchService systemInfoService = serviceManager.createService("system", SystemWatchService.class);
		NodeService nodeService = serviceManager.createService("node", NodeService.class);

		HttpRequestService httpRequestService = serviceManager.createService("http", HttpRequestService.class);
		NotificationService notificationService = serviceManager.createService("notification", NotificationService.class);
		ClusterAlertService clusterAlertService = serviceManager.createService("alert", ClusterAlertService.class);
		clusterAlertService.asSingleton();
		ProcessLoggerService processLoggerService = serviceManager.createService("processlogger", ProcessLoggerService.class);
		TaskStateService taskStateService = serviceManager.createService("taskstate", TaskStateService.class);
		CollectionQueryCountService collectionQueryCountService = serviceManager.createService("query_count", CollectionQueryCountService.class);

		logger.info("ServerHome = {}", serverHome);
		try {
			//모든 작업풀의 기반이므로 제일먼저 시작.
			jobService.start();
			//분산시스템의 기반이므로 두번째로 시작.
			nodeService.start();
			//문제발생시 알림통로이므로 그 다음으로 시작.
			clusterAlertService.start();
			
			dbService.start();
			//notification서비스는 db서비스를 이용하므로 db가 먼저로딩.
			notificationService.start();
            try {
			    pluginService.start();
            } catch (FastcatSearchException e) {
                logger.error("PluginService 시작에 실패했습니다.", e);
            }
			systemInfoService.start();

			httpRequestService.start();

			processLoggerService.start();
			taskStateService.start();

			irService.start();
			collectionQueryCountService.start();

			// 서비스가 모두 뜬 상태에서 후속작업.
			if (environment.isMasterNode() && pluginService.isRunning()) {
				pluginService.loadAction();
				pluginService.loadSchedule();
			}
			// 색인 스케쥴등록.
			if (environment.isMasterNode() && irService.isRunning()) {
				irService.reloadAllSchedule();
			}

		} catch (FastcatSearchException e) {
			logger.error("CatServer 시작에 실패했습니다.", e);
		}

		irService.registerLoadBanlancer(nodeService);

		if (shutdownHook == null) {
			shutdownHook = new ServerShutdownHook();
			Runtime.getRuntime().addShutdownHook(shutdownHook);
		}

		startTime = System.currentTimeMillis();

		logger.info("CatServer started!");
		isRunning = true;

		if (keepAlive) {
			setKeepAlive();
		}
	}

	private void setKeepAlive() {
		// keepAliveLatch 가 null일때만 실행되면, restart의 경우 이미 keep alive이므로 재실행하지 않는다.
		if (keepAliveLatch == null) {
			keepAliveLatch = new CountDownLatch(1);

			keepAliveThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						keepAliveLatch.await();
					} catch (InterruptedException e) {
						// bail out
					}
				}
			}, "CatServer[keepAlive]");
			keepAliveThread.setDaemon(false);
			keepAliveThread.start();
		}
	}

	public void restart() throws FastcatSearchException {
		logger.info("Restart CatServer!");

		stop();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ignore) {
			// Thread가 인터럽트 걸리므로 한번더 시도.
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ignore2) {
			}
		}
		start();

	}

	public void stop() throws FastcatSearchException {
		serviceManager.stopService(CollectionQueryCountService.class);

		serviceManager.stopService(TaskStateService.class);
		serviceManager.stopService(NotificationService.class);
		serviceManager.stopService(ClusterAlertService.class);
		serviceManager.stopService(ProcessLoggerService.class);

		serviceManager.stopService(HttpRequestService.class);

		serviceManager.stopService(PluginService.class);
		serviceManager.stopService(NodeService.class);
		serviceManager.stopService(SystemWatchService.class);
		serviceManager.stopService(IRService.class);
		serviceManager.stopService(JobService.class);
		serviceManager.stopService(DBService.class);

		logger.info("CatServer shutdown!");
		isRunning = false;
	}

	public void close() throws FastcatSearchException {

		serviceManager.closeService(CollectionQueryCountService.class);

		serviceManager.closeService(TaskStateService.class);
		serviceManager.closeService(NotificationService.class);
		serviceManager.closeService(ClusterAlertService.class);
		serviceManager.closeService(ProcessLoggerService.class);

		serviceManager.closeService(HttpRequestService.class);

		serviceManager.closeService(PluginService.class);
		serviceManager.closeService(NodeService.class);
		serviceManager.closeService(SystemWatchService.class);
		serviceManager.closeService(IRService.class);
		serviceManager.closeService(JobService.class);
		serviceManager.closeService(DBService.class);
		
		if (fileLock != null) {
			try {
				fileLock.release();
				logger.info("CatServer Lock Release! {}", fileLock);
			} catch (IOException e) {
				logger.error("", e);
			}

			try {
				fileLock.channel().close();
			} catch (Exception e) {
				logger.error("", e);
			}

			try {
				lockFile.delete();
				logger.info("Remove .lock file >> {}", lockFile.getAbsolutePath());
			} catch (Exception e) {
				logger.error("", e);
			}
		}

	}

	protected class ServerShutdownHook extends Thread {

		@Override
		public void run() {
			try {
				logger.info("Server Shutdown Requested!");
				CatServer.this.stop();
				CatServer.this.close();
				if (keepAliveLatch != null) {
					keepAliveLatch.countDown();
				}
			} catch (Throwable ex) {
				logger.error("CatServer.shutdownHookFail", ex);
			} finally {
				logger.info("Server Shutdown Complete!");
			}
		}
	}

}
