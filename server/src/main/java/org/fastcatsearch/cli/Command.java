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

import org.fastcatsearch.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author lupfeliz
 *
 */
public abstract class Command {

	public static final String SESSION_KEY_USING_COLLECTION = "org.fastcatsearch.cli.Command@session";
	
	private static final Logger logger = LoggerFactory.getLogger(Command.class);
	
	protected static final String[] CMD_USE_COLLECTION = new String[]{"use", "collection" };//세션에 컬렉션 사용을 명령함.
	
	protected static final String[] CMD_INFO_SYSTEM = new String[]{"sysinfo"}; //시스템 정보.
//	protected static final String[] CMD_SHOW_DICTIONARY = new String[]{"show", "dictionary"};
	protected static final String[] CMD_SHOW_SETTING = new String[]{"show", "setting"}; //fastcat.conf 셋팅.
	
	protected static final String[] CMD_LIST_COLLECTION = new String[]{"list", "collection"};
	protected static final String[] CMD_INFO_COLLECTION = new String[]{"info"}; //컬렉션정보-색인크기, 위치등..
	
	protected static final String[] CMD_SHOW_SCHEMA = new String[]{"show", "schema"};
	protected static final String[] CMD_SHOW_DATASOURCE = new String[]{"show", "datasource"};
	protected static final String[] CMD_SHOW_SCHEDULE = new String[]{"show", "schedule"};
	
	protected static final String[] CMD_START_INDEX = new String[]{"start", "index" };
	protected static final String[] CMD_STATUS_INDEX = new String[]{"status", "index" };
	
	protected static final String[] CMD_SET_SCHEDULE_INDEX = new String[]{"schedule", "set"};
	protected static final String[] CMD_START_SCHEDULE_INDEX = new String[]{"schedule", "start"};
	protected static final String[] CMD_STOP_SCHEDULE_INDEX = new String[]{"schedule", "stop"};
	protected static final String[] CMD_DELETE_SCHEDULE_INDEX = new String[]{"schedule", "delete"};
		
	protected static final String[] CMD_SEARCH = new String[] { "search" };
	protected static final String[] CMD_HELP = new String[] { "help" };
		
	protected Environment environment;
	
	abstract public boolean isCommand(String[] cmd);
	abstract public CommandResult doCommand(String[] cmd, ConsoleSessionContext context) throws IOException, CommandException;
	
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
	protected boolean isCommand(String[] expected, String[] actual){
		logger.trace("compare {} : {}", new Object[] { Arrays.asList(expected).toString(), 
				Arrays.asList(actual).toString() });
		for (int i = 0; i < expected.length; i++) {
			if(!(i < actual.length)) {
				return false;
			}
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
			int len = ListTableDecorator.length(str);
			if( len > maxColSize) {
				maxColSize = len;
			}
		}
		
		ListTableDecorator ltd = new ListTableDecorator(writer, Arrays.asList(new Integer[] { maxColSize }) );
		ltd.printbar();
		for(int inx=0;inx<data.length;inx++) {
			if(inx>0) {
				ltd.printbar();
			}
			ltd.printData(0, data[inx], 1);
		}
		ltd.printbar();
	}
	
	protected String printData(Object[] data) throws IOException {
		StringBuilder sb = new StringBuilder();
		printData(sb, data);
		return sb.toString();
	}
	
	protected String printData(String msg) throws IOException {
		StringBuilder sb = new StringBuilder();
		printData(sb, new Object[] { msg });
		return sb.toString();
	}
	
	/**
	 * printout complex table data
	 * @param writer
	 * @param data
	 * @param header
	 * @throws IOException
	 */
	protected void printData(Appendable writer, List<Object[]> data, String[] header) throws IOException {
		List<Integer> maxColSizes = null;
		for(Object[] cols : data) {
			//
			// Measure header's column sizes
			//
			if(maxColSizes==null && header!=null) {
				maxColSizes = new ArrayList<Integer>();
				for(String value : header) {
					maxColSizes.add(ListTableDecorator.length(value));
				}
			}
			//
			// Measure data's column size 
			//
			// First row's column size
			if(maxColSizes==null) {
				maxColSizes = new ArrayList<Integer>();
				for(Object value : cols) {
					maxColSizes.add(ListTableDecorator.length(value.toString()));
				}
				continue;
			}
			for(int inx=0;inx<cols.length;inx++) {
				Object value  = cols[inx];
				String str = "";
				int len = 0;
				if(value!=null) {
					str = value.toString();
				}
				len = ListTableDecorator.length(str);
				if(len > maxColSizes.get(inx)) {
					maxColSizes.set(inx, len);
				}
			}
		}
		
		ListTableDecorator ltd = new ListTableDecorator(writer,maxColSizes);
		ltd.printbar();
		int maxLines = 1;
		if(header!=null) {
			for(Object value : header) {
				int lines = ((String)value).split("\n").length;
				if(maxLines < lines) {
					maxLines = lines;
				}
			}
	
			for(int colInx=0;colInx<header.length; colInx++) {
				ltd.printData(colInx, header[colInx], maxLines);
			}
		}
		ltd.printbar();
		for(Object[] cols : data) {
			for(Object value : cols) {
				String str = "";
				if(value!=null) { 
					str = value.toString();
				}
				int lines = str.split("\n").length;
				if(maxLines < lines) {
					maxLines = lines;
				}
			}
			
			for(int colInx=0;colInx<cols.length; colInx++) {
				ltd.printData(colInx, cols[colInx], maxLines);
			}
			ltd.printbar();
		}
	}
	
	protected void printData(Appendable writer, List<Object[]> data) throws IOException {
		printData(writer, data);
	}
	
	protected String printData(List<Object[]> data, String[] header) throws IOException {
		StringBuilder sb = new StringBuilder();
		printData(sb, data, header);
		return sb.toString();
	}
}
