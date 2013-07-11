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

import java.util.List;

import org.fastcatsearch.cli.Command;
import org.fastcatsearch.cli.CommandResult;
import org.fastcatsearch.cli.ConsoleSessionContext;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionsConfig.Collection;
import org.fastcatsearch.service.ServiceManager;

/**
 * 
 * @author lupfeliz
 * 
 * print out collection's list
 *
 */
public class ListCollectionCommand extends Command {

	@Override
	public boolean isCommand(String[] cmd) {
		return isCommand(CMD_LIST_COLLECTION, cmd);
	}

	@Override
	public CommandResult doCommand(String[] cmd, ConsoleSessionContext context) {
		
		try {
//			String collectinListStr = IRSettings.getConfig().getString("collection.list");
//			String[] collectionNames = collectinListStr.split(",");
			
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			List<Collection> collectionList = irService.getCollectionList();
			
			String ret = printData(collectionList.toArray(new String[0]));
			
			return new CommandResult(ret, CommandResult.Status.SUCCESS);
		} catch (Exception e) {
			return new CommandResult(e.getMessage(),CommandResult.Status.FAIL);
		}
	}
}
