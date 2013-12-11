package org.fastcatsearch.job.statistics;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.fastcatsearch.cluster.ClusterUtils;
import org.fastcatsearch.cluster.NodeJobResult;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.job.MasterNodeJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.StaticticsSettings;
import org.fastcatsearch.settings.StaticticsSettings.Category;
import org.fastcatsearch.statistics.CategoryStatistics;
import org.fastcatsearch.statistics.SearchStatisticsService;

/**
 * 실시간 인기검색어 취합 MAIN 작업.
 * */
public class CollectRealTimePopularKeywordMainJob extends MasterNodeJob {

	private static final long serialVersionUID = -187434814363709436L;

	@Override
	public JobResult doRun() throws FastcatSearchException {

		// 파일전송을 명령한다.
		// SearchPopularKeywordRealTimeCollectJob
		IRService irService = ServiceManager.getInstance().getService(IRService.class);

		// 모든 컬렉션에 걸쳐 search node를 확인한다.
		List<String> searchNodeList = irService.getSearchNodeList();
		if (searchNodeList == null || searchNodeList.size() == 0) {
			throw new FastcatSearchException("There's no search node. {}", searchNodeList);
		}
		SearchStatisticsService searchStatisticsService = ServiceManager.getInstance().getService(SearchStatisticsService.class);
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		Collection<CategoryStatistics> list = searchStatisticsService.getCategoryStatisticsList();
		for (CategoryStatistics categoryStatistics : list) {
			// 카테고리별로 파일을 취합해온다.
			Category category = categoryStatistics.category();
			if (category.isUseRealTimePopularKeyword()) {
				SearchPopularKeywordRealTimeCollectJob job = new SearchPopularKeywordRealTimeCollectJob();
				NodeJobResult[] nodeJobResultList = ClusterUtils.sendJobToNodeIdList(job, nodeService, searchNodeList, true);
			}
		}

		// 다 받으면 해당 위치의 파일들을 하나의 파일로 모두 모아서
		for (CategoryStatistics categoryStatistics : list) {
			Category category = categoryStatistics.category();
			if (category.isUseRealTimePopularKeyword()) {
				File targetDir = environment.filePaths().getStatisticsRoot().file(category.getId(), "rt");
				File tmpDir = new File(targetDir, "tmp");
				StaticticsSettings staticticsSettings = searchStatisticsService.staticticsSettings();
				RealTimePopularKeywordGenerator g = new RealTimePopularKeywordGenerator(tmpDir, targetDir, staticticsSettings); // TODO
				g.generate();
			}
		}

		// 통계를 낸다.

		return new JobResult(true);
	}
}
