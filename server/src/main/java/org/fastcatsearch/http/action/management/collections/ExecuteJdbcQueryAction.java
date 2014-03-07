package org.fastcatsearch.http.action.management.collections;

import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.JDBCSourceInfo;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.DynamicClassLoader;

@ActionMapping(value = "/management/collections/execute-jdbc-query", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class ExecuteJdbcQueryAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		Writer writer = response.getWriter();
		try {

			String jdbcSourceId = request.getParameter("jdbcSourceId");
			String query = request.getParameter("query");
			int length = request.getIntParameter("length", 10);
			if (length > 100) {
				length = 100;
			}

			IRService irService = ServiceManager.getInstance().getService(IRService.class);

			JDBCSourceInfo jdbcSourceInfo = irService.getJDBCSourceInfo(jdbcSourceId);

			if (jdbcSourceInfo == null) {
				writer.write("[Error] Unknown jdbc id : " + jdbcSourceId);
			} else if (query == null || query.trim().length() == 0) {
				writer.write("[Error] Query is  empty.");
			} else {
				// 드라이버 로딩.
				Class<?> clazz = DynamicClassLoader.loadClass(jdbcSourceInfo.getDriver());
				if (clazz == null) {
					writer.write("[Error] Cannot find driver : " + jdbcSourceInfo.getDriver());
				} else {

					Connection connection = DriverManager.getConnection(jdbcSourceInfo.getUrl(), jdbcSourceInfo.getUser(), jdbcSourceInfo.getPassword());
					Statement statement = connection.createStatement();

					statement.setMaxRows(length);

					boolean hasResultSet = statement.execute(query);

					if (hasResultSet) {
						ResultSet rs = statement.getResultSet();
						ResultSetMetaData metaData = rs.getMetaData();
						int columnCount = metaData.getColumnCount();
						String[] columnLabelList = new String[columnCount];
						String[] columnTypeList = new String[columnCount];
						for (int i = 0; i < columnCount; i++) {
							columnLabelList[i] = metaData.getColumnLabel(i + 1).toUpperCase();
							columnTypeList[i] = metaData.getColumnTypeName(i + 1).toLowerCase();
						}

						while (rs.next()) {
							writer.write("-- ");
							writer.write(Integer.toString(rs.getRow()));
							writer.write(" -----------\n");
							for (int i = 0; i < columnCount; i++) {
								writer.write(columnLabelList[i]);
								writer.write(" [");
								writer.write(columnTypeList[i]);
								writer.write("] : ");
								String value = rs.getString(i + 1);
								writer.write(value != null ? value : "[NULL]");
								writer.write("\n");
							}
							writer.write("\n");
						}

					} else {
						int updateCount = statement.getUpdateCount();
						writer.write("[INFO] " + updateCount + " rows affected.");
					}
				}

			}
		} catch (Exception e) {
			writer.write("[ERROR] ");
			e.printStackTrace(new PrintWriter(writer));
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
}