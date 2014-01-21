package org.fastcatsearch.job.management;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.result.BasicStringResult;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.json.JSONStringer;

public class GetModuleStateJob extends Job implements Streamable {

	private static final long serialVersionUID = -5814628836593851820L;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public JobResult doRun() throws FastcatSearchException {
		try {
			
			ServiceManager serviceManager = ServiceManager.getInstance();
			
			AbstractService service = null;
			Set<String> serviceNames = new TreeSet<String>();
			Map<String,String> serviceNameMap = new HashMap<String, String>();
			Map<String,Boolean> serviceStateMap = new HashMap<String, Boolean>();
			for(Class serviceClass : serviceManager.getServiceClasses()) {
				String[] fqdn = serviceClass.getName().split("[.]");
				String serviceName = fqdn[fqdn.length-1];
				serviceNames.add(serviceName);
				service = serviceManager.getService(serviceClass);
				serviceNameMap.put(serviceName, serviceClass.getName());
				serviceStateMap.put(serviceName, service.isRunning());
			}
			
			JSONStringer stringer = new JSONStringer();
			stringer.object().key("moduleState").array();
			for(String serviceName : serviceNames) {
				stringer.object()
					.key("serviceName").value(serviceName)
					.key("serviceClass").value(serviceNameMap.get(serviceName))
					.key("status").value(serviceStateMap.get(serviceName))
					.endObject();
			}
			
			stringer.endArray().endObject();
			
			BasicStringResult result = new BasicStringResult();
			result.setResult(stringer.toString());
			
			return new JobResult(result);
			
		} catch (Exception e) {
			logger.error("", e);
		}
		return new JobResult(null);
	}

	@Override
	public void readFrom(DataInput input) throws IOException { }

	@Override
	public void writeTo(DataOutput output) throws IOException { }
}
