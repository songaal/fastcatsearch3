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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagementInfoHandler {
	private static Logger logger = LoggerFactory.getLogger(ManagementInfoHandler.class);
	private static ManagementInfoHandler instance = new ManagementInfoHandler();

	public static ManagementInfoHandler getInstance() {
		return instance;
	}

	private MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
	private RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
	private OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
	private com.sun.management.OperatingSystemMXBean sunOsMXBean;
	private static int MEGABITE_UNIT = 1024 * 1024;
	private boolean isSunVmLowVersion; // sun jvm이면서 1.6이하인지 여부. 여기에 해당하면 cpu사용률을 직접계산할 수 있다.
	private boolean isLoadAvgInfoSupported; // Load Average 지원여부. jvm vendor에 상관없이 1.6이상이면 표준MXBean에서 지원하는 스펙이다.
	private boolean isVmVersionHigh; // jvm버전이 1.7이상인지 여부. jvm cpu사용률과 system cpu사용률을 표준MXBean에서 지원한다.

	private long sunNanoBefore; // cpu사용률을 직접계산할때 필요한 변수. Previous 시스템 시간
	private long sunCpuBefore; // cpu사용률을 직접계산할때 필요한 변수. Previous cpu time. 나중에 이 값과 현재값과의 차이를 계산하여 cpu를 얼마나 사용했는지 계산하게됨.

	// 검색엔진 개발환경이 jvm1.5또는 1.6일때 1.6,1.7지원 메소드를 사용하면 컴파일이 안되며, 실제 운영환경에서도 exception이 발생하므로
	// java reflection method invocation을 사용한다.
	// JVM 1.7(21.0)에서만 지원하는 메서드 2개
	private Method getSystemCpuLoadMethod;
	private Method getProcessCpuLoadMethod;

	// JVM 1.6(20.0)에서만 지워하는 메서드
	private Method getSystemLoadAverageMethod;

	private boolean isWindows;
	private boolean isJvmCpuInfoSupported;
	private boolean isSystemCpuInfoSupported;
	private boolean isJvmMemoryInfoSupported = true; // 항상 지원.

	public boolean isJvmCpuInfoSupported() {
		return isJvmCpuInfoSupported;
	}

	public boolean isSystemCpuInfoSupported() {
		return isSystemCpuInfoSupported;
	}

	public boolean isLoadAvgInfoSupported() {
		return isLoadAvgInfoSupported && !isWindows;
	}

	public boolean isJvmMemoryInfoSupported() {
		return isJvmMemoryInfoSupported;
	}

	public static void main(String[] args) throws InterruptedException {
		ManagementInfoHandler h = ManagementInfoHandler.getInstance();
		logger.debug("isJvmCpuInfoSupported = " + h.isJvmCpuInfoSupported());
		logger.debug("isSystemCpuInfoSupported = " + h.isSystemCpuInfoSupported());
		logger.debug("isLoadAvgInfoSupported = " + h.isLoadAvgInfoSupported());
		logger.debug("isJvmMemoryInfoSupported = " + h.isJvmMemoryInfoSupported());
		logger.debug("");
		JvmCpuInfo jvmCpuInfo = new JvmCpuInfo();
		JvmMemoryInfo jvmMemoryInfo = new JvmMemoryInfo();
		while (true) {
			h.checkJvmCpuInfo(jvmCpuInfo);
			h.checkJvmMemoryInfo(jvmMemoryInfo);
			jvmCpuInfo.print();
			jvmMemoryInfo.print();
			Thread.sleep(1000);
		}
	}

	private ManagementInfoHandler() {
		String vmVendor = runtimeMXBean.getVmVendor();
		String vmVersionStr = runtimeMXBean.getVmVersion();
		isWindows = osMXBean.getName().startsWith("Windows");

		Pattern pat = Pattern.compile("\\d*(\\.?\\d*)");
		Matcher matcher = pat.matcher(vmVersionStr);
		if (matcher.find()) {
			vmVersionStr = matcher.group();
		}
		float vmVersion = Float.parseFloat(vmVersionStr);

		logger.debug("VmVendor() = " + vmVendor);
		logger.debug("VmVersion() = " + vmVersionStr);

		// OperatingSystemMXBean은 벤더와 버전에 제약이 있다.
		if ((vmVendor.startsWith("Oracle") || vmVendor.startsWith("Sun")) && vmVersion < 21.0f) {
			// sun MXBean에서는 getCpuTime을 제공하여 1.5과 1.6에서는 cpu사용률을 직접구현해야한다.
			isSunVmLowVersion = true;
			sunOsMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
			initCpuForSunLowVersion();
			isJvmCpuInfoSupported = true;
			// 1.7이상일 경우 표준 OperatingSystemMXBean을 사용한다.
		}

		// 1.7이상에서는 cpu사용률을 지원한다.
		// 1.6이상에서는 SystemLoadAverage를 지원한다.
		if (vmVersion >= 21.0f) {
			// jvm 1.7
			isVmVersionHigh = true;
			isLoadAvgInfoSupported = true;
			prepareJvm17Method();
			prepareJvm16Method();
		} else if (vmVersion >= 20.0f) {
			isLoadAvgInfoSupported = true;
			// 1.6 method 생성
			prepareJvm16Method();
		}
	}

	private void prepareJvm17Method() {
		try {
			getSystemCpuLoadMethod = osMXBean.getClass().getMethod("getSystemCpuLoad");
			getSystemCpuLoadMethod.setAccessible(true);
			logger.debug("OperatingSystemMXBean.getSystemCpuLoad() supported!");
			isSystemCpuInfoSupported = true;
		} catch (Exception e) {
			logger.debug("OperatingSystemMXBean.getProcessCpuLoad() NOT supported!");
		}
		try {
			getProcessCpuLoadMethod = osMXBean.getClass().getMethod("getProcessCpuLoad");
			getProcessCpuLoadMethod.setAccessible(true);
			isJvmCpuInfoSupported = true;
			logger.debug("OperatingSystemMXBean.getProcessCpuLoad() supported!");
		} catch (Exception e) {
			logger.debug("OperatingSystemMXBean.getProcessCpuLoad() NOT supported!");
		}
	}

	private void prepareJvm16Method() {
		try {
			getSystemLoadAverageMethod = osMXBean.getClass().getMethod("getSystemLoadAverage");
			getSystemLoadAverageMethod.setAccessible(true);
			isLoadAvgInfoSupported = true;
			logger.debug("OperatingSystemMXBean.getSystemLoadAverage() supported!");
		} catch (Exception e) {
			logger.debug("OperatingSystemMXBean.getSystemLoadAverage() NOT supported!");
		}
	}

	public void checkJvmMemoryInfo(JvmMemoryInfo jvmMemoryInfo) {
		jvmMemoryInfo.init((int) (memoryMXBean.getHeapMemoryUsage().getMax() / MEGABITE_UNIT), (int) (memoryMXBean.getHeapMemoryUsage()
				.getCommitted() / MEGABITE_UNIT), (int) (memoryMXBean.getHeapMemoryUsage().getUsed() / MEGABITE_UNIT), (int) (memoryMXBean
				.getNonHeapMemoryUsage().getMax() / MEGABITE_UNIT), (int) (memoryMXBean.getNonHeapMemoryUsage().getCommitted() / MEGABITE_UNIT),
				(int) (memoryMXBean.getNonHeapMemoryUsage().getUsed() / MEGABITE_UNIT));
	}

	public void checkJvmCpuInfo(JvmCpuInfo jvmCpuInfo) {
		/*
		 * 1. cpu사용률 생성
		 */
		if (isVmVersionHigh) {
			// 표준 mxbean사용.
			try {
//				getSystemCpuLoadMethod.setAccessible(true);
//				getProcessCpuLoadMethod.setAccessible(true);
				double cpuLoad = (Double) (getSystemCpuLoadMethod.invoke(osMXBean));
				double jvmLoad = (Double) (getProcessCpuLoadMethod.invoke(osMXBean));
				jvmCpuInfo.systemCpuUse = (int) (cpuLoad * 100);
				jvmCpuInfo.jvmCpuUse = (int) (jvmLoad * 100);
			} catch (Exception e) {
				jvmCpuInfo.systemCpuUse = 0;
				jvmCpuInfo.jvmCpuUse = 0;
				e.printStackTrace();
			}

		} else if (isSunVmLowVersion) {
			// 커스텀 cpu 사용률 생성.
			checkCpuForSunLowVersion(jvmCpuInfo);
		} else {
			// 지원안함.
			jvmCpuInfo.systemCpuUse = 0;
			jvmCpuInfo.jvmCpuUse = 0;
		}

		/*
		 * 2. LoadAvg 생성 LoadAvg는 벤더에 독립적이며 1.6이상 버전에만 의존한다.
		 */
		if (isLoadAvgInfoSupported) {
			// 로드AVG생성
			try {
				double loadAvg = (Double) (getSystemLoadAverageMethod.invoke(osMXBean));
				// logger.debug("osMXBean = "+osMXBean.getSystemLoadAverage());
				jvmCpuInfo.systemLoadAverage = loadAvg;
			} catch (Exception e) {
				jvmCpuInfo.systemLoadAverage = 0.0;
			}
		} else {
			// clear
			jvmCpuInfo.systemLoadAverage = 0.0;
		}

	}

	private void initCpuForSunLowVersion() {
		sunNanoBefore = System.nanoTime();
		sunCpuBefore = sunOsMXBean.getProcessCpuTime();
	}

	private void checkCpuForSunLowVersion(JvmCpuInfo jvmCpuInfo) {
		long sunCpuAfter = sunOsMXBean.getProcessCpuTime();
		long sunNanoAfter = System.nanoTime();

		int percent;
		if (sunNanoAfter > sunNanoBefore) {
			percent = (int) (((sunCpuAfter - sunCpuBefore) * 100L) / (sunNanoAfter - sunNanoBefore));
		} else {
			percent = 0;
		}
		// logger.debug("Cpu usage: "+percent+"%"+", "+(sunCpuAfter-sunCpuBefore)+" / "+(sunNanoAfter-sunNanoBefore));
		sunNanoBefore = System.nanoTime();
		sunCpuBefore = sunOsMXBean.getProcessCpuTime();

		jvmCpuInfo.systemCpuUse = 0;
		jvmCpuInfo.jvmCpuUse = percent;
	}

}
