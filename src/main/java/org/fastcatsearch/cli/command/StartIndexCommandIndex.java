package org.fastcatsearch.cli.command;

import java.io.IOException;
import java.util.Arrays;

import org.fastcatsearch.cli.Command;
import org.fastcatsearch.cli.CommandResult;
import org.fastcatsearch.cli.ListTableDecorator;
import org.fastcatsearch.control.JobController;
import org.fastcatsearch.job.FullIndexJob;
import org.fastcatsearch.job.IncIndexJob;
import org.fastcatsearch.job.Job;

public class StartIndexCommandIndex extends Command {
	
	@Override
	public boolean isCommand(String[] cmd) {
		return isCommand(CMD_START_INDEX,cmd);
	}

	@Override
	public CommandResult doCommand(String[] cmd) {
		
		
		String collection = null;
		boolean isFull = false;
		
		String msg = null;
		
		if(cmd.length == 3 ) {
			//
			isFull = "full".equalsIgnoreCase(cmd[2]);
			
		} else if(cmd.length == 4) {
			collection = cmd[2];
			isFull = "full".equalsIgnoreCase(cmd[3]);
		}
		Job job = null; 
		
		if(isFull) {
			job = new FullIndexJob();
			msg = "Full Indexing Job Executed..";
		} else {
			job = new IncIndexJob();
			msg = "Increametal Indexing Job Executed..";
		}
		
		try {
			if(collection != null) {


				job.setArgs(new String[] { collection });

				JobController.getInstance().offer(job);


				StringBuilder sb = new StringBuilder();
				ListTableDecorator ltd = new ListTableDecorator(sb, Arrays.asList(new Integer[] {
						msg.length()
				}));
				
				ltd.printbar();
				ltd.printData(0, msg);
				ltd.printbar();
				return new CommandResult(sb.toString(), CommandResult.Status.SUCCESS);
			}
		} catch (IOException e) { 
		}
		return new CommandResult("", CommandResult.Status.FAIL);
	}
}
