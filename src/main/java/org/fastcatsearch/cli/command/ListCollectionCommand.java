package org.fastcatsearch.cli.command;

import java.util.Arrays;

import org.fastcatsearch.cli.Command;
import org.fastcatsearch.cli.CommandResult;
import org.fastcatsearch.cli.ListTableDecorator;
import org.fastcatsearch.service.IRService;

public class ListCollectionCommand extends Command {

	@Override
	public boolean isCommand(String[] cmd) {
		return isCommand(cmd, CMD_LIST_COLLECTION);
	}

	@Override
	public CommandResult doCommand() {
		
		try {
			
			String[] collectionNames = IRService.getInstance().getCollectionNames();
			
			int maxColumnLength = 0;
			for(int i=0;i<collectionNames.length;i++) {
				if(maxColumnLength < collectionNames[i].length()) {
					maxColumnLength = collectionNames[i].length();
				}
			}
			
			
			StringBuilder sb = new StringBuilder();
			ListTableDecorator ltd = new ListTableDecorator(sb,
					Arrays.asList(new Integer[] { (""+collectionNames.length).length(), 
					maxColumnLength }));
			
			ltd.printbar();
			
			for(int i=0;i<collectionNames.length;i++) {
				ltd.printData(0, i+1);
				ltd.printData(1, collectionNames[i]);
			}
			
			ltd.printbar();
			
			return new CommandResult(sb.toString(), CommandResult.Status.SUCCESS);
		} catch (Exception e) {
			return new CommandResult(e.getMessage(),CommandResult.Status.FAIL);
		}
	}
}
