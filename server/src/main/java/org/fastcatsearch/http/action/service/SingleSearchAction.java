package org.fastcatsearch.http.action.service;

import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.SingleSearchJob;
import org.fastcatsearch.query.QueryMap;

public class SingleSearchAction extends AbstractSearchAction {

	protected Job createSearchJob(QueryMap queryMap) {
		SingleSearchJob job = new SingleSearchJob();
		job.setArgs(queryMap);
		return job;
	}

}
