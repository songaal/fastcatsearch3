package org.fastcatsearch.job.statistics;

import java.util.List;

import org.fastcatsearch.cluster.ClusterUtils;
import org.fastcatsearch.cluster.NodeJobResult;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.job.MasterNodeJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.StatisticsSettings;
import org.fastcatsearch.settings.StatisticsSettings.Category;
import org.fastcatsearch.statistics.SearchStatisticsService;

public class CollectSearchStatisticsLogsMainJob extends MasterNodeJob {

	private static final long serialVersionUID = -8689402133572211618L;

	public CollectSearchStatisticsLogsMainJob(String type){
		args = type;
	}
	
	@Override
	public JobResult doRun() throws FastcatSearchException {
		String type = getStringArgs();
		
		IRService irService = ServiceManager.getInstance().getService(IRService.class);

		// 모든 컬렉션에 걸쳐 search node를 확인한다.
		List<String> searchNodeList = irService.getSearchNodeList();
		if (searchNodeList == null || searchNodeList.size() == 0) {
			throw new FastcatSearchException("There's no search node. {}", searchNodeList);
		}
		
		SearchStatisticsService searchStatisticsService = ServiceManager.getInstance().getService(SearchStatisticsService.class);
		StatisticsSettings statisticsSettings = searchStatisticsService.statisticsSettings();
		List<Category> categoryList = statisticsSettings.getCategoryList();
		// TODO 이미 가져왔다고 가정하고 테스트.

		// 동기적으로 가져오므로, 이 루프가 끝나면 모든 카테고리의 로그파일을 전송받은 상태이다.
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		for (Category category : categoryList) {
			// 카테고리별로 파일을 취합해온다. 
			if (category.isUseRealTimePopularKeyword()) {
				CollectSearchStatisticsLogsJob job = new CollectSearchStatisticsLogsJob(type);
				NodeJobResult[] nodeJobResultList = ClusterUtils.sendJobToNodeIdList(job, nodeService, searchNodeList, true);
			}
		}
		
		
		return new JobResult(true);
	}

}
