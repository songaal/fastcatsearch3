package org.fastcatsearch.http.action.management.dictionary;

import java.io.Writer;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

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
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.ColumnSetting;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value="/management/dictionary/update",authority=ActionAuthority.Dictionary, authorityLevel=ActionAuthorityLevel.WRITABLE)
public class UpdateDictionaryWordAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		String pluginId = request.getParameter("pluginId");
		String dictionaryId = request.getParameter("dictionaryId");

		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		Plugin plugin = pluginService.getPlugin(pluginId);
		AnalysisPlugin analysisPlugin = (AnalysisPlugin) plugin;

		DictionaryDAO dictionaryDAO = analysisPlugin.getDictionaryDAO(dictionaryId);

		List<ColumnSetting> columnSettingList = dictionaryDAO.columnSettingList();

		int count = 0;
		String errorMessage = null;
		
		List<String> columnNameList = new ArrayList<String>();
		List<Object> columnValueList = new ArrayList<Object>();
		
		if (columnSettingList != null && columnSettingList.size() > 0) {
			
			String idColumnValue = request.getParameter("ID");
			for (int i = 0; i < columnSettingList.size(); i++) {
				ColumnSetting columnSetting = columnSettingList.get(i);
				
				String name = columnSetting.getName().toUpperCase();
				String type = columnSetting.getType();
				String value = request.getParameter(name);

				if(value != null) {
					Object typedValue = getTypedValue(type, value);
					columnNameList.add(name);
					columnValueList.add(typedValue);
				}
			}
			
			logger.debug("id > {} , {}, {}", idColumnValue, columnNameList, columnValueList);
			
			if(idColumnValue != null){
				try{
					count = dictionaryDAO.updateEntry(idColumnValue, columnNameList.toArray(new String[0]), columnValueList.toArray());
				}catch(Exception e){
					if(e.getCause() instanceof SQLIntegrityConstraintViolationException){
						errorMessage = "Duplicate word exist.";
					}else{
						errorMessage = e.getCause().toString();
					}
				}
			}else{
				errorMessage = "Cannot find entry. id="+idColumnValue;
			}
		}

		
		if(count > 0){
			analysisPlugin.dictionaryStatusDAO().updateUpdateTime(dictionaryId);
		}
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		resultWriter.object().key("success").value(count > 0);
		if(errorMessage != null){
			resultWriter.key("errorMessage").value(errorMessage);
		}
		resultWriter.endObject();
		resultWriter.done();

	}

	
	private Object getTypedValue(String type, String value){
		if (type.startsWith("int") || type.startsWith("INT")) {
			try {
				return Integer.parseInt(value);
			} catch (Exception ignore) {
			}
		} else if (type.contains("char") || type.contains("CHAR")) {
			return value;
		}
		
		return value;
	}
}
