package org.fastcatsearch.http.action.management.dictionary;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.dic.PreResult;
import org.fastcatsearch.ir.dictionary.CustomDictionary;
import org.fastcatsearch.ir.dictionary.MapDictionary;
import org.fastcatsearch.ir.dictionary.SetDictionary;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.analysis.AnalysisPlugin;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.DictionarySetting;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

import java.io.Writer;
import java.util.List;

@ActionMapping(value = "/management/dictionary/system", authority = ActionAuthority.Dictionary, authorityLevel = ActionAuthorityLevel.READABLE)
public class GetSystemDictionaryWordListAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		String pluginId = request.getParameter("pluginId");
		String search = request.getParameter("search");

		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		Plugin plugin = pluginService.getPlugin(pluginId);
        if(!plugin.isLoaded()) {
            Writer writer = response.getWriter();
            ResponseWriter resultWriter = getDefaultResponseWriter(writer);

            resultWriter.object().key("list").array();
            resultWriter.endArray();

            resultWriter.endObject();
            resultWriter.done();
            return;
        }
		AnalysisPlugin<CharVector, PreResult<CharVector>> analysisPlugin = (AnalysisPlugin<CharVector, PreResult<CharVector>>) plugin;

		AnalysisPluginSetting analysisPluginSetting = (AnalysisPluginSetting) plugin.getPluginSetting();
		List<DictionarySetting> dictionaryList = analysisPluginSetting.getDictionarySettingList();

		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);

		resultWriter.object().key("list").array();

		CharVector token = new CharVector(search);
		token.setIgnoreCase();
		
		List<?> list = analysisPlugin.getDictionary().find(token);
		if (list != null) {
			for (Object obj : list) {
				resultWriter.value(obj.toString());
			}
		}
		
		for (DictionarySetting setting : dictionaryList) {
			String dictionaryId = setting.getId();
			String dictionaryName = setting.getName();
			Object dictionaryObj = analysisPlugin.getDictionary().getDictionaryMap().get(dictionaryId);
			if (dictionaryObj != null) {
//				logger.debug("#### {} > {} {} {} ", token,  dictionaryObj, dictionaryId, dictionaryName);
				if (dictionaryObj instanceof SetDictionary) {
					if (((SetDictionary) dictionaryObj).set().contains(token)) {
						resultWriter.value(dictionaryName + " : FOUND");
					}
				} else if (dictionaryObj instanceof MapDictionary) {
					CharVector[] value = ((MapDictionary) dictionaryObj).map().get(token);
					if (value != null) {
						StringBuffer sb = new StringBuffer();
						for (int i = 0; i < value.length; i++) {
							if (i > 0) {
								sb.append(", ");
							}
							sb.append(value[i]);
						}
						resultWriter.value(dictionaryName + " : " + sb.toString());
					}
				} else if (dictionaryObj instanceof CustomDictionary) {
					if(((CustomDictionary) dictionaryObj).getWordSet().contains(token)){
						resultWriter.value(dictionaryName + " : FOUND");
					}
				}
			}
		}

		PreResult<CharVector> preResult = analysisPlugin.getDictionary().findPreResult(token);
		if(preResult != null){
			CharVector[] value = preResult.getResult();
			if (value != null) {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < value.length; i++) {
					if (i > 0) {
						sb.append(", ");
					}
					sb.append(value[i]);
				}
				resultWriter.value("PRE-ANALYSIS  : " + sb.toString());
			}
			value = preResult.getAddition();
			if (value != null) {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < value.length; i++) {
					if (i > 0) {
						sb.append(", ");
					}
					sb.append(value[i]);
				}
				resultWriter.value("ADDITION : " + sb.toString());
			}
		}
		

		resultWriter.endArray();

		resultWriter.endObject();
		resultWriter.done();

	}

}
