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
import java.util.Arrays;
import java.util.List;

import org.fastcatsearch.cli.Command;
import org.fastcatsearch.cli.CommandException;
import org.fastcatsearch.cli.CommandResult;
import org.fastcatsearch.cli.ConsoleSessionContext;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionsConfig.Collection;
import org.fastcatsearch.service.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author lupfeliz
 * 
 * store current using collection name
 *
 */
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
		if(cmd.length == 2) {
			//
			// Remove Using Collection Name From Session
			//
			context.setAttribute(SESSION_KEY_USING_COLLECTION, null);
		} else if(cmd.length >= 3) {
			String collectionId = cmd[2];
			logger.debug("using collection : {} -> {}", new Object[] { context.getAttribute(SESSION_KEY_USING_COLLECTION), collectionId });
			
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			List<Collection> collectionList = irService.getCollectionList();
			
			for(Collection collectionInfo : collectionList){
				if(collectionInfo.getId().equalsIgnoreCase(collectionId)) {
					msg = printData("Using collection "+collectionId);
					context.setAttribute(SESSION_KEY_USING_COLLECTION, collectionId);
					return new CommandResult(msg, CommandResult.Status.SUCCESS);
				}
			}
			
			msg = printData(new Object[] { "'"+collectionId+"' Not Found" });
			return new CommandResult(msg, CommandResult.Status.FAIL);
		}
		return new CommandResult("No result", CommandResult.Status.FAIL);
	}
}
