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

package org.fastcatsearch.servlet.cluster;

import java.io.Writer;

import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.cluster.ClusterSearchJob;
import org.fastcatsearch.servlet.AbstractSearchResultWriter;
import org.fastcatsearch.servlet.SearchResultWriter;
import org.fastcatsearch.servlet.SearchServlet;

public class ClusterSearchServlet extends SearchServlet {

	private static final long serialVersionUID = -4306873976446629787L;
	
	public ClusterSearchServlet(int resultType) {
		super(resultType);
	}

	protected Job createSearchJob(String queryString) {
		ClusterSearchJob job = new ClusterSearchJob();
		job.setArgs(new String[] { queryString });
		return job;
	}

	protected AbstractSearchResultWriter createSearchResultWriter(Writer writer) {
		return new SearchResultWriter(writer, isAdmin);
	}
}