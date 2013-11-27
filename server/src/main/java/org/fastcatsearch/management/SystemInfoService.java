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
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;

public class SystemInfoService extends AbstractService {

	private static long PERIOD = 1000; // 1초마다 InfoCheckerTask를 수행한다.
	private static long START_DELAY = 1000;

	private ManagementInfoHandler handler;
	private Timer timer;

	private JvmCpuInfo jvmCpuInfoPerSecond = new JvmCpuInfo();
	private JvmMemoryInfo jvmMemoryInfoPerSecond = new JvmMemoryInfo();

	private static SystemInfoService instance;

	public static SystemInfoService getInstance() {
		return instance;
	}

	public SystemInfoService(Environment environment, Settings settings, ServiceManager serviceManager) {
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

	class SystemInfoCheckTask extends TimerTask {

		@Override
		public void run() {
			handler.checkJvmCpuInfo(jvmCpuInfoPerSecond);
			handler.checkJvmMemoryInfo(jvmMemoryInfoPerSecond);
		}
	}

	@Override
	protected boolean doStart() throws FastcatSearchException {

		handler = ManagementInfoHandler.getInstance();
		logger.info("isCpuInfoSupported = {}", isJvmCpuInfoSupported() || isSystemCpuInfoSupported());
		logger.info("isLoadAvgInfoSupported = {}", isLoadAvgInfoSupported());

		timer = new Timer(true);
		timer.schedule(new SystemInfoCheckTask(), START_DELAY, PERIOD);
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
