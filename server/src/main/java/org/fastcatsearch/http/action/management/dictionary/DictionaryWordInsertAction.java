package org.fastcatsearch.http.action.management.dictionary;

import java.io.Writer;

import org.fastcatsearch.db.dao.AbstractDictionaryDAO;
import org.fastcatsearch.db.dao.BatchContext;
import org.fastcatsearch.db.dao.MapDictionaryDAO;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.plugin.AnalysisPlugin;
import org.fastcatsearch.plugin.AnalysisPluginSetting;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;
import org.json.JSONArray;
import org.json.JSONObject;

@ActionMapping("/management/dictionary/insert")
public class DictionaryWordInsertAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		String pluginId = request.getParameter("pluginId");
		String dictionaryId = request.getParameter("dictionaryId");
		String keyword = request.getParameter("keyword");
		
		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		Plugin plugin = pluginService.getPlugin(pluginId);
		AnalysisPlugin analysisPlugin = (AnalysisPlugin) plugin;
		
		AbstractDictionaryDAO dictionaryDAO = analysisPlugin.getDictionaryDAO(dictionaryId);
		
		if(dictionaryDAO.valueFieldList() != null && dictionaryDAO.valueFieldList().length > 0){
			if(dictionaryDAO.valueFieldList().length == 1){
				for(String valueFieldName : dictionaryDAO.valueFieldList()){
					String value = request.getParameter(valueFieldName);
					dictionaryDAO.putEntry(keyword, value);
				}
			}else{
				String[] values = new String[dictionaryDAO.valueFieldList().length];
				for(int i = 0 ;i < dictionaryDAO.valueFieldList().length; i++){
					values[i] = request.getParameter(dictionaryDAO.valueFieldList()[i]);
				}
				dictionaryDAO.putEntry(keyword, values);
			}
		}
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		resultWriter.object().key("success").value("true").endObject();
		resultWriter.done();
		
	}

}
