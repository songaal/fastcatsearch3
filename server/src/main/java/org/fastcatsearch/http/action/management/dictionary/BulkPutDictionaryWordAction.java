package org.fastcatsearch.http.action.management.dictionary;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.Writer;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.dao.DictionaryDAO;
import org.fastcatsearch.db.mapper.DictionaryMapper;
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

@ActionMapping(value="/management/dictionary/bulkPut", authority=ActionAuthority.Dictionary, authorityLevel=ActionAuthorityLevel.WRITABLE)
public class BulkPutDictionaryWordAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		String pluginId = request.getParameter("pluginId");
		String dictionaryId = request.getParameter("dictionaryId");
		String entryList = request.getParameter("entryList");

		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		Plugin plugin = pluginService.getPlugin(pluginId);
		AnalysisPlugin analysisPlugin = (AnalysisPlugin) plugin;
		
		DictionaryDAO dictionaryDAO = analysisPlugin.getDictionaryDAO(dictionaryId);
		
		BufferedReader reader = new BufferedReader(new StringReader(entryList));
		
		List<ColumnSetting> columnSettingList = dictionaryDAO.columnSettingList();

		int count = 0;
		String errorMessage = null;
		boolean isSuccess = false;
		if (columnSettingList != null && columnSettingList.size() > 0) {
				
			String[] columns = new String[columnSettingList.size()];
			
			String[] columnTypes = new String[columnSettingList.size()]; 
			
			for (int i = 0; i < columnSettingList.size(); i++) {
				ColumnSetting columnSetting = columnSettingList.get(i);
				columns[i] = columnSetting.getName();
				columnTypes[i] = columnSetting.getType();
			}
			
			MapperSession<DictionaryMapper> mapperSession = dictionaryDAO.openMapperSession();
			try{
				String line = null;
				while((line = reader.readLine()) != null){
					Object[] values = new Object[columnSettingList.size()];
					String[] entries = line.split("\t"); //필드값을 적게넣어서 length가 작을 수도 있다. 
					
					for (int i = 0; i < values.length; i++) {
						
						if(i < entries.length){
							if (columnTypes[i].startsWith("int") || columnTypes[i].startsWith("INT")) {
								int intValue = 0;
								try {
									intValue = Integer.parseInt(entries[i]);
								} catch (Exception ignore) {
								}
								values[i] = intValue;
							} else if (columnTypes[i].contains("char") || columnTypes[i].contains("CHAR")) {
								values[i] = entries[i];
							} else {
								values[i] = entries[i];
							}
							values[i] = entries[i];
							
							
//							logger.debug("put column {}", values[i]);
						}
					}
					count += dictionaryDAO.putRawEntry(mapperSession, columns, values);
				}
				isSuccess = true;
			}catch(Exception e){
				if(e.getCause() instanceof SQLIntegrityConstraintViolationException){
					errorMessage = "Duplicate word exist.";
				}else{
					errorMessage = e.getCause().toString();
				}
				//입력한 단어들을 롤백한다.
				mapperSession.rollback();
				isSuccess = false;
				count = 0;
			}finally {
				if(mapperSession != null){
					mapperSession.closeSession();
				}
			}
		}
		
		if(count > 0){
			analysisPlugin.dictionaryStatusDAO().updateUpdateTime(dictionaryId);
		}
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		resultWriter.object().key("success").value(isSuccess)
			.key("count").value(count);
		if(errorMessage != null){
			resultWriter.key("errorMessage").value(errorMessage);
		}
		resultWriter.endObject();
		resultWriter.done();

	}

}
