package org.fastcatsearch.http.action.service;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.cluster.ClusterSearchJob;
import org.fastcatsearch.query.QueryMap;

@ActionMapping("/service/search")
public class ClusterSearchAction extends AbstractSearchAction {

	@Override
	protected Job createSearchJob(QueryMap queryMap) {
		ClusterSearchJob job = new ClusterSearchJob();
		job.setArgs(queryMap);
		return job;
	}

}
