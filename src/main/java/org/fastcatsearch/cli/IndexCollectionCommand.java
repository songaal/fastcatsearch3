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

import org.fastcatsearch.cli.CommandResult.Status;

public class IndexCollectionCommand extends Command {
	public CommandResult doCommand(){
		
		
		return new CommandResult("결과 정보입니다.", Status.SUCCESS);
		
	}

	@Override
	public boolean isCommand() {
		// TODO Auto-generated method stub
		return false;
	}
}
