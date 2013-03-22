package org.fastcatsearch.cli.command;

import org.fastcatsearch.cli.Command;
import org.fastcatsearch.cli.CommandResult;

public class StartIndexCommandIncIndex extends Command {

	@Override
	public boolean isCommand(String[] cmd) {
		return isCommand(cmd,CMD_START_INCINDEX);
	}

	@Override
	public CommandResult doCommand(String[] cmd) {
		return null;
	}

}
