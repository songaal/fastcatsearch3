package org.fastcatsearch.http.action.management.dictionary;

import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.db.dao.AbstractDictionaryDAO;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.plugin.AnalysisPlugin;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping("/management/dictionary/list")
public class DictionaryWordListAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		String pluginId = request.getParameter("pluginId");
		String dictionaryId = request.getParameter("dictionaryId");
		String search = request.getParameter("search");
		int start = request.getIntParameter("start");
		int length = request.getIntParameter("length");
		boolean searchAll = request.getBooleanParameter("searchAll", true);
		
		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		Plugin plugin = pluginService.getPlugin(pluginId);
		AnalysisPlugin analysisPlugin = (AnalysisPlugin) plugin;
		
		AbstractDictionaryDAO dictionaryDAO = analysisPlugin.getDictionaryDAO(dictionaryId);
		
		int totalSize = dictionaryDAO.getCount(null, searchAll);
		int filteredSize = dictionaryDAO.getCount(search, searchAll);
		
		List<Map<String, Object>> list = dictionaryDAO.getEntryList(start, start + length, search, searchAll);
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		resultWriter.object().key(dictionaryId).array();
		
		for(Map<String, Object> vo : list){
			resultWriter.object()
				.key("id").value(vo.get("id")).key("keyword").value(vo.get("keyword"));
			if(dictionaryDAO.valueFieldList() != null && dictionaryDAO.valueFieldList().length > 0){
				for(int i = 0 ;i < dictionaryDAO.valueFieldList().length; i++){
					String field = dictionaryDAO.valueFieldList()[i];
					resultWriter.key(field).value(vo.get(field));
				}
			}
				
			resultWriter.endObject();
		}
		
		resultWriter.endArray();
		
		resultWriter.key("totalSize").value(totalSize).key("filteredSize").value(filteredSize)
		.endObject();
		
		resultWriter.done();
			
	}

}
