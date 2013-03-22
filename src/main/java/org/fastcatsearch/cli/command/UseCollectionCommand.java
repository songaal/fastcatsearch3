package org.fastcatsearch.cli.command;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.fastcatsearch.cli.Command;
import org.fastcatsearch.cli.CommandException;
import org.fastcatsearch.cli.CommandResult;
import org.fastcatsearch.cli.ConsoleSessionContext;
import org.fastcatsearch.ir.config.IRSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UseCollectionCommand extends Command {
	
	private static final Logger logger = LoggerFactory.getLogger(UseCollectionCommand.class);

	@Override
	public boolean isCommand(String[] cmd) {
		return isCommand(CMD_USE_COLLECTION, cmd);
	}

	@Override
	public CommandResult doCommand(String[] cmd, ConsoleSessionContext context)
			throws CommandException, IOException {
		String msg = null;
		if(cmd.length >= 3) {
			String collection = cmd[2];
			logger.debug("using collection : {} -> {}", new Object[] { context.getAttribute(SESSION_KEY), collection });
			context.setAttribute(SESSION_KEY, collection);
			
			String collectinListStr = IRSettings.getConfig().getString("collection.list");
			
			List<String>collectionNames  = Arrays.asList(collectinListStr.split(","));
			
			if(collectionNames.contains(collection)) {
				msg = printData(new Object[] { "Using collection "+collection });
				return new CommandResult(msg, CommandResult.Status.SUCCESS);
			}
			
			msg = printData(new Object[] { "'"+collection+"' Not Found" });
			return new CommandResult(msg, CommandResult.Status.FAIL);
			
			
		} else {
			throw new CommandException ("Syntax Error : Collection Not Select");
		}
	}
}
