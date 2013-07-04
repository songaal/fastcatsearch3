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

package org.fastcatsearch.task;

public class TaskException extends Exception{

	public TaskException(String message) {
		super(message);
	}
	
	public TaskException(String message, Throwable cause) {
        super(message, cause);
    }
	
	public TaskException(Throwable cause) {
        super(cause);
    }

}
