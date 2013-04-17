package org.fastcatsearch.cli.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.cli.Command;
import org.fastcatsearch.cli.CommandException;
import org.fastcatsearch.cli.CommandResult;
import org.fastcatsearch.cli.ConsoleSessionContext;
import org.fastcatsearch.control.JobController;
import org.fastcatsearch.control.JobResult;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.query.Row;
import org.fastcatsearch.job.SearchJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchActionCommand extends Command {
	
	private static final Logger logger = LoggerFactory.getLogger(SearchActionCommand.class);

	@Override
	public boolean isCommand(String[] cmd) {
		return isCommand(CMD_SEARCH, cmd);
	}

	@Override
	public CommandResult doCommand(String[] cmd, ConsoleSessionContext context)
			throws IOException, CommandException {
		
		String msg = "";
		StringBuilder queryBuffer = new StringBuilder();
		
		for(int inx=1;inx<cmd.length;inx++) {
			queryBuffer.append(cmd[inx]);
		}
		
		SearchJob job = new SearchJob();
		job.setArgs(new String[] { queryBuffer.toString() });
		Result result = null;
		JobResult jobResult = JobController.getInstance().offer(job);
		Object obj = jobResult.poll(100);
		
		if(jobResult.isSuccess()) {
			result = (Result)obj;
			int columns = result.getFieldCount();
			String[] header = result.getFieldNameList();
			List<Object[]> data = new ArrayList<Object[]>();
			for(Row row : result.getData()) {
				Object[] record = new Object[columns];
				for(int colInx=0;colInx < columns; colInx++) {
					record[colInx] = new String(row.get(colInx));
				}
				data.add(record);
			}
			msg = super.printData(data, header);
			return new CommandResult(msg, CommandResult.Status.SUCCESS);
		}
		return new CommandResult(msg, CommandResult.Status.FAIL);
	}

}
