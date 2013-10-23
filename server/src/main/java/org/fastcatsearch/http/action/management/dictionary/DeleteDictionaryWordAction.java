package org.fastcatsearch.http.action.management.dictionary;

import java.io.Writer;

import org.fastcatsearch.db.dao.DictionaryDAO;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.analysis.AnalysisPlugin;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping("/management/dictionary/delete")
public class DeleteDictionaryWordAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		String pluginId = request.getParameter("pluginId");
		String dictionaryId = request.getParameter("dictionaryId");
		String deleteIdList = request.getParameter("deleteIdList");

		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		Plugin plugin = pluginService.getPlugin(pluginId);
		AnalysisPlugin analysisPlugin = (AnalysisPlugin) plugin;

		DictionaryDAO dictionaryDAO = analysisPlugin.getDictionaryDAO(dictionaryId);
		int result = dictionaryDAO.deleteEntryList(deleteIdList);

		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		resultWriter.object().key("success").value(result > 0).key("result").value(result).endObject();
		resultWriter.done();

	}

}
