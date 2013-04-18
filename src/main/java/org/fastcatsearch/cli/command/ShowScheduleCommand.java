package org.fastcatsearch.cli.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.cli.Command;
import org.fastcatsearch.cli.CommandException;
import org.fastcatsearch.cli.CommandResult;
import org.fastcatsearch.cli.ConsoleSessionContext;
import org.fastcatsearch.db.DBHandler;
import org.fastcatsearch.db.object.IndexingSchedule;

public class ShowScheduleCommand extends CollectionExtractCommand {

	private String[] header = new String[]{"Collection Name","Kind","Start Time", "Term", "Active"};
	private ArrayList<Object[]> data = new ArrayList<Object[]>();
	
	
	@Override
	public boolean isCommand(String[] cmd) {
		return isCommand(cmd, CMD_SHOW_SCHEDULE);
	}

	@Override
	public CommandResult doCommand(String[] cmd, ConsoleSessionContext context) throws IOException, CommandException {
		//collection이 정의되지 않았다면 넘긴다 바로.
		String collection = extractCollection(context);
		if ( collection == null || collection.trim().length() == 0 )
			return new CommandResult(
					"collection is not defines\r\nuse like this\r\nuse collection collectionName;\r\nshow schema;",
					CommandResult.Status.SUCCESS);
		boolean isExists = isCollectionExists(collection);
		
		if ( isExists == false )
			return new CommandResult("there is no collection [" + collection + "] in collectionList",
					CommandResult.Status.SUCCESS);
		
		if ( cmd.length != 2 )
			return new CommandResult("invald command",CommandResult.Status.SUCCESS);
		
		
		DBHandler dbHandler = DBHandler.getInstance();
		IndexingSchedule fullIndexSchedule = dbHandler.IndexingSchedule.select(collection, "F");
		IndexingSchedule  incIndexSchedule = dbHandler.IndexingSchedule.select(collection, "I"); 
		
		return new CommandResult("invald command",CommandResult.Status.SUCCESS);
		
	}

	
	private void addRecord(List<Object[]> data, String cn, String kind, String startTime, String term) {
		data.add(new Object[] { cn, kind, startTime, term });
	}
	
}
