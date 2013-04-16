package org.fastcatsearch.cli.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.cli.Command;
import org.fastcatsearch.cli.CommandException;
import org.fastcatsearch.cli.CommandResult;
import org.fastcatsearch.cli.ConsoleSessionContext;
import org.fastcatsearch.db.object.IndexingResult;
import org.fastcatsearch.ir.config.IRConfig;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.ir.util.Formatter;

public class ShowSettingCommand extends Command {

	private String[] header = new String[] { "property name", "property value" };
	private String[] basicProperties = new String[] { "server.port", "synonym.two-way", "dynamic.classpath" };
	private String[] collectionProperties = new String[] { "collection.list" };
	private String[] jobExecuteProperties = new String[] { "jobExecutor.core.poolsize", "jobExecutor.max.poolsize",
			"jobExecutor.keepAliveTime" };

	private String[] documentProperties = new String[] { "pk.term.interval", "pk.bucket.size",
			"document.read.buffer.size", "document.write.buffer.size", "document.block.size", "document.block.size",
			"document.compression.type" };

	private String[] indexingProperties = new String[] { "index.term.interval", "index.work.bucket.size",
			"index.work.memory", "index.work.check", "data.sequence.cycle" };

	private String[] searchProperties = new String[] { "search.highlightAndSummary" };

	private String[] segmentProperties = new String[] { "segment.separate.add.indexing", "segment.document.limit",
			"segment.revision.backup.size" };

	private String[] serverProperties = new String[] { "server.admin.path", "server.port", "serverlogs.dir" };

	private String[] dictionaryProperties = new String[] { "user.dic.path", "synonym.dic.path", "synonym.two-way",
			"stopword.dic.path", "specialCharacter.map.path" };

	// org.fastcat search는 사전을 지원하지 않는다.

	@Override
	public boolean isCommand(String[] cmd) {
		return isCommand(CMD_SHOW_SETTING, cmd);
	}

	@Override
	public CommandResult doCommand(String[] cmd, ConsoleSessionContext context) throws IOException, CommandException {
		if (cmd.length != 2) {
			return new CommandResult("invalid Command", CommandResult.Status.SUCCESS);
		} else {
			IRConfig irConfig = IRSettings.getConfig(true);
			ArrayList<Object[]> data = new ArrayList<Object[]>();
			
			for (String property : basicProperties)
				addRecord(data, property, irConfig.getString(property));
			
			for (String property : collectionProperties)
				addRecord(data, property, irConfig.getString(property));
			
			for (String property : jobExecuteProperties)
				addRecord(data, property, irConfig.getString(property));
			
			for (String property : documentProperties)
				addRecord(data, property, irConfig.getString(property));

			for (String property : indexingProperties)
				addRecord(data, property, irConfig.getString(property));
			
			for (String property : searchProperties)
				addRecord(data, property, irConfig.getString(property));
			
			for (String property : segmentProperties)
				addRecord(data, property, irConfig.getString(property));
			
			for (String property : serverProperties)
				addRecord(data, property, irConfig.getString(property));
			
			for (String property : dictionaryProperties)
				addRecord(data, property, irConfig.getString(property));
			
			return new CommandResult(printData(data, header), CommandResult.Status.SUCCESS);
		}
	}

	private void addRecord(List<Object[]> data, String key, String value) {
		data.add(new Object[] { key, value });
	}

}

