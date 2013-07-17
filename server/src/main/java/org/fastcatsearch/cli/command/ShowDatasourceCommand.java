package org.fastcatsearch.cli.command;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.cli.CommandException;
import org.fastcatsearch.cli.CommandResult;
import org.fastcatsearch.cli.ConsoleSessionContext;
import org.fastcatsearch.cli.command.exception.CollectionNotDefinedException;
import org.fastcatsearch.cli.command.exception.CollectionNotFoundException;
import org.fastcatsearch.db.DBContext;
import org.fastcatsearch.db.DBService;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.SingleSourceConfig;
import org.fastcatsearch.service.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShowDatasourceCommand extends CollectionExtractCommand {

	private static Logger logger = LoggerFactory.getLogger(ShowDatasourceCommand.class);

	private String header[] = new String[] { "property name", "property value" };
	private String webHeader[] = new String[] { "row", "link", "title", "cate", "encoding" };

	private ArrayList<Object[]> data = new ArrayList<Object[]>();

	@Override
	public boolean isCommand(String[] cmd) {
		return isCommand(CMD_SHOW_DATASOURCE, cmd);
	}

	@Override
	public CommandResult doCommand(String[] cmd, ConsoleSessionContext context) throws IOException, CommandException {
		String collectionId="";

		try {
			collectionId = extractCollection(context);
			checkCollectionExists(collectionId);
		} catch (CollectionNotDefinedException e) {
			return new CommandResult(
					"collection is not define\r\nuse like this\r\nuse collection collectionName;\r\nshow schema;",
					CommandResult.Status.SUCCESS);
		} catch (CollectionNotFoundException e) {
			return new CommandResult("collection " + collectionId + " is not exists", CommandResult.Status.SUCCESS);
		}

		if (cmd.length != 2)
			return new CommandResult("invalid command", CommandResult.Status.SUCCESS);

		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		CollectionContext collectionContext = irService.collectionContext(collectionId);
		SingleSourceConfig dataSourceConfig = null;//collectionContext.dataSourceConfig();

		if (dataSourceConfig == null)
			return new CommandResult("error invalid DataSource [" + collectionId + "] schema data",
					CommandResult.Status.SUCCESS);

//		logger.debug("ReaderType {}", dataSourceConfig.getReaderType());

//		if (getDatasource(dataSourceConfig, collectionId)) {
//			if (dataSourceConfig.sourceType.equals("WEB") == false)
//				return new CommandResult(printData(data, header), CommandResult.Status.SUCCESS);
//			else
//				return new CommandResult(printData(data, webHeader), CommandResult.Status.SUCCESS);
//		} else
			// 컬렉션 정보를 가져올때 실패 했을 경우
			return new CommandResult("error loading collection [" + collectionId + "] schema data",
					CommandResult.Status.SUCCESS);

	}

	private boolean getDatasource(SingleSourceConfig ds, String collection) {
		try {
//			if (ds.sourceType.equals("FILE")) {
//				addRecord(data, "fullFilePath", ds.fullFilePath);
//				addRecord(data, "incFilePath", ds.incFilePath);
//				addRecord(data, "fileDocParser", ds.fileDocParser);
//				addRecord(data, "fileEncoding", ds.fileEncoding);
//			} else if (ds.sourceType.equals("DB")) {
//				addRecord(data, "sourceFrom", ds.sourceFrom.equals("SINGLE") ? "SINGLE" : "MULTI");
//				addRecord(data, "driver", ds.driver);
//				addRecord(data, "url", ds.url);
//				addRecord(data, "user", ds.user);
//				addRecord(data, "password", ds.password);
//				addRecord(data, "fetchSize", ds.fetchSize + "");
//				addRecord(data, "bulkSize", ds.bulkSize + "");
//				addRecord(data, "beforeFullQuery", ds.beforeFullQuery);
//				addRecord(data, "beforeIncQuery", ds.beforeIncQuery);
//				addRecord(data, "fullQuery", ds.fullQuery);
//				addRecord(data, "incQuery", ds.incQuery);
//				addRecord(data, "afterFullQuery", ds.afterFullQuery);
//				addRecord(data, "afterIncQuery", ds.afterIncQuery);
//				addRecord(data, "fullBackupPath", ds.fullBackupPath);
//				addRecord(data, "incBackupPath", ds.incBackupPath);
//				addRecord(data, "backupFileEncoding", ds.backupFileEncoding);
//
//			} else if (ds.sourceType.equals("WEB")) {
//				DBContext dbContext = DBService.getInstance().getDBContext();
//				String selectSQL = "select * from from (select ROW_NUMBER() as rownum , " + collection
//						+ "WebPageSource) ";
//				try {
//					ResultSet rs = dbContext.selectSQL(selectSQL);
//					while (rs.next()) {
//						addRecord(data, rs.getString("rownum"), rs.getString("link"), rs.getString("title"),
//								rs.getString("cate"), rs.getString("encoding"));
//					}
//				} catch (Exception e) {
//					return false;
//				} finally {
//					dbContext.close();
//				}
//
//			} else if (ds.sourceType.equals("CUSTOM")) {
//				addRecord(data, "customReaderClass", ds.customReaderClass);
//				addRecord(data, "customConfigFile", ds.customConfigFile);
//			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void addRecord(List<Object[]> data, String propertyName, String propertyValue) {
		data.add(new Object[] { propertyName, propertyValue });
	}

	private void addRecord(List<Object[]> data, String id, String link, String title, String cate, String encoding) {
		data.add(new Object[] { id, link, title, cate, encoding });
	}

}
