package org.fastcatsearch.cli.command;

import org.fastcatsearch.cli.Command;
import org.fastcatsearch.cli.CommandResult;
import org.fastcatsearch.service.IRService;

public class ListCollectionCommand extends Command {

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
			
			sb.append("+-");
			for(int i=0;i<maxColumnLength;i++) {
				sb.append("-");
			}
			sb.append("-+");
			
			for(int i=0;i<collectionNames.length;i++) {
				sb.append("| ");
				sb.append(String.format("%"+maxColumnLength+"s", collectionNames[i]));
				sb.append("| ");
			}
			
			sb.append("+-");
			for(int i=0;i<maxColumnLength;i++) {
				sb.append("-");
			}
			sb.append("-+");
			
			sb.append("+-");
			for(int i=0;i<maxColumnLength;i++) {
				sb.append("-");
			}
			sb.append("-+");
			
			return new CommandResult(sb.toString(), CommandResult.Status.SUCCESS);
		} catch (Exception e) {
			return new CommandResult(e.getMessage(),CommandResult.Status.FAIL);
		}
	}
}
