package org.fastcatsearch.http.action.management.dictionary;

import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.db.dao.DictionaryDAO;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.analysis.AnalysisPlugin;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.ColumnSetting;
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
		String searchColumns = request.getParameter("searchColumns");
		
		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		Plugin plugin = pluginService.getPlugin(pluginId);
		AnalysisPlugin analysisPlugin = (AnalysisPlugin) plugin;
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		
		int totalSize = 0;
		int filteredSize = 0;
		DictionaryDAO dictionaryDAO = analysisPlugin.getDictionaryDAO(dictionaryId);
		resultWriter.object().key(dictionaryId).array();
		
		String[] searchColumnList = null;
		if(searchColumns != null){
			searchColumnList = searchColumns.split(",");
		}
		if(dictionaryDAO != null){
			totalSize = dictionaryDAO.getCount(null, null);
			filteredSize = dictionaryDAO.getCount(search, searchColumnList);
			
			List<Map<String, Object>> list = dictionaryDAO.getEntryList(start, start + length, search, searchColumnList);
			
			List<ColumnSetting> columnSettingList = dictionaryDAO.columnSettingList();
			for(Map<String, Object> vo : list){
				resultWriter.object();
				if(columnSettingList != null){
					for(int i = 0 ;i < columnSettingList.size(); i++){
						ColumnSetting columnSetting = columnSettingList.get(i);
						String name = columnSetting.getName();
						resultWriter.key(name).value(vo.get(name));
					}
				}
					
				resultWriter.endObject();
			}
			
		}
		
		resultWriter.endArray();
		
		resultWriter.key("totalSize").value(totalSize).key("filteredSize").value(filteredSize)
		.endObject();
		resultWriter.done();
			
	}

}
