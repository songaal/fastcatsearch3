package org.fastcatsearch.http.action.management.dictionary;

import org.fastcatsearch.db.dao.DictionaryDAO;
import org.fastcatsearch.db.dao.DictionaryStatusDAO;
import org.fastcatsearch.db.vo.DictionaryStatusVO;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.analysis.AnalysisPlugin;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.DictionarySetting;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

import java.io.File;
import java.io.Writer;
import java.util.Date;
import java.util.List;

@ActionMapping(value="/management/dictionary/overview", authority=ActionAuthority.Dictionary, authorityLevel=ActionAuthorityLevel.READABLE)
public class GetDictionaryOverviewAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		//TODO admin node인지 확인하고 해당 노드가 아니면 전달하여 받아온다.
		//해당노드이면 그대로 수행한다.
		
		String pluginId = request.getParameter("pluginId");
		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);

		Plugin plugin = pluginService.getPlugin(pluginId);
        if(!plugin.isLoaded()){
            Writer writer = response.getWriter();
            ResponseWriter resultWriter = getDefaultResponseWriter(writer);
            resultWriter.object().key("overview").array();
            resultWriter.endArray().endObject();
            resultWriter.done();
            return;
        }
		AnalysisPlugin analysisPlugin = (AnalysisPlugin) plugin;
		
		AnalysisPluginSetting analysisPluginSetting = (AnalysisPluginSetting) plugin.getPluginSetting();
		List<DictionarySetting> dictionaryList = analysisPluginSetting.getDictionarySettingList();
		
		DictionaryStatusDAO dictionaryStatusDAO = analysisPlugin.dictionaryStatusDAO();
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		resultWriter.object().key("overview").array();
		if(dictionaryList != null){
			for(DictionarySetting dictionary : dictionaryList){
				String dictionaryId = dictionary.getId();
				String name = dictionary.getName();
				DictionarySetting.Type type = dictionary.getType();
				String tokenType = dictionary.getTokenType();
				if(tokenType == null || tokenType.length() == 0){
					tokenType = "NONE";
				}
				String ignoreCase = dictionary.isIgnoreCase() ? "Y" : "N";
				resultWriter.object()
				.key("id").value(dictionaryId)
				.key("name").value(name)
				.key("type").value(type)
				.key("tokenType").value(tokenType)
				.key("ignoreCase").value(ignoreCase);
				int entrySize = 0;
				String updateTime = null;
				int applyEntrySize = 0;
				String applyTime = null;

				if(type == DictionarySetting.Type.SYSTEM){
					entrySize = analysisPlugin.getDictionary().size();
					applyEntrySize = entrySize;
					updateTime = "-";
					applyTime = "-";
				}else{
					DictionaryDAO dictionaryDAO = analysisPlugin.getDictionaryDAO(dictionaryId);
					if(dictionaryDAO != null){
						entrySize = dictionaryDAO.getCount(null, null);
					}
					DictionaryStatusVO dictionaryStatusVO = dictionaryStatusDAO.getEntry(dictionaryId);
					applyEntrySize = dictionaryStatusVO.applyEntrySize;
					if(dictionaryStatusVO.updateTime.getTime() == 0){
						updateTime = "-";
					}else{
						updateTime = Formatter.formatDateEndsMinute(new Date(dictionaryStatusVO.updateTime.getTime()));
					}
					if(dictionaryStatusVO.applyTime.getTime() == 0){
						applyTime = "-";
					}else{
						applyTime = Formatter.formatDateEndsMinute(new Date(dictionaryStatusVO.applyTime.getTime()));
					}
				}
				
				resultWriter.key("entrySize").value(entrySize);
				resultWriter.key("updateTime").value(updateTime);
				resultWriter.key("applyEntrySize").value(applyEntrySize);
				resultWriter.key("applyTime").value(applyTime);
				
				//file 시간은 applyTime 와 중복되므로 향후 어떻게 할지 고려필요.
				File file = analysisPlugin.getDictionaryFile(dictionaryId);
				if(file.exists()){
					String lastModified = Formatter.formatDate(new Date(file.lastModified()));
					resultWriter.key("fileTime").value(lastModified);
				}else{
					resultWriter.key("fileTime").value("-");
				}
				resultWriter.endObject();
			}
		}
		resultWriter.endArray().endObject();
		
		resultWriter.done();
		
		
	}

}
