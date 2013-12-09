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
import org.fastcatsearch.settings.StaticticsSetting.Category;
import org.fastcatsearch.statistics.CategoryStatistics;
import org.fastcatsearch.statistics.SearchStatisticsService;
import org.fastcatsearch.transport.TransportException;

public class SearchPopularKeywordRealTimeCollectJob extends Job {

	private static final long serialVersionUID = 3670077290640786680L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		
		SearchStatisticsService searchStatisticsService = ServiceManager.getInstance().getService(SearchStatisticsService.class);
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		Collection<CategoryStatistics> list = searchStatisticsService.getCategoryStatisticsList();
		for(CategoryStatistics categoryStatistics : list){
			Category category = categoryStatistics.category();
			if(category.isUseRealTimePopularKeyword()){
				
				
			}
			File sourceFile = categoryStatistics.getRealTimeKeywordFile();
			
			File a = null;//stat/category명/tmp/하위에 oneday.log.노드명 으로 기록한다. 
			File targetFile = environment.filePaths().relativise(sourceFile);
		
			if(!nodeService.isMaster()){
				
				try {
					nodeService.sendFile(nodeService.getMaserNode(), sourceFile, targetFile);
				} catch (TransportException e) {
					e.printStackTrace();
				}
			}
			
			
			
		}
		
		return null;
	}

}
