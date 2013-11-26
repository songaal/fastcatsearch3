package org.fastcatsearch.http.action.management.analysis;

import java.io.IOException;
import java.io.OutputStream;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.PluginSetting;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.JAXBConfigs;

@ActionMapping("/management/analysis/plugin-setting")
public class GetAnalysisPluginSettings extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		
		String pluginId = request.getParameter("pluginId");
		
		Plugin plugin = pluginService.getPlugin(pluginId);
		
		PluginSetting pluginSetting = plugin.getPluginSetting();
		
		if(pluginSetting instanceof AnalysisPluginSetting){
			
			OutputStream ostream = null;
			
			try {
			
				ostream = response.getOutputStream();
	
				JAXBConfigs.writeRawConfig(ostream, pluginSetting, AnalysisPluginSetting.class);
				
			} finally {
				
				if(ostream!=null) try {
					ostream.close();
				} catch (IOException e) { }
			}
		}
	}
}
