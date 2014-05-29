package org.fastcatsearch.http.action.management.common;

import java.io.Writer;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.management.JvmCpuInfo;
import org.fastcatsearch.management.JvmMemoryInfo;
import org.fastcatsearch.management.SystemWatchService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping("/management/common/system-usage")
public class GetSystemUsageAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		SystemWatchService managementInfoService = ServiceManager.getInstance().getService(SystemWatchService.class);
		
		JvmCpuInfo jvmCpuInfo = managementInfoService.getJvmCpuInfo();
		
		JvmMemoryInfo jvmMemoryInfo = managementInfoService.getJvmMemoryInfo();
		
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		resultWriter.object()
			.key("cpu")
				.object()
				.key("jvm").value(jvmCpuInfo.jvmCpuUse)
				.key("system").value(jvmCpuInfo.systemCpuUse)
				.key("loadAvg").value(jvmCpuInfo.systemLoadAverage)
				.endObject()
			.key("memory")
				.object()
				.key("max").value(jvmMemoryInfo.maxHeapMemory + jvmMemoryInfo.maxNonHeapMemory)
				.key("used").value(jvmMemoryInfo.usedHeapMemory + jvmMemoryInfo.usedNonHeapMemory)
				.endObject()
		.endObject();
		
		resultWriter.done();
		
		
	}

}
