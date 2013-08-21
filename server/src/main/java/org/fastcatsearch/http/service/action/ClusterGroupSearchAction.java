package org.fastcatsearch.http.service.action;

import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.cluster.ClusterGroupSearchJob;
import org.fastcatsearch.query.QueryMap;

public class ClusterGroupSearchAction extends AbstractSearchAction {

	public ClusterGroupSearchAction(String type) {
		super(type);
	}

	@Override
	protected Job createSearchJob(QueryMap queryMap) {
		Job searchJob = new ClusterGroupSearchJob();
		searchJob.setArgs(queryMap);
    	return searchJob;
	}

}
