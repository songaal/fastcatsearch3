package org.fastcatsearch.cli.command;

import java.io.IOException;

import org.fastcatsearch.cli.Command;
import org.fastcatsearch.cli.CommandException;
import org.fastcatsearch.cli.CommandResult;
import org.fastcatsearch.cli.ConsoleSessionContext;
import org.fastcatsearch.cli.command.exception.CollectionNotDefinedException;
import org.fastcatsearch.cli.command.exception.CollectionNotFoundException;
import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.dao.IndexingSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScheduleIndexStartCommand extends CollectionExtractCommand {

	private static Logger logger = LoggerFactory.getLogger(ScheduleIndexStartCommand.class);

	@Override
	public boolean isCommand(String[] cmd) {
		logger.debug("input Command : {}", new Object[] { cmd });
		if (cmd.length == 3) {
			if (cmd[0].equals("schedule") && cmd[1].equals("start")) {
				if (cmd[2].equals("F") || cmd[2].equals("I"))
					return true;
			}
		}
		return false;
	}

	// 스켸쥴 설정
	@Override
	public CommandResult doCommand(String[] cmd, ConsoleSessionContext context) throws IOException, CommandException {

		logger.debug("input Command : {}", new Object[] { cmd });

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

		String indexType = cmd[2];
		if (indexType == null || indexType.trim().length() == 0
				|| (indexType.equals("F") == false && indexType.equals("I") == false))
			return new CommandResult("invalid Command\r\nschedule set [F|I] d:h:m\n\rd:day, h:hour, m:minute",
					CommandResult.Status.SUCCESS);

		DBService dbHandler = DBService.getInstance();
		IndexingSchedule indexSchedule = dbHandler.db().getDAO("IndexingSchedule");
		int affectCount = indexSchedule.updateStatus(collection, indexType, true);
		if (affectCount == 0)
			return new CommandResult("There is no Schedule [" + collection + "," + indexType + "] ",
					CommandResult.Status.SUCCESS);
		else
			return new CommandResult("Schedule [" + collection + "," + indexType + "] Started",
					CommandResult.Status.SUCCESS);
	}

}
