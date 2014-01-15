package org.fastcatsearch.job.plugin;

import java.io.IOException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.analysis.AnalysisPlugin;
import org.fastcatsearch.service.ServiceManager;

/**
 * 플러그인의 사전을 리로드 한다.
 * */
public class UpdateDictionaryJob extends Job implements Streamable {

	private static final long serialVersionUID = 7694355608917697387L;

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
		if (plugin != null && plugin instanceof AnalysisPlugin) {
			AnalysisPlugin analysisPlugin = (AnalysisPlugin) plugin;
			analysisPlugin.reloadDictionary();
			return new JobResult(true);
		} else {
			return new JobResult(false);
		}
	}

}
