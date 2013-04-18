package org.fastcatsearch.cli.command;

import java.io.IOException;

import org.fastcatsearch.cli.Command;
import org.fastcatsearch.cli.CommandException;
import org.fastcatsearch.cli.CommandResult;
import org.fastcatsearch.cli.ConsoleSessionContext;
import org.fastcatsearch.cli.command.exception.CollectionNotDefinedException;
import org.fastcatsearch.cli.command.exception.CollectionNotFoundException;
import org.fastcatsearch.db.DBHandler;
import org.fastcatsearch.db.object.IndexingSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScheduleIndexDeleteCommand extends CollectionExtractCommand {

	private static Logger logger = LoggerFactory.getLogger(ScheduleIndexDeleteCommand.class);

	@Override
	public boolean isCommand(String[] cmd) {
		logger.debug("input Command : {}", new Object[] { cmd });
		return isCommand(cmd, CMD_DELETE_SCHEDULE_INDEX);
	}

	// 스켸쥴 설정
	@Override
	public CommandResult doCommand(String[] cmd, ConsoleSessionContext context) throws IOException, CommandException {

		String collection = "";

		try {
			collection = extractCollection(context);
			checkCollectionExists(collection);
		} catch (CollectionNotDefinedException e) {
			return new CommandResult("collection is not define\r\nuse like this\r\nuse collection collectionName;",
					CommandResult.Status.SUCCESS);
		} catch (CollectionNotFoundException e) {
			return new CommandResult("collection " + collection + " is not exists", CommandResult.Status.SUCCESS);
		}

		int affectCount = DBHandler.getInstance().IndexingSchedule.delete(collection);
		if (affectCount > 0)
			return new CommandResult("Schedule [" + collection + "] deleted ", CommandResult.Status.SUCCESS);
		else
			return new CommandResult("there is no Schedule [" + collection + "] ", CommandResult.Status.SUCCESS);
	}

}
