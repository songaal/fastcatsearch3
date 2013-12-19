package org.fastcatsearch.job;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.db.mapper.PopularKeywordMapper;
import org.fastcatsearch.db.mapper.RelateKeywordMapper;
import org.fastcatsearch.db.vo.PopularKeywordVO;
import org.fastcatsearch.db.vo.RelateKeywordVO;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.keyword.KeywordDictionary.KeywordDictionaryType;
import org.fastcatsearch.keyword.KeywordService;
import org.fastcatsearch.keyword.PopularKeywordDictionary;
import org.fastcatsearch.keyword.RelateKeywordDictionary;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.StatisticsSettings.Category;
import org.fastcatsearch.statistics.SearchStatisticsService;

public class KeywordDictionaryCompileApplyJob extends MasterNodeJob {

	private static final long serialVersionUID = 5101762691161535526L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		Map<String, String> args = getMapArgs();
		String dictionaryTypeStr = args.get("dictionaryType");
		
		KeywordDictionaryType dictionaryType = null;
		
		String timeStr = null;
		
		SimpleDateFormat dateFormat = null;
		
		Calendar calendar = Calendar.getInstance();
		
		KeywordService keywordService = ServiceManager.getInstance().getService(KeywordService.class);
		
		SearchStatisticsService statisticsService = ServiceManager.getInstance().getService(SearchStatisticsService.class);
		
		List<Category> categoryList = statisticsService.statisticsSettings().getCategoryList();
		
		try {
			dictionaryType = KeywordDictionaryType.valueOf(dictionaryTypeStr);
			
			if(dictionaryType == KeywordDictionaryType.POPULAR_KEYWORD_REALTIME) {
				
				dateFormat = new SimpleDateFormat("yyyyMMddHH");
				
				calendar.add(Calendar.HOUR, -1);
				
				timeStr = "R" + dateFormat.format(calendar.getTime());
				
				compilePopularKeyword(keywordService, categoryList, dictionaryType, timeStr);
				
			} else if(dictionaryType == KeywordDictionaryType.POPULAR_KEYWORD_DAY) {
				
				dateFormat = new SimpleDateFormat("yyyyMMdd");
				
				calendar.add(Calendar.DATE, -1);
				
				timeStr = "D" + dateFormat.format(calendar.getTime());
				
				compilePopularKeyword(keywordService, categoryList, dictionaryType, timeStr);
				
			} else if(dictionaryType == KeywordDictionaryType.POPULAR_KEYWORD_WEEK) {
				
				dateFormat = new SimpleDateFormat("yyyyMMww");
				
				calendar.add(Calendar.WEEK_OF_YEAR, -1);
				
				timeStr = "W" + dateFormat.format(calendar.getTime());
				
				compilePopularKeyword(keywordService, categoryList, dictionaryType, timeStr);
				
			} else  if(dictionaryType == KeywordDictionaryType.RELATE_KEYWORD) {
				
				compileRelateKeyword(keywordService, categoryList, dictionaryType);
			}
			
		} catch (IllegalArgumentException e) {
			logger.error("",e);
			return new JobResult("INVALID KEYWORD DICTIONARY TYPE >> "+dictionaryTypeStr);
		} catch (Exception e) {
			logger.error("",e);
			return new JobResult("KEYWORD DICTIONARY COMPILE ERROR >> "+e.getMessage());
		}
		
		return new JobResult(true);
	}
	
	private void compilePopularKeyword(KeywordService service, List<Category> categoryList,
			KeywordDictionaryType type, String time) throws Exception {
		
		PopularKeywordMapper mapper = service.getMapperSession(PopularKeywordMapper.class).getMapper();
		
		for(Category category : categoryList) {
		
			List<PopularKeywordVO> keywordList = mapper.getEntryList( category.getId(), time);
			
			PopularKeywordDictionary dictionary = new PopularKeywordDictionary(keywordList);
			
			File writeFile = service.getFile(category.getId(), type);
			
			File parentDir = writeFile.getParentFile();
			
			if(!parentDir.exists()) {
				FileUtils.forceMkdir(parentDir);
			}
			
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
	
	private void compileRelateKeyword(KeywordService service, List<Category> categoryList, 
			KeywordDictionaryType type) throws Exception {
		
		RelateKeywordMapper mapper = service
				.getMapperSession(RelateKeywordMapper.class).getMapper();
		
		
		for(Category category : categoryList) {
			
			List<RelateKeywordVO> keywordList = mapper.getEntryList(category.getId());
			
			RelateKeywordDictionary dictionary = new RelateKeywordDictionary();
			
			for(RelateKeywordVO keyword : keywordList) {
				dictionary.putRelateKeyword(keyword.getKeyword(), keyword.getValue());
			}
			
			File writeFile = service.getFile(category.getId(), type);
			
			File parentDir = writeFile.getParentFile();
			
			if(!parentDir.exists()) {
				FileUtils.forceMkdir(parentDir);
			}
			
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
