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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fastcatsearch.cli.Command;
import org.fastcatsearch.cli.CommandException;
import org.fastcatsearch.cli.CommandResult;
import org.fastcatsearch.cli.ConsoleSessionContext;
import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.dao.IndexingResult;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.settings.IRSettings;

/**
 * 
 * @author lupfeliz
 *
 */
public class StatusIndexCommand extends Command {
	
	private static final String FULL_INDEXING = "Full Indexing";
	private static final String INC_INDEXING = "Inc Indexing";

	@Override
	public boolean isCommand(String[] cmd) {
		return isCommand(CMD_STATUS_INDEX, cmd);
	}

	@Override
	public CommandResult doCommand(String[] cmd, ConsoleSessionContext context)
			throws IOException, CommandException {
		DBService dbHandler = DBService.getInstance();
		String collectinListStr = IRSettings.getConfig().getString("collection.list");
		List<String>collectionNames  = Arrays.asList(collectinListStr.split(","));
		
		String msg = null;
		String collection = null;
		String type = null;
		boolean listAll = false;
		
		String[] header = new String[] { 
				"Collection",
				"Job Type",
				"Result",
				"Documents",
				"Time starts",
				"Time finished",
				"Duration"
		};
		List<Object[]> data = new ArrayList<Object[]>();
		
		if(cmd.length == 2) {
			//status index
			collection = (String)context.getAttribute(SESSION_KEY_USING_COLLECTION);
			if(collection == null) {
				listAll = true;
			}
			
		} else if(cmd.length == 3 ) {
			//status index ${collectionName}
			collection = cmd[2];
			
		} else if(cmd.length == 4) {
			//status index ${collectionName} ${full | inc}
			collection = cmd[2];
			type = cmd[3];
		}
		
		if(listAll) {
			for(String collectionName : collectionNames) {
				IndexingResult indexResult = null;
				
				indexResult = dbHandler.IndexingResult.select(collectionName, "F");
				if(indexResult != null) {
					addRecord(data, collectionName, FULL_INDEXING, indexResult);
				}
				indexResult = dbHandler.IndexingResult.select(collectionName, "I");
				if(indexResult != null) {
					addRecord(data, collectionName, INC_INDEXING, indexResult);
				}
			}
			msg = printData(data, header);
			return new CommandResult(msg, CommandResult.Status.SUCCESS);
		} else {
			if(collection == null) {
				throw new CommandException("Error : Collection Not Selected");
			}
			
			IndexingResult indexResult = null;
			
			if(type==null) {
	
				indexResult = dbHandler.IndexingResult.select(collection, "F");
				if(indexResult != null) {
					addRecord(data, collection, FULL_INDEXING, indexResult);
				}
				indexResult = dbHandler.IndexingResult.select(collection, "I");
				if(indexResult != null) {
					addRecord(data, collection, INC_INDEXING, indexResult);
				}
			} else if("full".equalsIgnoreCase(type)) {
				indexResult = dbHandler.IndexingResult.select(collection, "F");
				if(indexResult != null) {
					addRecord(data, collection, FULL_INDEXING, indexResult);
				}
				
			} else if("inc".equalsIgnoreCase(type)) {
				indexResult = dbHandler.IndexingResult.select(collection, "I");
				if(indexResult != null) {
					addRecord(data, collection, INC_INDEXING, indexResult);
				}
			}
			if(data.size() > 0) {
				msg = printData(data, header);
				return new CommandResult(msg, CommandResult.Status.SUCCESS);
			} else {
				throw new CommandException("Data Not Found");
			}
		}
	}
	
	private void addRecord(List<Object[]> data,String collectionName, String type, IndexingResult result ) {
		if(result != null) {
			data.add(new Object[] {
					collectionName,
					type,
					(result.status == IndexingResult.STATUS_SUCCESS?"Success":"") +
					(result.status == IndexingResult.STATUS_FAIL?"Fail":"") +
					(result.status == IndexingResult.STATUS_RUNNING?"Running":""),
					result.docSize,
					result.startTime,
					result.endTime,
					Formatter.getFormatTime((long)result.duration)
			});
		}
	}
}
