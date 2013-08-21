package org.fastcatsearch.http.service.action;

import org.fastcatsearch.job.GroupSearchJob;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.query.QueryMap;

public class GroupSearchAction extends AbstractSearchAction {

	public GroupSearchAction(String type) {
		super(type);
	}

	@Override
	protected Job createSearchJob(QueryMap queryMap) {
		Job searchJob = new GroupSearchJob();
		searchJob.setArgs(queryMap);
    	return searchJob;
	}

}
