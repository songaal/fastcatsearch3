package org.fastcatsearch.http.action.management.analysis;

import java.io.Writer;
import java.util.Collection;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.PluginSetting;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping("/management/analysis/plugin-list")
public class GetAnalysisPluginList extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		Collection<Plugin> pluginList = pluginService.getPlugins();
		
		
		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		responseWriter.object().key("pluginList").array("plugin");
		for(Plugin plugin : pluginList){
			PluginSetting pluginSetting = plugin.getPluginSetting();
			if(pluginSetting instanceof AnalysisPluginSetting){
				responseWriter.object()
				.key("id").value(pluginSetting.getId())
				.key("name").value(pluginSetting.getName())
				.key("version").value(pluginSetting.getVersion())
				.key("description").value(pluginSetting.getDescription())
				.key("className").value(pluginSetting.getClassName())
				.endObject();
			}
		}
		responseWriter.endArray().endObject();
		responseWriter.done();
	}

}
