package org.fastcatsearch.http.action.management.dictionary;

import java.io.Writer;

import org.fastcatsearch.db.dao.DictionaryDAO;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.analysis.AnalysisPlugin;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value="/management/dictionary/truncate", authority=ActionAuthority.Dictionary, authorityLevel=ActionAuthorityLevel.WRITABLE)
public class TruncateDictionaryAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		String pluginId = request.getParameter("pluginId");
		String dictionaryId = request.getParameter("dictionaryId");

		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		Plugin plugin = pluginService.getPlugin(pluginId);
		AnalysisPlugin analysisPlugin = (AnalysisPlugin) plugin;

		DictionaryDAO dictionaryDAO = analysisPlugin.getDictionaryDAO(dictionaryId);
		
		boolean isSuccess = false;
		String errorMessage = null;
		try{
			dictionaryDAO.truncate();
			isSuccess = true;
			analysisPlugin.dictionaryStatusDAO().updateUpdateTime(dictionaryId);
		}catch(Exception e){
			isSuccess = false;
			errorMessage = e.getCause().toString();
		}

		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		resultWriter.object().key("success").value(isSuccess);
		if(!isSuccess && errorMessage != null){
			resultWriter.key("errorMessage").value(errorMessage);
		}
		resultWriter.endObject();
		resultWriter.done();

	}

}
