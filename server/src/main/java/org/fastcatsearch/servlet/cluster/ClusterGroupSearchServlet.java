package org.fastcatsearch.servlet.cluster;

import java.io.Writer;

import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.cluster.ClusterGroupSearchJob;
import org.fastcatsearch.servlet.AbstractSearchResultWriter;
import org.fastcatsearch.servlet.GroupResultWriter;
import org.fastcatsearch.servlet.SearchServlet;

public class ClusterGroupSearchServlet extends SearchServlet {
	
	private static final long serialVersionUID = 5098561451064873332L;

	public ClusterGroupSearchServlet(int resultType){
    	super(resultType);
    }
    
	@Override
	protected Job createSearchJob(String queryString) {
		Job searchJob = new ClusterGroupSearchJob();
		searchJob.setArgs(new String[]{queryString});
    	return searchJob;
	}

	@Override
	protected AbstractSearchResultWriter createSearchResultWriter(Writer writer) {
		return null;//new GroupResultWriter(writer);
	}
}
