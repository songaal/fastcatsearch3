package org.fastcatsearch.job.statistics;

import java.io.File;
import java.util.Collection;

import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.StaticticsSettings.Category;
import org.fastcatsearch.statistics.CategoryStatistics;
import org.fastcatsearch.statistics.SearchStatisticsService;
import org.fastcatsearch.transport.TransportException;

/*
 * 각 search node에서 실행되는 키워드 로그 master로 전송작업. 
 * */
public class SearchPopularKeywordRealTimeCollectJob extends Job {

	private static final long serialVersionUID = 3670077290640786680L;

	@Override
	public JobResult doRun() throws FastcatSearchException {

		SearchStatisticsService searchStatisticsService = ServiceManager.getInstance().getService(SearchStatisticsService.class);
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		Collection<CategoryStatistics> list = searchStatisticsService.getCategoryStatisticsList();
		for (CategoryStatistics categoryStatistics : list) {
			Category category = categoryStatistics.category();
			if (category.isUseRealTimePopularKeyword()) {

				File sourceFile = categoryStatistics.getRealTimeKeywordFile();
				
				if(sourceFile == null || !sourceFile.exists()){
					logger.warn("Cannot find log file > {}", sourceFile);
					continue;
				}
				File rtTmpHome = environment.filePaths().getStatisticsRoot().file(category.getId(), "rt", "tmp");

				File targetFile = new File(rtTmpHome, nodeService.getMyNode().id() + ".log");
				File targetFile2 = environment.filePaths().relativise(targetFile);

				logger.debug("send rt file to {} | {} > {}", nodeService.getMaserNode(), sourceFile, targetFile2);
				try {
					nodeService.sendFile(nodeService.getMaserNode(), sourceFile, targetFile2);
				} catch (TransportException e) {
					e.printStackTrace();
				}

			}

		}

		return new JobResult(true);
	}

}
