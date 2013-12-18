package org.fastcatsearch.job;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.additional.KeywordDictionary;
import org.fastcatsearch.additional.KeywordService;
import org.fastcatsearch.additional.PopularKeywordDictionary;
import org.fastcatsearch.additional.KeywordDictionary.KeywordDictionaryType;
import org.fastcatsearch.db.mapper.PopularKeywordMapper;
import org.fastcatsearch.db.vo.PopularKeywordVO;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.StaticticsSettings.Category;
import org.fastcatsearch.statistics.SearchStatisticsService;

public class KeywordDictionaryCompileApplyJob extends MasterNodeJob {

	@Override
	public JobResult doRun() throws FastcatSearchException {
		Map<String, String> args = getMapArgs();
		String dictionaryTypeStr = args.get("dictionaryType");
		
		KeywordDictionaryType dictionaryType = null;
		
		
		String fileName = null;
		
		String timeStr = null;
		
		SimpleDateFormat dateFormat = null;
		
		Calendar calendar = Calendar.getInstance();
		
		KeywordService keywordService = ServiceManager.getInstance().getService(KeywordService.class);
		
		SearchStatisticsService statisticsService = ServiceManager.getInstance().getService(SearchStatisticsService.class);
		
		List<Category> categoryList = statisticsService.staticticsSettings().getCategoryList();
		
		try {
			dictionaryType = KeywordDictionaryType.valueOf(dictionaryTypeStr);
			
			if(dictionaryType == KeywordDictionaryType.POPULAR_KEYWORD_REALTIME) {
				
				fileName = PopularKeywordDictionary.realTimeFileName;
				
				dateFormat = new SimpleDateFormat("yyyyMMddHH");
				
				calendar.add(Calendar.HOUR, -1);
				
				timeStr = "R" + dateFormat.format(calendar.getTime());
				
				compilePopularKeyword(keywordService, categoryList, fileName, timeStr);
				
			} else if(dictionaryType == KeywordDictionaryType.POPULAR_KEYWORD_DAY) {
				
				fileName = PopularKeywordDictionary.lastDayFileName;
				
				dateFormat = new SimpleDateFormat("yyyyMMdd");
				
				calendar.add(Calendar.DATE, -1);
				
				timeStr = "D" + dateFormat.format(calendar.getTime());
				
			} else if(dictionaryType == KeywordDictionaryType.POPULAR_KEYWORD_WEEK) {
				
				fileName = PopularKeywordDictionary.lastWeekFileName;
				
				dateFormat = new SimpleDateFormat("yyyyMMww");
				
				calendar.add(Calendar.WEEK_OF_YEAR, -1);
				
				timeStr = "W" + dateFormat.format(calendar.getTime());
				
			}
			
		} catch (IllegalArgumentException e) {
			return new JobResult("INVALID KEYWORD DICTIONARY TYPE >> "+dictionaryTypeStr);
		} catch (Exception e) {
			return new JobResult("KEYWORD DICTIONARY COMPILE ERROR >> "+e.getMessage());
		}
		
		return new JobResult(true);
	}
	
	private void compilePopularKeyword(KeywordService service, List<Category> categoryList,
			String fileName, String time) throws Exception {
		
		
		PopularKeywordMapper mapper = (PopularKeywordMapper) service
				.getMapperSession(PopularKeywordMapper.class);
		
		for(Category category : categoryList) {
		
			List<PopularKeywordVO> keywordList = mapper.getEntryList( category.getId(), time);
			
			PopularKeywordDictionary dictionary = new PopularKeywordDictionary(keywordList);
			
			File writeFile = service.getFile("popular", category.getId() + "_" + fileName);
			
			OutputStream ostream = null;
			
			try {
				
				ostream = new FileOutputStream(writeFile);
				
				dictionary.writeTo(ostream);
				
			} finally {
				
				if (ostream != null) try {
					ostream.close();
				} catch (IOException e) { }
			}
		}
	}

}
