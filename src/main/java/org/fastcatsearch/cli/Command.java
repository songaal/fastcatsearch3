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

public abstract class Command {
	
	protected static final String[] CMD_INFO_SYSTEM = new String[]{"sysinfo"}; //시스템 정보.
	protected static final String[] CMD_SHOW_DICTIONARY = new String[]{"show", "dictionary"};
	protected static final String[] CMD_SHOW_SETTING = new String[]{"show", "setting"}; //fastcat.conf 셋팅.
	
	protected static final String[] CMD_LIST_COLLECTION = new String[]{"list", "collection"};
	protected static final String[] CMD_INFO_COLLECTION = new String[]{"info"}; //컬렉션정보-색인크기, 위치등..
	
	protected static final String[] CMD_SHOW_SCHEMA = new String[]{"show", "schema"};
	protected static final String[] CMD_SHOW_DATASOURCE = new String[]{"show", "datasource"};
	
	protected static final String[] CMD_START_FULLINDEX = new String[]{"start", "index", "full"};
	protected static final String[] CMD_START_INCINDEX = new String[]{"start", "index", "inc"};
	protected static final String[] CMD_STATUS_FULLINDEX = new String[]{"status", "index", "full"};
	protected static final String[] CMD_STATUS_INCINDEX = new String[]{"status", "index", "inc"};
	
	protected static final String[] CMD_SET_SCHEDULE_FULLINDEX = new String[]{"set", "schedule", "full"};
	protected static final String[] CMD_SET_SCHEDULE_INCINDEX = new String[]{"set", "schedule", "inc"};
	
	abstract public boolean isCommand(String[] cmd);
	abstract public CommandResult doCommand();
	
	protected boolean isCommand(String[] expected, String[] actual){
		for (int i = 0; i < expected.length; i++) {
			if(!expected[i].equalsIgnoreCase(actual[i])){
				return false;
			}
		}
		return true;
	}
}
