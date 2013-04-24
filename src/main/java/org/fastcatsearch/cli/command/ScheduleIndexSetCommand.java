package org.fastcatsearch.cli.command;

import java.io.IOException;

import org.fastcatsearch.cli.Command;
import org.fastcatsearch.cli.CommandException;
import org.fastcatsearch.cli.CommandResult;
import org.fastcatsearch.cli.ConsoleSessionContext;
import org.fastcatsearch.cli.command.exception.CollectionNotDefinedException;
import org.fastcatsearch.cli.command.exception.CollectionNotFoundException;
import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.object.IndexingSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScheduleIndexSetCommand extends CollectionExtractCommand {

	private static Logger logger = LoggerFactory.getLogger(ScheduleIndexSetCommand.class);

	@Override
	public boolean isCommand(String[] cmd) {
		logger.debug("input Command : {}", new Object[] { cmd });
		if (cmd.length == 4) {
			if (cmd[0].equals("schedule") && cmd[1].equals("set")) {
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

		// schedule set [f|i] peroid
		// 0 1 2 3
		String indexType = cmd[2];

		String period = cmd[3];
		if (period == null || period.trim().length() == 0)
			return new CommandResult("invalid Command\r\nschedule set [F|I] d:h:m\n\rd:day, h:hour, m:minute",
					CommandResult.Status.SUCCESS);

		String[] subPeriod = period.split(":");
		if (subPeriod.length != 3)
			return new CommandResult("invalid Command\r\nschedule set [F|I] d:h:m\n\rd:day, h:hour, m:minute",
					CommandResult.Status.SUCCESS);

		int iDay = 0;
		int iHour = 0;
		int iMinute = 0;
		try {
			iDay = Integer.parseInt(subPeriod[0].trim());
		} catch (Exception e) {
			iDay = 0;
		}

		try {
			iHour = Integer.parseInt(subPeriod[1].trim());
		} catch (Exception e) {
			iHour = 0;
		}

		try {
			iMinute = Integer.parseInt(subPeriod[2].trim());
		} catch (Exception e) {
			iMinute = 0;
		}

		IndexingSchedule is = DBService.getInstance().IndexingSchedule;
		is.deleteByType(collection, indexType);

		int iPeriod = iDay * 60 * 60 * 24 + iHour * 60 * 60 + iMinute * 60;
		if (iPeriod == 0)
			return new CommandResult("invalid Command\r\nschedule set [F|I] d:h:m\n\rd:day, h:hour, m:minute",
					CommandResult.Status.SUCCESS);

		try {
			SimpleDateFormat sdf = new SimpleDateFormat();
			Timestamp tsNow = new Timestamp(System.currentTimeMillis());
			int affectCount = is.updateOrInsert(collection, indexType, iPeriod, tsNow, false);
			return new CommandResult("update Complete", CommandResult.Status.SUCCESS);

		} catch (Exception e) {
			return new CommandResult("invalid Command\r\nschedule set [F|I] d:h:m\n\rd:day, h:hour, m:minute",
					CommandResult.Status.SUCCESS);
		}

	}

}
