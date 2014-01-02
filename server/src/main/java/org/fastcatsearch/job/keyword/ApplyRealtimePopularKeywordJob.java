package org.fastcatsearch.job.keyword;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.cluster.ClusterUtils;
import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeJobResult;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.mapper.PopularKeywordMapper;
import org.fastcatsearch.db.vo.PopularKeywordVO;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.job.MasterNodeJob;
import org.fastcatsearch.keyword.KeywordDictionary.KeywordDictionaryType;
import org.fastcatsearch.keyword.KeywordDictionaryCompiler;
import org.fastcatsearch.keyword.KeywordService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.KeywordServiceSettings;
import org.fastcatsearch.settings.KeywordServiceSettings.KeywordServiceCategory;
import org.fastcatsearch.settings.StatisticsSettings;
import org.fastcatsearch.settings.StatisticsSettings.Category;
import org.fastcatsearch.statistics.SearchStatisticsService;
import org.fastcatsearch.transport.TransportException;
import org.fastcatsearch.transport.common.SendFileResultFuture;

public class ApplyRealtimePopularKeywordJob extends MasterNodeJob {

	private static final long serialVersionUID = -1701020768052291618L;

	@Override
	public JobResult doRun() throws FastcatSearchException {

		IRService irService = ServiceManager.getInstance().getService(IRService.class);

		// 모든 컬렉션에 걸쳐 search node를 확인한다.
		List<String> searchNodeList = irService.getSearchNodeList();
		if (searchNodeList == null || searchNodeList.size() == 0) {
			throw new FastcatSearchException("There's no search node. {}", searchNodeList);
		}

		SearchStatisticsService searchStatisticsService = ServiceManager.getInstance().getService(SearchStatisticsService.class);
		StatisticsSettings statisticsSettings = searchStatisticsService.statisticsSettings();
		List<Category> categoryList = statisticsSettings.getCategoryList();

		KeywordService keywordService = ServiceManager.getInstance().getService(KeywordService.class);
		KeywordServiceSettings keywordServiceSettings = keywordService.keywordServiceSettings();
		List<KeywordServiceCategory> keywordServiceCategoryList = keywordServiceSettings.getCategoryList();

		
		/*
		 * 1. 사전 컴파일.
		 * */
		List<String> successCategoryIdList = new ArrayList<String>();
		OUTTER: for (KeywordServiceCategory keywordCategory : keywordServiceCategoryList) {
			// 카테고리별로 통계를 낸다.
			String categoryId = keywordCategory.getId();
			boolean found = false;
			for(Category category : categoryList){
				if(category.getId().equalsIgnoreCase(categoryId)){
					found = true;
					if(!category.isUseRealTimePopularKeyword()){
						//통계를 내는 카테고리가 아니면 건너뛴다.
						continue OUTTER;
					}else{
						break;
					}
				}
			}
			if(!found){
				//statisticsSettings 에 없는 카테고리는 건너뛴다. 
				continue;
			}
			
			
			if (keywordCategory.isServiceRealTimePopularKeyword()) {
				File dictFile = keywordService.getFile(categoryId, KeywordDictionaryType.POPULAR_KEYWORD_REALTIME);
				//디렉토리 생성.
				if(!dictFile.getParentFile().exists()){
					dictFile.getParentFile().mkdirs();
				}
				
				MapperSession<PopularKeywordMapper> mapperSession = keywordService.getMapperSession(PopularKeywordMapper.class);
				try {
					PopularKeywordMapper mapper = mapperSession.getMapper();
					List<PopularKeywordVO> list = mapper.getTopEntryList(categoryId, "REALTIME", 10);
					KeywordDictionaryCompiler.compilePopularKeyword(list, dictFile);
					//성공. dictFile 파일을 전파를 위해 categoryId 유지.
					successCategoryIdList.add(categoryId);
					
					//reload keywordService popular
					keywordService.loadPopularKeywordDictionary(categoryId, KeywordDictionaryType.POPULAR_KEYWORD_REALTIME, 1);
				} catch (Exception e) {
					logger.error("", e);
				} finally {
					if (mapperSession != null) {
						mapperSession.closeSession();
					}
				}

			}
		}
		
		List<String> serviceNodeIdList = keywordServiceSettings.getServiceNodeList();
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		List<Node> serviceNodeList = nodeService.getNodeById(serviceNodeIdList);
		
		/*
		 * 2. 사전 파일 전송.
		 */
		for(String categoryId : successCategoryIdList){
			File dictFile = keywordService.getFile(categoryId, KeywordDictionaryType.POPULAR_KEYWORD_REALTIME);
			for(Node node : serviceNodeList){
				File targetFile = environment.filePaths().relativise(dictFile);
				SendFileResultFuture future;
				try {
					future = nodeService.sendFile(node, dictFile, targetFile);
					if(future != null){
						future.take();
					}
				} catch (TransportException e) {
					logger.error("", e);
				}
			}
		}
		
		/*
		 * 리로드 요청.
		 */
		ReloadKeywordDictionaryJob reloadJob = new ReloadKeywordDictionaryJob();
		reloadJob.setArgs(new Object[]{successCategoryIdList, KeywordDictionaryType.POPULAR_KEYWORD_REALTIME});
		NodeJobResult[] resultList = ClusterUtils.sendJobToNodeIdList(reloadJob, nodeService, serviceNodeIdList, false);
		
		return new JobResult(true);
	}

}
