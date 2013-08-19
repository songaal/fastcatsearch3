package org.fastcatsearch.http.service.action;

import java.io.PrintWriter;
import java.io.Writer;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.SearchJob;
import org.fastcatsearch.servlet.AbstractSearchResultWriter;
import org.fastcatsearch.servlet.SearchResultWriter;
import org.fastcatsearch.util.ResultStringer;
import org.fastcatsearch.util.StringifyException;

public class SingleSearchAction extends AbstractSearchAction {

	public SingleSearchAction() {
	}

	public SingleSearchAction(String type) {
		super(type);
	}

	protected Job createSearchJob(String queryString) {
		SearchJob job = new SearchJob();
		job.setArgs(new String[] { queryString });
		return job;
	}

	@Override
	public void doSearch(long requestId, String queryString, int timeout, PrintWriter writer) throws Exception {

		long searchTime = 0;
		long st = System.currentTimeMillis();
		Job searchJob = createSearchJob(queryString);

		ResultFuture jobResult = JobService.getInstance().offer(searchJob);
		Object obj = jobResult.poll(timeout);
		searchTime = (System.currentTimeMillis() - st);
		writeSearchLog(requestId, obj, searchTime);

		ResultStringer rStringer = getSearchResultStringer();

		AbstractSearchResultWriter resultWriter = createSearchResultWriter(writer);

		try {
			resultWriter.writeResult(obj, rStringer, searchTime, jobResult.isSuccess());
		} catch (StringifyException e) {
			logger.error("", e);
		}

		writer.close();

	}

	@Override
	protected AbstractSearchResultWriter createSearchResultWriter(Writer writer) {
		return new SearchResultWriter(writer, false);
	}

}
