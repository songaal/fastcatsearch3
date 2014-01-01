package org.fastcatsearch.job.statistics;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.StatisticsSettings.Category;
import org.fastcatsearch.statistics.CategoryStatistics;
import org.fastcatsearch.statistics.SearchStatisticsService;
import org.fastcatsearch.transport.TransportException;

/*
 * 각 search node에서 실행되는 키워드 로그 master로 전송작업. 
 * */
public class CollectRealTimePopularKeywordJob extends Job implements Streamable {

	private static final long serialVersionUID = 3670077290640786680L;

	private String category;
	
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
				File rtTmpHome = environment.filePaths().getStatisticsRoot().file(category.getId(), "rt", "collect");

				File targetFile = new File(rtTmpHome, nodeService.getMyNode().id() + ".log");
				File targetFile2 = environment.filePaths().relativise(targetFile);

				logger.debug("send rt file to {} | {} > {}", nodeService.getMaserNode(), sourceFile, targetFile2);
				try {
					nodeService.sendFile(nodeService.getMaserNode(), sourceFile, targetFile2);
				} catch (TransportException e) {
					logger.error("", e);
				}

			}

		}

		return new JobResult(true);
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		
	}

}
