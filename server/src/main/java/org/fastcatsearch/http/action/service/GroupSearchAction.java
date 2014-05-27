package org.fastcatsearch.http.action.service;

import java.io.Writer;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.writer.AbstractSearchResultWriter;
import org.fastcatsearch.http.writer.GroupResultWriter;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.search.GroupSearchJob;
import org.fastcatsearch.query.QueryMap;

@ActionMapping("/service/search-single/group")
public class GroupSearchAction extends AbstractSearchAction {

	@Override
	protected Job createSearchJob(QueryMap queryMap) {
		Job searchJob = new GroupSearchJob();
		searchJob.setArgs(queryMap);
    	return searchJob;
	}

	@Override
	protected AbstractSearchResultWriter createSearchResultWriter(Writer writer) {
		return new GroupResultWriter(getSearchResultWriter(writer));
	}
}
