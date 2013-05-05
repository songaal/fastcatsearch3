package org.fastcatsearch.cli.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.fastcatsearch.cli.Command;
import org.fastcatsearch.cli.CommandException;
import org.fastcatsearch.cli.CommandResult;
import org.fastcatsearch.cli.ConsoleSessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelpCommand extends Command {
	
	private static Logger logger = LoggerFactory.getLogger(HelpCommand.class);
	
	@Override
	public boolean isCommand(String[] cmd) {
		logger.debug("cmd : {}", new Object[]{cmd});
		return isCommand(CMD_HELP, cmd);
	}

	@Override
	public CommandResult doCommand(String[] cmd, ConsoleSessionContext context) throws IOException, CommandException {
		StringBuilder sb = new StringBuilder();
		sb.append("");
		
		InputStream stream = getClass().getResourceAsStream("/help.txt");
		BufferedReader br = new BufferedReader( new InputStreamReader(stream,"utf-8"));
		String line = "";
		while ( true )
		{
			line = br.readLine();
			if (line == null)
				break;
			sb.append(line).append("\r\n");
		}
		
		br.close();		
		return new CommandResult(sb.toString(), CommandResult.Status.SUCCESS);
	}
}

/*  1. System Information
 *  sysinfo;
 *  	- this operation show all fastcatsearch system infomation
 *  
 *  show setting;
 *  	- this operation show fastcatsearch system enviroment values
 *  
 *  2. Indexing operation 
 *  start index [F|I]
 *  	- indexing collection
 *  	- third parameter  
 *  	 	F :  Full Indexing. 
 *  		I :  Increment Indexing.
 *  	- this operation must be execute after "use collection [collection Name]" operation. 
 *  
 *  3. Collection operation
 * 	list collection;
 *  	- this operation will show all collections
 *  
 * 	use collection [collection Name]
 *  	- this operation work like DBMS "use" operation
 *      - after this operation. other operation will referece [collection Name]
 *  
 *  info;
 *  	- this operation show collection status information
 *  	- fieldCount, indexFieldCount, groupFieldCount, sortFieldCount, dataSourceType, Activate Status 
 * 
 *  show schema;
 *  	- this operation show collection's schema 
 *  	- this operation work like DBMS "desc database" operation. 
 *  
 *  show datasource;
 *  	- this operation show collection's datasource information
 *  	- datasource Type has four type DB, FILE, WEB, Custom
 *  	-- DB
 *    		collection data from database
 *  	-- FILE 
 *    		collecting data from file 
 *  	-- WEB
 *    		collect data from web
 *  	-- Custom
 *    		collect data from custom setting.
 *    		this operation will support by using user define class, 
 *    		that class must be inherited by DataSourceModifier.
 *    
 *  show schedule;
 *   	- this operation will show collection's Schedule setting and status. 
 *   
 * schedule operations
 * schedule set [F|I] d:h:m
 * 		- this operation set current collection's indexing schedule
 * 		- schedule start time is current time;
 * 		- if you have already set schedule then this operation delete them and reset.
 *  
 * 		parameters 
 * 		[F|I] 
 * 			F : Full indexing
 * 			I : Increment indexing
 * 		
 * 		d:h:m
 * 			d: day
 *			h: hour
 *			m: minute 
 *		ie. 1:0:0 -> 1day.
 *			0:2:0 -> 2hour. 
 * 		
 * shchedule start [F|I];
 * 		- this operation set F|I schedule Activate 
 * 
 * schedule stop [F|I];
 * 		- this operation set F|I schedule DeActivate
 * 
 * schedule delete;
 * 		- this operation delete all schedule
 * 
 * search [search Command]
 * 		- this operation send query to fastcatsearch and print result
 * 		- you can see search Command from fastcatsearch wiki.
 * */
