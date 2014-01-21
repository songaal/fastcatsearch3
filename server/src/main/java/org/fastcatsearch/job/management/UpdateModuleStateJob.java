package org.fastcatsearch.job.management;

import java.io.IOException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;

public class UpdateModuleStateJob extends Job implements Streamable {
	
	private static final long serialVersionUID = -7054816123263156939L;
	private String action; 
	private String serviceClasses;
	
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	
	public String getServiceClasses() {
		return serviceClasses;
	}
	
	public void setServiceClasses(String serviceClasses) {
		this.serviceClasses = serviceClasses;
	}
	
	@Override
	public JobResult doRun() throws FastcatSearchException {
		try {
			ServiceManager serviceManager = ServiceManager.getInstance();
			AbstractService service = null;
			
			if(serviceClasses == null) { serviceClasses = ""; }
			
			String[] classNames = serviceClasses.split(",");
			for(String className : classNames) {
				@SuppressWarnings("rawtypes")
				Class cls = Class.forName(className.trim());
				service = serviceManager.getService(cls);
				
				if("stop".equals(action) || "restart".equals(action)) {
					service.stop();
				}
				
				
				if("start".equals(action) || "restart".equals(action)) {
					service.start();
				}
			}
			return new JobResult(true);
		} catch (Exception e) {
			logger.error("", e);
		}
		return new JobResult(false);
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		action = input.readString();
		serviceClasses = input.readString();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(action);
		output.writeString(serviceClasses);
	}

}
