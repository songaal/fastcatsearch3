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

public class CommandResult {
	public enum Status { SUCCESS, FAIL, WARNING, ERROR };
	
	String result;
	Status status;
	
	public CommandResult(String result, Status status){
		this.result = result;
		this.status = status;
	}
}
