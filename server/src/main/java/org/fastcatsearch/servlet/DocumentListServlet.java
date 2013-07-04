/*
 * Copyright (C) 2011 WebSquared Inc. http://websqrd.com
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.fastcatsearch.servlet;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.job.DocumentListJob;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.util.ResultStringer;
import org.fastcatsearch.util.StringifyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentListServlet extends AbstractDocumentListServlet {

	private static final long serialVersionUID = 963640595944747847L;
	private static Logger logger = LoggerFactory.getLogger(DocumentListServlet.class);

	public DocumentListServlet(int resultType) {
		super(resultType);
	}

	@Override
	protected void doSearch(long requestId, String queryString, HttpServletResponse response) throws ServletException, IOException {
		long searchTime = 0;
		long st = System.currentTimeMillis();
		DocumentListJob documentListJob = (DocumentListJob) createDocumentListJob(queryString);

		ResultFuture resultFuture = JobService.getInstance().offer(documentListJob);
		Object obj = resultFuture.take();
		searchTime = (System.currentTimeMillis() - st);
		writeSearchLog(requestId, obj, searchTime);
		Result result = null;

		if (resultFuture.isSuccess())
			result = (Result) obj;

		ResultStringer rStringer = getResultStringer();
		writeHeader(response, rStringer);

		AbstractDocumentListResultWriter resultWriter = createSearchResultWriter(response.getWriter());

		try {
			resultWriter.writeResult(result, rStringer, searchTime, resultFuture.isSuccess());
		} catch (StringifyException e) {
			logger.error("", e);
		}

		response.getWriter().close();

	}

	protected Job createDocumentListJob(String queryString) {
		DocumentListJob job = new DocumentListJob();
		job.setArgs(new String[] { queryString });
		return job;
	}

	@Override
	protected AbstractDocumentListResultWriter createSearchResultWriter(Writer writer) {
		return new DocumentListResultWriter(writer, isAdmin);
	}

}
