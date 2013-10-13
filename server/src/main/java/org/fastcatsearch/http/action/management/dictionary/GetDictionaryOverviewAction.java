package org.fastcatsearch.http.action.management.dictionary;

import java.io.File;
import java.io.Writer;
import java.util.Date;
import java.util.List;

import org.fastcatsearch.db.dao.DictionaryDAO;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.util.Formatter;
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
				String name = dictionary.getName();
				String type = dictionary.getType();
				resultWriter.object()
				.key("id").value(dictionaryId)
				.key("name").value(name)
				.key("type").value(type);
				
				DictionaryDAO dictionaryDAO = analysisPlugin.getDictionaryDAO(dictionaryId);
				int entrySize = 0;
				if(dictionaryDAO != null){
					entrySize = dictionaryDAO.getCount(null, null);
				}
				resultWriter.key("entrySize").value(entrySize);
				
				
				File file = analysisPlugin.getDictionaryFile(dictionaryId);
				if(file.exists()){
					String lastModified = Formatter.formatDate(new Date(file.lastModified()));
					resultWriter.key("syncTime").value(lastModified);
				}else{
					resultWriter.key("syncTime").value("-");
				}
				resultWriter.endObject();
			}
		}
		resultWriter.endArray().endObject();
		
		resultWriter.done();
		
		
	}

}
