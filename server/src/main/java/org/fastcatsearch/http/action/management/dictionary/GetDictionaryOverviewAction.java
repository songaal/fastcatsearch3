package org.fastcatsearch.http.action.management.dictionary;

import java.io.Writer;
import java.util.List;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.job.state.TaskKey;
import org.fastcatsearch.job.state.TaskState;
import org.fastcatsearch.job.state.TaskStateService;
import org.fastcatsearch.plugin.AnalysisPluginSetting;
import org.fastcatsearch.plugin.AnalysisPluginSetting.Dictionary;
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
		AnalysisPluginSetting analysisPluginSetting = (AnalysisPluginSetting) plugin.getPluginSetting();
		List<Dictionary> dictionaryList = analysisPluginSetting.getDictionaryList();
		
		for(Dictionary dictionary : dictionaryList){
			String dictionaryId = dictionary.getId();
			String daoId = analysisPluginSetting.getKey(dictionaryId);
//			pluginService.db().getDAO(daoId, clazz);
			
		}
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		resultWriter.object().key("dictionaryOverview").array();
		
		if(taskStateList != null){
			for(TaskState taskState : taskStateList){
				TaskKey taskKey = taskState.taskKey();
				resultWriter.object()
				.key("isScheduled").value(taskKey.isScheduled())
				.key("summary").value(taskState.getSummary())
				.key("progress").value(taskState.getProgressRate())
				.key("startTime").value(taskState.getStartTime())
				.key("elapsed").value(taskState.getElapsedTime())
				.endObject();
			}
		}
		resultWriter.endArray().endObject();
		
		resultWriter.done();
		
		
	}

}
