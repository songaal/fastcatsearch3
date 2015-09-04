package org.fastcatsearch.http.action.service;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.ActionMethod;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.search.ClusterSearchJob;
import org.fastcatsearch.query.QueryMap;

@ActionMapping(value = "/service/search", method = {ActionMethod.GET, ActionMethod.POST})
public class ClusterSearchAction extends AbstractSearchAction {

	@Override
	protected Job createSearchJob(QueryMap queryMap) {
		ClusterSearchJob job = new ClusterSearchJob();
		job.setArgs(queryMap);
		return job;
	}

}
