package org.fastcatsearch.http.action.management.dictionary;

import java.io.Writer;
import java.util.List;

import org.fastcatsearch.db.dao.MapDictionaryDAO;
import org.fastcatsearch.db.dao.SetDictionaryDAO;
import org.fastcatsearch.db.vo.MapDictionaryVO;
import org.fastcatsearch.db.vo.SetDictionaryVO;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.plugin.AnalysisPluginSetting;
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
		String keyword = request.getParameter("keyword");
		int start = request.getIntParameter("start");
		int length = request.getIntParameter("length");
		
		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		Plugin plugin = pluginService.getPlugin(pluginId);
		AnalysisPluginSetting analysisPluginSetting = (AnalysisPluginSetting) plugin.getPluginSetting();
		
		String daoId = analysisPluginSetting.getKey(dictionaryId);
		Object dao = pluginService.db().getDAO(daoId);
		int totalSize = 0;
		int filteredSize = 0;
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		resultWriter.object().key(dictionaryId).array();
		if(dao instanceof SetDictionaryDAO){
			SetDictionaryDAO setDictionary = (SetDictionaryDAO) dao;
			totalSize = setDictionary.selectCount();
			List<SetDictionaryVO> list = null;
			logger.debug("keyword >> {}", keyword);
			if(keyword != null){
				filteredSize = setDictionary.selectCountWithKeyword(keyword);
				list = setDictionary.selectPageWithKeyword(keyword, start, length);
			}else{
				filteredSize = totalSize;
				list = setDictionary.selectPage(start, length);
			}
			for(SetDictionaryVO vo : list){
				resultWriter.object().key("id").value(vo.id).key("word").value(vo.keyword).endObject();
			}
		}else if(dao instanceof MapDictionaryDAO) {
			MapDictionaryDAO mapDictionary = (MapDictionaryDAO) dao;
			totalSize = mapDictionary.selectCount();
			List<MapDictionaryVO> list = null;
			if(keyword != null){
				filteredSize = mapDictionary.selectCountWithKeyword(keyword);
				list = mapDictionary.selectPageWithKeyword(keyword, start, length);
			}else{
				filteredSize = totalSize;
				list = mapDictionary.selectPage(start, length);
			}
			for(MapDictionaryVO vo : list){
				resultWriter.object().key(vo.keyword).value(vo.value).endObject();
				resultWriter.object().key("id").value(vo.id).key("key").value(vo.keyword).key("word").value(vo.value).endObject();
			}
		}
		
		resultWriter.endArray();
		
		
		resultWriter.key("totalSize").value(totalSize).key("filteredSize").value(filteredSize)
		.endObject();
		
		resultWriter.done();
			
		
		
		
		
				
		
		
		
	}

}
