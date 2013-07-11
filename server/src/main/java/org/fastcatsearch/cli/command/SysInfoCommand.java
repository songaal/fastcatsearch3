package org.fastcatsearch.cli.command;

import java.io.IOException;
import java.util.ArrayList;

import org.fastcatsearch.cli.Command;
import org.fastcatsearch.cli.CommandException;
import org.fastcatsearch.cli.CommandResult;
import org.fastcatsearch.cli.ConsoleSessionContext;

public class SysInfoCommand extends Command {

	@Override
	public boolean isCommand(String[] cmd) {
		return isCommand(CMD_INFO_SYSTEM, cmd);		
	}

	private String[] header = new String[] {"property name", "property value"};
	@Override
	public CommandResult doCommand(String[] cmd, ConsoleSessionContext context)
			throws IOException, CommandException {
		if ( cmd.length > 1)
		{
			return new CommandResult("invalid Command", CommandResult.Status.SUCCESS);
		}
		else
		{
			ArrayList<Object[]> data = new ArrayList<Object[]>();
//			StringBuilder sb = new StringBuilder();
			
//			data.add(new Object[]{"search engine version : ",	environment.VERSION});
			data.add(new Object[]{"search engine home : ",		environment.home()});
			data.add(new Object[]{"java Vendor : ",System.getProperty("java.vendor")});
			data.add(new Object[]{"java name   : ",System.getProperty("java.vm.name")});
			data.add(new Object[]{"java version: ",System.getProperty("java.version")});
			data.add(new Object[]{"operating system  : ",System.getProperty("os.name")+"("+System.getProperty("os.arch")+")"});
			data.add(new Object[]{"user acount : ",System.getProperty("user.name")});
			return new CommandResult(printData(data,header), CommandResult.Status.SUCCESS);
		}
		
	}

}
