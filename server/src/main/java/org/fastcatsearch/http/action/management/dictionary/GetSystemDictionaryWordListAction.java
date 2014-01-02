package org.fastcatsearch.http.action.management.dictionary;

import java.io.Writer;
import java.util.List;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.analysis.AnalysisPlugin;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.DictionarySetting;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value="/management/dictionary/system", authority=ActionAuthority.Dictionary, authorityLevel=ActionAuthorityLevel.READABLE)
public class GetSystemDictionaryWordListAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		String pluginId = request.getParameter("pluginId");
		String search = request.getParameter("search");
		
		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		Plugin plugin = pluginService.getPlugin(pluginId);
		AnalysisPlugin analysisPlugin = (AnalysisPlugin) plugin;
		
		AnalysisPluginSetting analysisPluginSetting = (AnalysisPluginSetting) plugin.getPluginSetting();
		List<DictionarySetting> dictionaryList = analysisPluginSetting.getDictionarySettingList();
		
		CharVector token = new CharVector(search);
		List<?> list = analysisPlugin.getDictionary().find(token);
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		
		resultWriter.object().key("list").array();
		
		if(list != null){
			for(Object obj : list){
				resultWriter.value(obj.toString());
			}
		}
		
		resultWriter.endArray();
		
		resultWriter.endObject();
		resultWriter.done();
			
	}

}
