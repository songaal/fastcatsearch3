package org.fastcatsearch.cli.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fastcatsearch.cli.Command;
import org.fastcatsearch.cli.CommandException;
import org.fastcatsearch.cli.CommandResult;
import org.fastcatsearch.cli.ConsoleSessionContext;
import org.fastcatsearch.ir.config.FieldSetting;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.ir.config.Schema;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.service.IRService;

public class ShowSchemaCommand extends CollectionExtractCommand {

	private static Logger logger = LoggerFactory.getLogger(ShowSchemaCommand.class);

	// 데이타 출력용 헤더
	private String[] header = new String[] { "Collection", "Field Name", "key", "Field Type", "Field Size", "Index",
			"Sort", "Sort Size", "Group", "Filter", "Store", "Normalize", "Column", "Virtual", "Modify", "TagRemove",
			"MultiValue" };

	private ArrayList<Object[]> data = new ArrayList<Object[]>();

	// 데이타를 쌓아 놓는 곳

	@Override
	public boolean isCommand(String[] cmd) {
		return isCommand(cmd, CMD_SHOW_SCHEMA);
	}

	@Override
	public CommandResult doCommand(String[] cmd, ConsoleSessionContext context) throws IOException, CommandException {

		// collection이 정의되지 않았다면 넘긴다 바로.
		String collection = "";

		try {
			collection = extractCollection(context);
		} catch (CollectionNotDefinedException e) {
			return new CommandResult(
					"collection is not define\r\nuse like this\r\nuse collection collectionName;\r\nshow schema;",
					CommandResult.Status.SUCCESS);
		}

		if (cmd.length != CMD_SHOW_SCHEMA.length)
			return new CommandResult("invalid command", CommandResult.Status.SUCCESS);

		try {
			checkCollectionExists(collection);
		} catch (CollectionNotFoundException e) {
			return new CommandResult("collection " + collection + " is not exists", CommandResult.Status.SUCCESS);
		}
		
		if (getSchema(collection))
			return new CommandResult(printData(data, header), CommandResult.Status.SUCCESS);
		else
			// 컬렉션 정보를 가져올때 실패 했을 경우
			return new CommandResult("error loading collection [" + collection + "] schema data",
					CommandResult.Status.SUCCESS);

	}

	private boolean getSchema(String collection) {

		Schema schema = null;
		try {
			schema = IRSettings.getSchema(collection, true);
			for (FieldSetting fs : schema.getFieldSettingList())
				getFieldData(fs, collection);
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	private boolean getFieldData(FieldSetting fs, String collection) {
		try {
			addRecord(data, collection, fs.name, (fs.primary ? "O" : ""), fs.type.name(), fs.size + "",
					getIndexTokenName(fs), (fs.sortSetting == null ? "" : "O"), (fs.sortSetting == null ? ""
							: (fs.sortSetting.sortSize > 0 ? fs.sortSetting.sortSize + "" : "0")),
					(fs.groupSetting == null ? "" : "O"), (fs.filterSetting == null ? "" : "O"),
					(fs.store == true ? "O" : ""), (fs.normalize == true ? "O" : ""), (fs.column == true ? "O" : ""),
					(fs.virtual == true ? "O" : ""), (fs.modify == true ? "O" : ""), (fs.tagRemove == true ? "O" : ""),
					(fs.multiValue == true ? "0" : ""));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private String getIndexTokenName(FieldSetting fs) {
		if (fs.indexSetting == null)
			return "";
		else
			return fs.indexSetting.handler;
	}

	private void addRecord(List<Object[]> data, String cn, String fName, String key, String fType, String fSize,
			String index, String sort, String sortSize, String Group, String filter, String store, String normalize,
			String column, String virtual, String modify, String tagRemove, String multiValue) {
		data.add(new Object[] { cn, fName, key, fType, fSize, index, sort, sortSize, Group, filter, store, normalize,
				column, virtual, modify, tagRemove, multiValue });
	}

	private void addEmptyRecord(List<Object[]> data) {
		data.add(new Object[] { "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" });
	}

	// if ((isCollectionExists(collection) == false) &&
	// (collection.equalsIgnoreCase(ALLCOLLECTION) == false) ) {
	// // 입력된 collection이 현재 컬렉션 리스트에 있거나, 아니면 전체 컬렉션을 지정하는게 아닐 경우 에러.
	// return new CommandResult("there is no collection [" + collection +
	// "] in collectionList",
	// CommandResult.Status.SUCCESS);
	// }
}
