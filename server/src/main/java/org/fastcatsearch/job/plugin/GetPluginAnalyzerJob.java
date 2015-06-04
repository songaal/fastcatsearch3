package org.fastcatsearch.job.plugin;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.result.BasicStringResult;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.PluginSetting;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.Analyzer;
import org.fastcatsearch.service.ServiceManager;
import org.json.JSONException;
import org.json.JSONStringer;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class GetPluginAnalyzerJob extends Job implements Streamable {
	
	private static final long serialVersionUID = -4342583698931447970L;
	
	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		Collection<Plugin> pluginList = pluginService.getPlugins();
		
		JSONStringer stringer = new JSONStringer();
		try {
		
			stringer.object().key("pluginList").array();
			for(Plugin plugin : pluginList){
				PluginSetting pluginSetting = plugin.getPluginSetting();
				if(pluginSetting instanceof AnalysisPluginSetting){
					
					AnalysisPluginSetting setting = (AnalysisPluginSetting) pluginSetting;
					
					List<Analyzer> analyzerList = setting.getAnalyzerList();
					
					stringer.object()
					.key("id").value(pluginSetting.getId())
					.key("name").value(pluginSetting.getName())
					.key("version").value(pluginSetting.getVersion())
					.key("description").value(pluginSetting.getDescription())
					.key("className").value(pluginSetting.getClassName())
					.key("licenseStatus").value(plugin.getLicenseStatus())
					.key("analyzer").array();
					if(analyzerList != null) {
                        for (Analyzer analyzer : analyzerList) {
                            stringer.object()
                                    .key("id").value(analyzer.getId())
                                    .key("name").value(analyzer.getName())
                                    .endObject();
                        }
                    }
                    stringer.endArray().endObject();

				}
			}
			stringer.endArray().endObject();
			BasicStringResult result = new BasicStringResult();
			result.setResult(stringer.toString());
			
			return new JobResult(result);
			
			
		} catch (JSONException e) {
			logger.debug("error orrurs : {}",e.getMessage());
		} finally {
		}
		return new JobResult(false);
	}

	@Override
	public void readFrom(DataInput input) throws IOException { }

	@Override
	public void writeTo(DataOutput output) throws IOException { }
}
