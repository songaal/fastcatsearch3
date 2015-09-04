package org.fastcatsearch.http.action.service;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.ActionMethod;
import org.fastcatsearch.http.writer.AbstractSearchResultWriter;
import org.fastcatsearch.http.writer.GroupResultWriter;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.search.ClusterGroupSearchJob;
import org.fastcatsearch.query.QueryMap;

import java.io.Writer;

@ActionMapping(value = "/service/search/group", method = {ActionMethod.GET, ActionMethod.POST})
public class ClusterGroupSearchAction extends AbstractSearchAction {

	@Override
	protected Job createSearchJob(QueryMap queryMap) {
		Job searchJob = new ClusterGroupSearchJob();
		searchJob.setArgs(queryMap);
    	return searchJob;
	}
	
	@Override
	protected AbstractSearchResultWriter createSearchResultWriter(Writer writer, boolean isFieldLowercase, boolean noUnicode) {
		return new GroupResultWriter(getSearchResultWriter(writer, isFieldLowercase, noUnicode));
	}
}
