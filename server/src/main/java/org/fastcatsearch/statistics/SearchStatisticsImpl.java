package org.fastcatsearch.statistics;

import java.io.File;
import java.util.Map;

import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.search.SearchStatistics;

public class SearchStatisticsImpl implements SearchStatistics {
	private Map<String, CategoryStatistics> categoryStatisticsMap;
	
	public SearchStatisticsImpl(File staticsticsHome, Map<String, CategoryStatistics> categoryStatisticsMap) {
		this.categoryStatisticsMap = categoryStatisticsMap;
	}

	@Override
	public void add(Query q) {
		
		if (q != null) {
			Metadata metadata = q.getMeta();
			if(metadata != null && metadata.userData() != null){
				
				Map<String, String> userData = metadata.userData();
				String category = userData.get(SearchStatistics.CATEGORY);
				if(category != null){
					CategoryStatistics categoryStatistics = categoryStatisticsMap.get(category);
					// 통계추가.
					categoryStatistics.addStatistics(userData);
				}
			}
			

		}
	}

}
