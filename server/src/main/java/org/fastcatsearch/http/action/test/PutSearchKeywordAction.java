package org.fastcatsearch.http.action.test;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.search.SearchStatistics;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.statistics.CategoryStatistics;
import org.fastcatsearch.statistics.SearchStatisticsService;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping("/test/put-search-keyword")
public class PutSearchKeywordAction extends ServiceAction {

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {
		String category = request.getParameter("category");
		String keyword = request.getParameter("keyword");
		String prevKeyword = request.getParameter("prevKeyword");
		
		SearchStatisticsService searchStatisticsService = ServiceManager.getInstance().getService(SearchStatisticsService.class);
		
		Query q = new Query();
		Map<String, String> userData = new HashMap<String, String>();
		q.getMeta().setUserData(userData);
		
		userData.put(SearchStatistics.CATEGORY, category);
		userData.put(SearchStatistics.KEYWORD, keyword);
		if(prevKeyword != null){
			userData.put(SearchStatistics.PREV_KEYWORD, prevKeyword);	
		}
		searchStatisticsService.searchStatistics().add(q);
		
		CategoryStatistics categoryStatistics = searchStatisticsService.categoryStatistics(category);
		
		int lastCount = categoryStatistics.getLastCount();
		logger.debug("categoryStatistics.getLastCount() > {}", categoryStatistics.getLastCount());
		
		Writer writer = response.getWriter();
		writeHeader(response);
		
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		responseWriter.object()
		.key("success").value(true)
		.key("lastCount").value(lastCount)
		.endObject();
		responseWriter.done();
	}

}
