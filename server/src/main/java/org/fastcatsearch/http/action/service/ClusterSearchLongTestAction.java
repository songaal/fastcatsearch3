package org.fastcatsearch.http.action.service;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.ActionMethod;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.search.ClusterSearchJob;
import org.fastcatsearch.job.search.ClusterSearchLongTestJob;
import org.fastcatsearch.query.QueryMap;

@ActionMapping(value = "/service/search-long-test", method = {ActionMethod.GET, ActionMethod.POST})
public class ClusterSearchLongTestAction extends AbstractSearchAction {

	@Override
	protected Job createSearchJob(QueryMap queryMap) {
        ClusterSearchLongTestJob job = new ClusterSearchLongTestJob();
		job.setArgs(queryMap);
		return job;
	}

}
