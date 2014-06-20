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

package org.fastcatsearch.management;

import java.util.Timer;
import java.util.TimerTask;

import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.notification.NotificationService;
import org.fastcatsearch.notification.message.DiskUsageNotification;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;

public class SystemWatchService extends AbstractService {

	private static long START_DELAY = 1000L;
	private static long INFO_CHECK_PERIOD = 1000L; // 1초마다 InfoCheckerTask를 수행한다.
	private static long DISK_CHECK_PERIOD = 5 * 60 * 1000L; //5분마다 확인. 
	
	private SystemInfoHandler handler;
	private Timer timer;

	private JvmCpuInfo jvmCpuInfoPerSecond = new JvmCpuInfo();
	private JvmMemoryInfo jvmMemoryInfoPerSecond = new JvmMemoryInfo();
	private SystemDiskInfo systemDiskInfo = new SystemDiskInfo();
	
	private static SystemWatchService instance;

	public static SystemWatchService getInstance() {
		return instance;
	}

	public SystemWatchService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
		
		
	}

	public boolean isJvmCpuInfoSupported() {
		return handler.isJvmCpuInfoSupported();
	}

	public boolean isSystemCpuInfoSupported() {
		return handler.isSystemCpuInfoSupported();
	}

	public boolean isLoadAvgInfoSupported() {
		return handler.isLoadAvgInfoSupported();
	}

	public boolean isJvmMemoryInfoSupported() {
		return handler.isJvmMemoryInfoSupported();
	}

	public JvmCpuInfo getJvmCpuInfo() {
		return jvmCpuInfoPerSecond;
	}

	public JvmMemoryInfo getJvmMemoryInfo() {
		return jvmMemoryInfoPerSecond;
	}
	
	public SystemDiskInfo getSystemDiskInfo() {
		return systemDiskInfo;
	}

	class SystemInfoCheckTask extends TimerTask {

		@Override
		public void run() {
			handler.checkJvmCpuInfo(jvmCpuInfoPerSecond);
			handler.checkJvmMemoryInfo(jvmMemoryInfoPerSecond);
			handler.checkSystemDiskInfo(systemDiskInfo);
		}
	}

	class DiskWatchTask extends TimerTask {

		private int diskUsageThreshold;
		private long lastReportTime;
		private int lastDiskUsage;
		
		public DiskWatchTask() {
			diskUsageThreshold = settings.getInt("disk_usage_warning");
		}
		
		@Override
		public void run() {
			
			if(diskUsageThreshold > 0) {
				int diskUsage = (int) (((float) systemDiskInfo.usedDiskSize / (float) systemDiskInfo.totalDiskSize) * 100);
//				logger.debug("check >> {} / {}", diskUsage, diskUsageThreshold);
				if(diskUsage >= diskUsageThreshold) {
					//동일한 usage는 1시간 이후에 재 알림한다. 동일하지 않으면 바로 리포팅. 
					if(lastDiskUsage != diskUsage || System.currentTimeMillis() - lastReportTime >= 60 * 60 * 1000) {
						NotificationService notificationService = ServiceManager.getInstance().getService(NotificationService.class);
						notificationService.sendNotification(new DiskUsageNotification(diskUsage));
						lastReportTime = System.currentTimeMillis();
					}
				}
				lastDiskUsage = diskUsage;
			}
		}
	}
	
	
	@Override
	protected boolean doStart() throws FastcatSearchException {

		handler = SystemInfoHandler.getInstance();
		logger.info("isCpuInfoSupported = {}", isJvmCpuInfoSupported() || isSystemCpuInfoSupported());
		logger.info("isLoadAvgInfoSupported = {}", isLoadAvgInfoSupported());

		timer = new Timer("SystemWatchServiceTimer", true);
		
		timer.schedule(new SystemInfoCheckTask(), START_DELAY, INFO_CHECK_PERIOD);
		timer.schedule(new DiskWatchTask(), START_DELAY, DISK_CHECK_PERIOD);
		return true;
	}

	@Override
	protected boolean doStop() throws FastcatSearchException {
		timer.cancel();
		timer = null;
		return true;
	}

	@Override
	protected boolean doClose() throws FastcatSearchException {
		return true;
	}
}
