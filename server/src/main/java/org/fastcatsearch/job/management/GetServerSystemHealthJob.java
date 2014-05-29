package org.fastcatsearch.job.management;

import java.io.IOException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.management.JvmCpuInfo;
import org.fastcatsearch.management.JvmMemoryInfo;
import org.fastcatsearch.management.SystemDiskInfo;
import org.fastcatsearch.management.SystemWatchService;
import org.fastcatsearch.service.ServiceManager;

public class GetServerSystemHealthJob extends Job {

	private static final long serialVersionUID = -9023882122708815679L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		SystemWatchService systemInfoService = ServiceManager.getInstance().getService(SystemWatchService.class);
		JvmCpuInfo jvmCpuInfo = systemInfoService.getJvmCpuInfo();
		JvmMemoryInfo jvmMemoryInfo = systemInfoService.getJvmMemoryInfo();
		SystemDiskInfo systemDiskInfo = systemInfoService.getSystemDiskInfo();
		
		SystemHealthInfo result = new SystemHealthInfo();
		
		result.jvmCpuUse = jvmCpuInfo.jvmCpuUse;
		result.systemCpuUse = jvmCpuInfo.systemCpuUse;
		result.systemLoadAverage = jvmCpuInfo.systemLoadAverage;
		
		result.committedMemory = jvmMemoryInfo.committedHeapMemory + jvmMemoryInfo.committedNonHeapMemory;
		result.maxMemory = jvmMemoryInfo.maxHeapMemory + jvmMemoryInfo.maxNonHeapMemory;
		result.usedMemory = jvmMemoryInfo.usedHeapMemory + jvmMemoryInfo.usedNonHeapMemory;
		result.totalMemory = jvmMemoryInfo.totalPhysicalMemorySize;
		
		result.totalDiskSize = systemDiskInfo.totalDiskSize;
		result.usedDiskSize = systemDiskInfo.usedDiskSize;
		result.freeDiskSize = systemDiskInfo.freeDiskSize;
		return new JobResult(result);
	}
	
	
	
	public static class SystemHealthInfo implements Streamable {
		
		public int jvmCpuUse;
		public int systemCpuUse;
		public double systemLoadAverage;
		public int maxMemory;
		public int committedMemory;
		public int usedMemory;
		public int totalMemory;
		
		public int totalDiskSize;
		public int usedDiskSize;
		public int freeDiskSize;
		
		@Override
		public void readFrom(DataInput input) throws IOException {
			jvmCpuUse = input.readInt();
			systemCpuUse = input.readInt();
			systemLoadAverage = input.readDouble();
			maxMemory = input.readInt();
			committedMemory = input.readInt();
			usedMemory = input.readInt();
			totalMemory = input.readInt();
			totalDiskSize = input.readInt();
			usedDiskSize = input.readInt();
			freeDiskSize = input.readInt();
		}

		@Override
		public void writeTo(DataOutput output) throws IOException {
			output.writeInt(jvmCpuUse);
			output.writeInt(systemCpuUse);
			output.writeDouble(systemLoadAverage);
			output.writeInt(maxMemory);
			output.writeInt(committedMemory);
			output.writeInt(usedMemory);
			output.writeInt(totalMemory);
			output.writeInt(totalDiskSize);
			output.writeInt(usedDiskSize);
			output.writeInt(freeDiskSize);
		}
		
		
		
		
	}

}
