package org.fastcatsearch.http.action.management.dictionary;

import java.io.Writer;
import java.util.List;

import org.fastcatsearch.db.dao.DictionaryDAO;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.plugin.analysis.AnalysisPlugin;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.DictionarySetting;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping("/management/dictionary/overview")
public class GetDictionaryOverviewAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		//TODO admin node인지 확인하고 해당 노드가 아니면 전달하여 받아온다.
		//해당노드이면 그대로 수행한다.
		
		String pluginId = request.getParameter("pluginId");
		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		Plugin plugin = pluginService.getPlugin(pluginId);
		AnalysisPlugin analysisPlugin = (AnalysisPlugin) plugin;
		
		AnalysisPluginSetting analysisPluginSetting = (AnalysisPluginSetting) plugin.getPluginSetting();
		List<DictionarySetting> dictionaryList = analysisPluginSetting.getDictionarySettingList();
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		resultWriter.object().key("overview").array();
		if(dictionaryList != null){
			for(DictionarySetting dictionary : dictionaryList){
				String dictionaryId = dictionary.getId();
				DictionaryDAO dictionaryDAO = analysisPlugin.getDictionaryDAO(dictionaryId);
				int entrySize = dictionaryDAO.getCount(null, null);
				resultWriter.object()
				.key("name").value(dictionaryId)
				.key("size").value(entrySize)
				//TODO status, sync time
				.endObject();
			}
		}
		resultWriter.endArray().endObject();
		
		resultWriter.done();
		
		
	}

}
