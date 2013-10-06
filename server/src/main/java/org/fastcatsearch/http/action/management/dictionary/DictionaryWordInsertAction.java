package org.fastcatsearch.http.action.management.dictionary;

import java.io.Writer;

import org.fastcatsearch.db.dao.BatchContext;
import org.fastcatsearch.db.dao.MapDictionaryDAO;
import org.fastcatsearch.db.dao.SetDictionaryDAO;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
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
		String words = request.getParameter("wordList");
		JSONArray wordList = new JSONArray(words);
		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		Plugin plugin = pluginService.getPlugin(pluginId);
		AnalysisPluginSetting analysisPluginSetting = (AnalysisPluginSetting) plugin.getPluginSetting();
		
		String daoId = analysisPluginSetting.getKey(dictionaryId);
		Object dao = pluginService.db().getDAO(daoId);
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		resultWriter.object().key(dictionaryId).array();
		
		if(dao instanceof SetDictionaryDAO){
			SetDictionaryDAO setDictionary = (SetDictionaryDAO) dao;
			if(wordList.length() == 0){
				//ignore
			}else if(wordList.length() > 1){
				BatchContext batchContext = setDictionary.startInsertBatch();
				for(int i=0; i<wordList.length(); i++){
					int count = setDictionary.insertBatch(wordList.getString(i), batchContext);
					if(count == -1){
						break;
					}
				}
				
				setDictionary.endInsertBatch(batchContext);
				batchContext.close();
			}else{
				setDictionary.insert(wordList.getString(0));
			}
		}else if(dao instanceof MapDictionaryDAO) {
			MapDictionaryDAO mapDictionary = (MapDictionaryDAO) dao;
			if(wordList.length() == 0){
				//ignore
			}else if(wordList.length() > 1){
				BatchContext batchContext = mapDictionary.startInsertBatch();
				for(int i=0; i<wordList.length(); i++){
					JSONObject obj = wordList.getJSONObject(i);
					int count = mapDictionary.insertBatch(obj.getString("key"), obj.getString("value"), batchContext);
					if(count == -1){
						break;
					}
				}
				
				mapDictionary.endInsertBatch(batchContext);
				batchContext.close();
			}else{
				JSONObject obj = wordList.getJSONObject(0);
				mapDictionary.insert(obj.getString("key"), obj.getString("value"));
			}
		}
		resultWriter.endArray().endObject();
		
		resultWriter.done();
			
		
		
		
		
				
		
		
		
	}

}
