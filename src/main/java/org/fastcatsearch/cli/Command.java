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

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Command {
	
	public static final String SESSION_KEY = "org.fastcatsearch.cli.Command@session";
	
	private static final Logger logger = LoggerFactory.getLogger(Command.class);
	
	protected static final String[] CMD_USE_COLLECTION = new String[]{"use", "collection" };//세션에 컬렉션 사용을 명령함.
	
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
	abstract public CommandResult doCommand(String[] cmd, ConsoleSessionContext context) throws IOException, CommandException;
	
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

	/**
	 * printout single column data
	 * @param writer
	 * @param data
	 */
	protected void printData(Appendable writer, Object[] data) throws IOException {
		//List<Integer> columnSize = new ArrayList;
		int maxColSize = 1;
		for(Object value : data) {
			String str = value.toString();
			if(str.length() > maxColSize) {
				maxColSize = str.length();
			}
		}
		
		ListTableDecorator ltd = new ListTableDecorator(writer, Arrays.asList(new Integer[] { maxColSize }) );
		ltd.printbar();
		for(Object value : data) {
			ltd.printData(0, value, 0);
		}
		ltd.printbar();
	}
	
	protected String printData(Object[] data) throws IOException {
		StringBuilder sb = new StringBuilder();
		printData(sb, data);
		return sb.toString();
	}
}
