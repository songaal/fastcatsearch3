package org.fastcatsearch.job.plugin;

import java.io.IOException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.service.ServiceManager;

/**
 * 해당 플러그인이 존재하는지 확인.
 * */
public class CheckPluginExistsJob extends Job implements Streamable {

	private static final long serialVersionUID = -2595613883866451780L;

	@Override
	public void readFrom(DataInput input) throws IOException {
		args = input.readString();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(getStringArgs());
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {
		String pluginId = getStringArgs();
		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		Plugin plugin = pluginService.getPlugin(pluginId);
		if(plugin != null){
			return new JobResult(true);
		}else{
			return new JobResult(false);
		}
	}

}
