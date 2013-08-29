package org.fastcatsearch.http.action.service;

import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.SingleSearchJob;
import org.fastcatsearch.query.QueryMap;

public class SingleSearchAction extends AbstractSearchAction {

	public SingleSearchAction(String type) {
		super(type);
	}

	protected Job createSearchJob(QueryMap queryMap) {
		SingleSearchJob job = new SingleSearchJob();
		job.setArgs(queryMap);
		return job;
	}

}
