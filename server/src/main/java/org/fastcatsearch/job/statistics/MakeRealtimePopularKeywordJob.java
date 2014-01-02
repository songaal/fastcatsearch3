package org.fastcatsearch.job.statistics;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.fastcatsearch.alert.AlertServiceTest;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.mapper.PopularKeywordMapper;
import org.fastcatsearch.db.vo.PopularKeywordVO;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.job.MasterNodeJob;
import org.fastcatsearch.job.keyword.ApplyRealtimePopularKeywordJob;
import org.fastcatsearch.keyword.KeywordService;
import org.fastcatsearch.notification.NotificationService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.StatisticsSettings;
import org.fastcatsearch.settings.StatisticsSettings.Category;
import org.fastcatsearch.statistics.RealtimePopularKeywordGenerator;
import org.fastcatsearch.statistics.SearchStatisticsService;
import org.fastcatsearch.statistics.vo.RankKeyword;

/**
 * 실시간 인기검색어 생성 작업. 컴파일 및 적용은 아직 하지 않는다.
 * */
public class MakeRealtimePopularKeywordJob extends MasterNodeJob {

	private static final long serialVersionUID = -187434814363709436L;

	@Override
	public JobResult doRun() throws FastcatSearchException {

		String timeFormatString = "R" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date());// 지금시간.
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, -1);
		String prevTimeFormatString = "R" + new SimpleDateFormat("yyyyMMddHHmm").format(cal.getTime()); // 1시간 이전.

		SearchStatisticsService searchStatisticsService = ServiceManager.getInstance().getService(SearchStatisticsService.class);
		StatisticsSettings statisticsSettings = searchStatisticsService.statisticsSettings();
		List<Category> categoryList = statisticsSettings.getCategoryList();

		String fileEncoding = "utf-8";
		

		// 다 받으면 해당 위치의 파일들을 하나의 파일로 모두 모아서
		logger.debug("environment {}", environment);
		logger.debug("filePaths {}", environment.filePaths());
		logger.debug("getStatisticsRoot {}", environment.filePaths().getStatisticsRoot());

		for (Category category : categoryList) {
			// 카테고리별로 통계를 낸다.
			if (category.isUseRealTimePopularKeyword()) {
				try {
					String categoryId = category.getId();
					// 취합된 로그파일이 존재하는 디렉토리.
					File collectDir = environment.filePaths().getStatisticsRoot().file(categoryId, "collect", "rt");
					File targetDir = environment.filePaths().getStatisticsRoot().file(categoryId, "popular", "rt");
					if(!targetDir.exists()){
						targetDir.mkdirs();
					}

					File[] inFileList = collectDir.listFiles(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							try {
								return FilenameUtils.getExtension(name).equals("log");
							} catch (Exception e) {
								return false;
							}
						}
					});

					if(inFileList == null || inFileList.length == 0){
						logger.warn("[{}] Skip making realtime popular keyword due to no log files > {}", categoryId, inFileList);
						continue;
					}
					RealtimePopularKeywordGenerator g = new RealtimePopularKeywordGenerator(targetDir, inFileList, statisticsSettings, fileEncoding);
					// 카테고리별 실시간 인기키워드결과.
					List<RankKeyword> result = g.generate();

					logger.debug("-- REALTIME POPULAR [{}] --", category.getId());
					for (RankKeyword rankKeyword : result) {
						logger.debug("{}", rankKeyword);
					}
					logger.debug("--------------------");

					KeywordService keywordService = ServiceManager.getInstance().getService(KeywordService.class);
					MapperSession<PopularKeywordMapper> mapperSession = keywordService.getMapperSession(PopularKeywordMapper.class);
					try {
						// DB에 입력한다.
						PopularKeywordMapper mapper = mapperSession.getMapper();
						// 먼저 TIME = RECENT 에 1~10위 까지 업데이트함. 10개가 안될수 있으므로, Update를 사용.

						int LIMIT = 10;
						String TIME_REALTIME = "REALTIME";
						int size = result.size() < LIMIT ? result.size() : LIMIT; 
						for (int i = 0; i < size; i++) {
							RankKeyword rankKeyword = result.get(i);
							int rank = rankKeyword.getRank();
							
							PopularKeywordVO vo = mapper.getRankEntry(categoryId, TIME_REALTIME, rank);
							
							PopularKeywordVO newVo = new PopularKeywordVO(categoryId, TIME_REALTIME, rankKeyword.getKeyword(), 0, rank, rankKeyword.getRankDiffType(), rankKeyword.getRankDiff());
							if(vo == null){
								mapper.putEntry(newVo);
							}else{
								mapper.updateEntry(newVo);
							}
							// 히스토리성 데이터를 남김.
							newVo.setTime(timeFormatString);
							mapper.putEntry(newVo);
						}
						
						for(RankKeyword rankKeyword : result){
							PopularKeywordVO newVo = new PopularKeywordVO(categoryId, TIME_REALTIME, rankKeyword.getKeyword(), 0, rankKeyword.getRank(), rankKeyword.getRankDiffType(), rankKeyword.getRankDiff());
							// 히스토리성 데이터를 남김.
							newVo.setTime(timeFormatString);
							mapper.putEntry(newVo);
						}
						
						//TODO 구현필요.
						//mapper.deleteElderThan(categoryId, prevTimeFormatString);// 1시간이전 데이터 지움.
					} finally {
						if (mapperSession != null) {
							mapperSession.closeSession();
						}
					}

				} catch (Throwable e) {
					logger.error("[" + category.getId() + "] fail to generate realtime popular keyword", e);

				}

			}
		}

		// 카테고리별로 컴파일된 실시간 인기검색어 파일을 서비스노드에 전파하고 load시킨다.
		ApplyRealtimePopularKeywordJob applyJob = new ApplyRealtimePopularKeywordJob();
		ResultFuture resultFuture = jobExecutor.offer(applyJob);
		resultFuture.take();
		
		
		return new JobResult(true);
	}
}
