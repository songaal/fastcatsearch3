package org.fastcatsearch.cli;

public class CommandException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4168010294735694200L;
	
	private CommandResult result;
	
	public CommandException (String msg) {
		this.result = new CommandResult(msg, CommandResult.Status.ERROR);
	}
	
	public CommandException (CommandResult result) {
		this.result = result;
	}
	
	public CommandResult getCommandResult() {
		return result;
	}
	
	public void setCommandResult(CommandResult result) {
		this.result = result;
	}

	@Override
	public String getMessage() {
		return result.result;
	}
}