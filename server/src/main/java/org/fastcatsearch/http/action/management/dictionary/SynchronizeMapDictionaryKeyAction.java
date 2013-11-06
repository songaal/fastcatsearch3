package org.fastcatsearch.http.action.management.dictionary;

import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.ibatis.session.SqlSession;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.dao.DictionaryDAO;
import org.fastcatsearch.db.mapper.DictionaryMapper;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.analysis.AnalysisPlugin;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping("/management/dictionary/sync-map-dictionary-key")
public class SynchronizeMapDictionaryKeyAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		/**
		 * 맵사전 재구축, 맵사전 재구축을 위해, 현재 입력되어 있는 맵사전을 두고 따로 임시 저장소를 마련하여, 새로운 맵사전
		 * 데이터를 만든다. 새로 만들어진 맵사전에 update를 통하여 데이터를 입력 포맷은 단어 개행문자 구분
		 * 
		 * TODO:만약 사전데이터가 아주 크다면 Stream 형식을 고려 하도록 한다. FIXME:맵사전에 한해 스키마가 바뀌지
		 * 않는다고 가정, 하드코딩 함.
		 */

		int status = 1;

		Random random = new Random();
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);

		String pluginId = request.getParameter("pluginId", ""); // plugin
		String dictionaryId = request.getParameter("dictionaryId", ""); // brand, maker....
		String dataStr = request.getParameter("values", "");
		String[] dataArray = dataStr.split(",");
		List<String> dataList = Arrays.asList(dataArray);

		String srcTableName = dictionaryId + "_dictionary";
		String tmpTableName = "TMP_TABLE_" + random.nextInt(10000);

		String queryCreate = "CREATE TABLE " + tmpTableName + " AS SELECT * FROM " + srcTableName + " WITH NO DATA";

		String querySelect = "SELECT KEYWORD,VALUE FROM " + srcTableName;

		String queryInsert = "INSERT INTO " + tmpTableName + " (ID,KEYWORD,VALUE) VALUES (?,?,?) ";

		String[] querySwap = new String[] { "RENAME TABLE ", srcTableName, " TO ", tmpTableName + "__" };

		String[] queryDrop = new String[] { "DROP TABLE ", tmpTableName + "__" };

		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		Plugin plugin = pluginService.getPlugin(pluginId);

		@SuppressWarnings("rawtypes")
		AnalysisPlugin analysisPlugin = (AnalysisPlugin) plugin;

		DictionaryDAO dictionaryDAO = analysisPlugin.getDictionaryDAO(dictionaryId);

		SqlSession sqlSession = null;
		Connection connection = null;
		try {
			MapperSession<DictionaryMapper> mapperSession = dictionaryDAO.openMapperSession();
			sqlSession = mapperSession.getSession();
			connection = sqlSession.getConnection();

			logger.debug("prepare for refresh");

			Set<String> dupCheckSet = new HashSet<String>();
			// tmp 생성, 입력
			PreparedStatement statement = null;
			try {
				statement = connection.prepareStatement(queryCreate);
				statement.executeUpdate();
			} finally {
				if (statement != null) {
					statement.close();
				}
			}

			try {
				statement = connection.prepareStatement(queryInsert);
				
				PreparedStatement statement2 = null;
				ResultSet resultSet = null;
				try {
					statement2 = connection.prepareStatement(querySelect);
					resultSet = statement2.executeQuery();
					int cnt = 0;

					// 우선 기존 데이터베이스 데이터 중 최신데이터에 존재하는 것만 업데이트
					while (resultSet.next()) {
						String keyword = resultSet.getString(1);
						String value = resultSet.getString(2);
						logger.trace("insert into {}.{}  keyword = {}, value={}", new Object[] { pluginId, srcTableName, keyword, value });
						if (!dupCheckSet.contains(keyword)) {
							if (dataList.contains(keyword)) {
								statement.setInt(1, cnt);
								statement.setString(2, keyword);
								statement.setString(3, value);
								statement.addBatch();
								dupCheckSet.add(keyword);
								cnt++;
							}
							if (cnt > 0 && cnt % 10000 == 0) {
								statement.executeBatch();
							}
						}
					}
				} finally {
					if (resultSet != null) {
						resultSet.close();
					}
					if (statement2 != null) {
						statement2.close();
					}
				}

				int cnt = 0;
				// 기존 데이터베이스에 없는 데이터만 모아서 업데이트
				for (String keyword : dataArray) {
					logger.trace("insert into {}.{}  keyword = {}", new Object[] { pluginId, srcTableName, keyword });
					if (!dupCheckSet.contains(keyword)) {
						statement.setInt(1, cnt);
						statement.setString(2, keyword);
						statement.setString(3, "");
						statement.addBatch();
						dupCheckSet.add(keyword);
						cnt++;
					}
					if (cnt > 0 && cnt % 10000 == 0) {
						statement.executeBatch();
					}
				}
				dupCheckSet.clear();

				logger.debug("total {}. {} {} values inserted..", pluginId, srcTableName, cnt);
				statement.executeBatch();
				statement.close();

				// 원본테이블 -> 임시테이블2 이름변경
				StringBuilder query = new StringBuilder();
				for (int inx = 0; inx < querySwap.length; inx++) {
					query.append(querySwap[inx]);
				}
				statement = connection.prepareStatement(query.toString());
				statement.executeUpdate();

			} finally {
				if (statement != null) {
					statement.close();
				}
			}

			// 임시테이블 -> 원본테이블 이름변경
			StringBuilder query = new StringBuilder();
			for (int inx = 0; inx < querySwap.length; inx++) {
				query.append((inx == 1 ? tmpTableName : (inx == 3 ? srcTableName : querySwap[inx])));
			}
			try {
				statement = connection.prepareStatement(query.toString());
				statement.executeUpdate();
			} finally {
				if (statement != null) {
					statement.close();
				}
			}
			// tmp 데이터 클린 (이름바뀐 원본)
			query.setLength(0);
			for (int inx = 0; inx < queryDrop.length; inx++) {
				query.append(queryDrop[inx]);
			}
			try {
				statement = connection.prepareStatement(query.toString());
				statement.executeUpdate();
			} finally {
				if (statement != null) {
					statement.close();
				}
			}

			// tmp 최초데이터 삭제
			query.setLength(0);
			for (int inx = 0; inx < queryDrop.length; inx++) {
				query.append(inx == 1 ? tmpTableName : queryDrop[inx]);
			}
			try {
				statement = connection.prepareStatement(query.toString());
				statement.executeUpdate();
			} finally {
				if (statement != null) {
					statement.close();
				}
			}

			logger.debug("table swap ok");

			// 성공메시지.
			logger.debug("MapDictionary value refresh ok");

			analysisPlugin.dictionaryStatusDAO().updateUpdateTime(dictionaryId);

			status = 0;

		} catch (SQLException e) {
			status = 1;
			logger.error("", e);
		} finally {

			//pooling에서 close가 필요한가?
//			if (connection != null) {
//				connection.close();
//			}

			if (sqlSession != null) {
				if(status == 0){
					sqlSession.commit();
				}else{
					sqlSession.rollback();
				}
				sqlSession.close();
			}

		}

		resultWriter.object().key("status").value(status).key("success").value(status == 0).endObject();

		resultWriter.done();
	}
}