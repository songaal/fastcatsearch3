package org.fastcatsearch.cli.command;

import org.fastcatsearch.cli.Command;
import org.fastcatsearch.cli.CommandResult;
import org.fastcatsearch.cli.ConsoleSessionContext;
import org.fastcatsearch.ir.config.IRSettings;

public class ListCollectionCommand extends Command {

	@Override
	public boolean isCommand(String[] cmd) {
		return isCommand(CMD_LIST_COLLECTION, cmd);
	}

	@Override
	public CommandResult doCommand(String[] cmd, ConsoleSessionContext context) {
		
		try {
			String collectinListStr = IRSettings.getConfig().getString("collection.list");
			String[] collectionNames = collectinListStr.split(",");
			
			int maxColumnLength = 0;
			for(int i=0;i<collectionNames.length;i++) {
				if(maxColumnLength < collectionNames[i].length()) {
					maxColumnLength = collectionNames[i].length();
				}
			}
			
			String ret = printData(collectionNames);
			
			return new CommandResult(ret, CommandResult.Status.SUCCESS);
		} catch (Exception e) {
			return new CommandResult(e.getMessage(),CommandResult.Status.FAIL);
		}
	}
}
