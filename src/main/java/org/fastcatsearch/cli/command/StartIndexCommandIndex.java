/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */
package org.fastcatsearch.cli.command;

import java.io.IOException;

import org.fastcatsearch.cli.Command;
import org.fastcatsearch.cli.CommandException;
import org.fastcatsearch.cli.CommandResult;
import org.fastcatsearch.cli.ConsoleSessionContext;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.job.FullIndexJob;
import org.fastcatsearch.job.IncIndexJob;
import org.fastcatsearch.job.Job;

/**
 * 
 * @author lupfeliz
 *
 */
public class StartIndexCommandIndex extends Command {
	
	@Override
	public boolean isCommand(String[] cmd) {
		return isCommand(CMD_START_INDEX,cmd);
	}

	@Override
	public CommandResult doCommand(String[] cmd, ConsoleSessionContext context) throws CommandException {
		
		String collection = null;
		boolean isIncremental = false;
		
		String msg = null;
		
		if(cmd.length == 2) {
			collection = (String)context.getAttribute(SESSION_KEY_USING_COLLECTION);
			isIncremental = false;
			
		} else if(cmd.length == 3 ) {
			
			collection = (String)context.getAttribute(SESSION_KEY_USING_COLLECTION);
			isIncremental = "inc".equalsIgnoreCase(cmd[2]);
			
		} else if(cmd.length == 4) {
			collection = cmd[2];
			isIncremental = "inc".equalsIgnoreCase(cmd[3]);
		}
		Job job = null; 
		
		if(isIncremental) {
			job = new IncIndexJob();
			msg = "Increametal Indexing Job ["+collection+"] Executed..";
		} else {
			job = new FullIndexJob();
			msg = "Full Indexing Job ["+collection+"] Executed..";
		}
		
		if(collection == null) {
			throw new CommandException("Error : Collection Not Selected");
		}
		
		try {
			if(collection != null) {

				job.setArgs(new String[] { collection });

				JobService.getInstance().offer(job);
				
				String ret = printData(msg);

				return new CommandResult(ret, CommandResult.Status.SUCCESS);
			}
		} catch (IOException e) { 
		}
		return new CommandResult("", CommandResult.Status.FAIL);
	}
}
