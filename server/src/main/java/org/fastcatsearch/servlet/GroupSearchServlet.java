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

package org.fastcatsearch.servlet;

import java.io.Writer;

import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.search.GroupSearchJob;

public class GroupSearchServlet extends SearchServlet {
	
	private static final long serialVersionUID = 3688812897370632625L;

	public GroupSearchServlet(int resultType){
    	super(resultType);
    }
    
	@Override
	protected Job createSearchJob(String queryString) {
		Job searchJob = new GroupSearchJob();
		searchJob.setArgs(new String[]{queryString});
    	return searchJob;
	}

	@Override
	protected AbstractSearchResultWriter createSearchResultWriter(Writer writer) {
		return null;//new GroupResultWriter(writer);
	}
}
