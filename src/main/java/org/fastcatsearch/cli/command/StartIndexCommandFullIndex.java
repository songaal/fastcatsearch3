package org.fastcatsearch.cli.command;

import java.io.IOException;
import java.util.Arrays;

import org.fastcatsearch.cli.Command;
import org.fastcatsearch.cli.CommandResult;
import org.fastcatsearch.cli.ListTableDecorator;
import org.fastcatsearch.control.JobController;
import org.fastcatsearch.job.FullIndexJob;

public class StartIndexCommandFullIndex extends Command {
	
	@Override
	public boolean isCommand(String[] cmd) {
		return isCommand(cmd,CMD_START_FULLINDEX);
	}

	@Override
	public CommandResult doCommand(String[] cmd) {
		JobController.getInstance().offer(new FullIndexJob());
		
		String msg = "Full Index Job Scheduled..";
		
		StringBuilder sb = new StringBuilder();
		ListTableDecorator ltd = new ListTableDecorator(sb, Arrays.asList(new Integer[] {
				msg.length()
		}));
		
		try {
			ltd.printbar();
			ltd.printData(0, msg);
			ltd.printbar();
			
			return new CommandResult(sb.toString(), CommandResult.Status.SUCCESS);
		} catch (IOException e) { 
			return new CommandResult("", CommandResult.Status.FAIL);
		}
	}
}
