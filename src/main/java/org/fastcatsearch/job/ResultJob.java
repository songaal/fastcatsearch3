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

package org.fastcatsearch.job;

import org.fastcatsearch.control.JobException;
import org.fastcatsearch.service.ServiceException;

public class ResultJob extends Job {
	
	private static final long serialVersionUID = -2003755021642747642L;
	
	public Object getResult(){
		return args;
	}
	public Object run0() throws JobException, ServiceException {
		return null;
	}

}
