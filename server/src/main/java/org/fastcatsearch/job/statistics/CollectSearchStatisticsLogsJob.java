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

public class CollectSearchStatisticsLogsJob extends Job implements Streamable {
	private static final long serialVersionUID = -7019696060888896839L;
	
	public static String TYPE_REALTIME = "REALTIME";
	public static String TYPE_DAILY = "DAILY";

	public CollectSearchStatisticsLogsJob() {
	}

	public CollectSearchStatisticsLogsJob(String type) {
		this.args = type;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		args = input.readString();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString((String) args);
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {
		String type = getStringArgs();
		SearchStatisticsService searchStatisticsService = ServiceManager.getInstance().getService(SearchStatisticsService.class);
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		Collection<CategoryStatistics> list = searchStatisticsService.getCategoryStatisticsList();
		for (CategoryStatistics categoryStatistics : list) {
			Category category = categoryStatistics.category();

			if (type.equals(TYPE_REALTIME)) {
				if (category.isUseRealTimePopularKeyword()) {
					File sourceFile = categoryStatistics.getRealTimeKeywordFile();
					logger.info("## COLLECT Realtime search log > {}", sourceFile);

					if (sourceFile == null || !sourceFile.exists()) {
						logger.warn("Cannot find log file > {}", sourceFile);
						continue;
					}
					File collectHome = environment.filePaths().getStatisticsRoot().file(category.getId(), "collect", "rt");

					File targetFile = new File(collectHome, nodeService.getMyNode().id() + ".log");
					File targetFile2 = environment.filePaths().relativise(targetFile);

					logger.debug("send rt file to {} | {} > {}", nodeService.getMaserNode(), sourceFile, targetFile2);
					try {
						nodeService.sendFile(nodeService.getMaserNode(), sourceFile, targetFile2);
					} catch (TransportException e) {
						logger.error("", e);
					}

				}
			} else if (type.equals(TYPE_DAILY)) {

				//무조건 가져온다.
				File sourceFile = categoryStatistics.getOneDayKeywordFile();
				logger.info("## COLLECT One-day search log > {}", sourceFile);

				if (sourceFile == null || !sourceFile.exists()) {
					logger.warn("Cannot find log file > {}", sourceFile);
					continue;
				}
				File collectHome = environment.filePaths().getStatisticsRoot().file(category.getId(), "collect", "daily");

				File targetFile = new File(collectHome, nodeService.getMyNode().id() + ".log");
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
}
