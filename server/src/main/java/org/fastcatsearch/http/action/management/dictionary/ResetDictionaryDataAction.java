package org.fastcatsearch.http.action.management.dictionary;

import java.io.Writer;
import java.sql.SQLException;
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

@ActionMapping(value = "/management/dictionary/reset-data", authority = ActionAuthority.Dictionary, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class ResetDictionaryDataAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		/**
		 * 맵사전 재구축, http post 로 받아오는 데이터를 기반으로 사전 재구축 기존데이터는 삭제하고 입력.
		 * 
		 * TODO:만약 사전데이터가 아주 크다면 Stream 형식을 고려 하도록 한다. FIXME:맵사전에 한해 스키마가 바뀌지 않는다고 가정, 하드코딩 함.
		 */

		int status = 1;

		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);

		String pluginId = request.getParameter("pluginId", ""); // plugin
		String dictionaryId = request.getParameter("dictionaryId", ""); // brand, maker....
		String dataStr = request.getParameter("values", "");
		String[] dataArray = dataStr.split("\n");

		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		Plugin plugin = pluginService.getPlugin(pluginId);

		@SuppressWarnings("rawtypes")
		AnalysisPlugin analysisPlugin = (AnalysisPlugin) plugin;

		MapperSession<DictionaryMapper> mapperSession = null;

		try {

			DictionaryDAO dictionaryDAO = analysisPlugin.getDictionaryDAO(dictionaryId);
			List<ColumnSetting> columnSettingList = dictionaryDAO.columnSettingList();

			String[] columns = new String[columnSettingList.size()];

			for (int colInx = 0; colInx < columnSettingList.size(); colInx++) {
				ColumnSetting columnSetting = columnSettingList.get(colInx);
				columns[colInx] = columnSetting.getName().toUpperCase();
			}

			String tableName = dictionaryDAO.getTableName();

			mapperSession = dictionaryDAO.openMapperSession();

			mapperSession.getMapper().truncate(tableName);

			for (int recordInx = 0; recordInx < dataArray.length; recordInx++) {
				String recordStr = dataArray[recordInx];

				String[] record = recordStr.split("\t");
				String[] values = new String[columns.length];
				System.arraycopy(record, 0, values, 0, record.length);
				
				logger.trace("put entry data table:{}, columns:{}, values:{}", tableName, columns, values);

				mapperSession.getMapper().putEntry(tableName, columns, values);
			}

			mapperSession.commit();

			analysisPlugin.dictionaryStatusDAO().updateUpdateTime(dictionaryId);

			status = 0;

		} catch (SQLException e) {
			status = 1;
			logger.error("", e);
		} finally {
			if (mapperSession != null) {
				if (status == 0) {
					mapperSession.commit();
				} else {
					mapperSession.rollback();
				}
				mapperSession.closeSession();
			}
		}

		resultWriter.object().key("status").value(status).key("success").value(status == 0).endObject();

		resultWriter.done();
	}
}