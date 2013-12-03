package org.fastcatsearch.job.management;

import java.io.IOException;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;

public class GetServerSystemInfoJob extends Job {

	private static final long serialVersionUID = -9023882122708815679L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		Node myNode = nodeService.getMyNode();
		
		ServerSystemInfo result = new ServerSystemInfo();
		
		result.osName = Environment.OS_NAME;
		result.osArch = System.getProperty("os.arch");
		result.userName = System.getProperty("user.name");
		result.fileEncoding = System.getProperty("file.encoding");
		result.javaHome = System.getProperty("java.home");
		result.javaVendor = System.getProperty("java.vendor");
		result.javaVersion = System.getProperty("java.version");
		result.javaClasspath = System.getProperty("java.class.path");
		result.homePath = environment.homeFile().getAbsolutePath();
		
		return new JobResult(result);
	}
	
	
	
	public static class ServerSystemInfo implements Streamable {
		public String osName;
		public String osArch;
		public String userName;
		public String fileEncoding;
		public String javaHome;
		public String javaVendor;
		public String javaVersion;
		public String javaClasspath;
		public String homePath;
		
		@Override
		public void readFrom(DataInput input) throws IOException {
			osName = input.readString();
			osArch = input.readString();
			userName = input.readString();
			fileEncoding = input.readString();
			javaHome = input.readString();
			javaVendor = input.readString();
			javaVersion = input.readString();
			javaClasspath = input.readString();
			homePath = input.readString();
		}

		@Override
		public void writeTo(DataOutput output) throws IOException {
			output.writeString(osName);
			output.writeString(osArch);
			output.writeString(userName);
			output.writeString(fileEncoding);
			output.writeString(javaHome);
			output.writeString(javaVendor);
			output.writeString(javaVersion);
			output.writeString(javaClasspath);
			output.writeString(homePath);
		}
		
		
		
		
	}

}
