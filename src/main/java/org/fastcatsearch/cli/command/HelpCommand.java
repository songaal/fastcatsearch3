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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelpCommand extends Command {
	
	private static Logger logger = LoggerFactory.getLogger(HelpCommand.class);
	
	@Override
	public boolean isCommand(String[] cmd) {
		logger.debug("cmd : {}", new Object[]{cmd});
		return isCommand(CMD_HELP, cmd);
	}

	@Override
	public CommandResult doCommand(String[] cmd, ConsoleSessionContext context) throws IOException, CommandException {
		return new CommandResult("now contructing ^^ may be tomorrow~ ", CommandResult.Status.SUCCESS);
	}
}
