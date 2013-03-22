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

package org.fastcatsearch.cli;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Command {
	
	private static final Logger logger = LoggerFactory.getLogger(Command.class);
	
	protected static final String[] CMD_INFO_SYSTEM = new String[]{"sysinfo"}; //시스템 정보.
	protected static final String[] CMD_SHOW_DICTIONARY = new String[]{"show", "dictionary"};
	protected static final String[] CMD_SHOW_SETTING = new String[]{"show", "setting"}; //fastcat.conf 셋팅.
	
	protected static final String[] CMD_LIST_COLLECTION = new String[]{"list", "collection"};
	protected static final String[] CMD_INFO_COLLECTION = new String[]{"info"}; //컬렉션정보-색인크기, 위치등..
	
	protected static final String[] CMD_SHOW_SCHEMA = new String[]{"show", "schema"};
	protected static final String[] CMD_SHOW_DATASOURCE = new String[]{"show", "datasource"};
	
	protected static final String[] CMD_START_INDEX = new String[]{"start", "index" };
	protected static final String[] CMD_STATUS_INDEX = new String[]{"status", "index" };
	
	protected static final String[] CMD_SET_SCHEDULE_INDEX = new String[]{"set", "schedule"};
	
	abstract public boolean isCommand(String[] cmd);
	abstract public CommandResult doCommand(String[] cmd);
	
	protected boolean isCommand(String[] expected, String[] actual){
		logger.trace("compare {} : {}", new Object[] { Arrays.asList(expected).toString(), 
				Arrays.asList(actual).toString() });
		for (int i = 0; i < expected.length; i++) {
			if(!expected[i].equalsIgnoreCase(actual[i])){
				return false;
			}
		}
		
		return true;
	}
}
