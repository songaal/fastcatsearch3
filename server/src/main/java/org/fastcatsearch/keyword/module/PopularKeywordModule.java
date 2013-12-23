package org.fastcatsearch.keyword.module;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.db.vo.PopularKeywordVO;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.keyword.KeywordDictionary.KeywordDictionaryType;
import org.fastcatsearch.keyword.PopularKeywordDictionary;
import org.fastcatsearch.module.AbstractModule;
import org.fastcatsearch.module.ModuleException;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.settings.StatisticsSettings.Category;

public class PopularKeywordModule extends AbstractModule {

	private File home;
	private List<Category> categoryList;

	private Map<String, Map<KeywordDictionaryType, PopularKeywordDictionary>> categoryKeywordDictionaryMap;
	
	public PopularKeywordModule(File moduleHome, Environment environment, Settings settings) {
		super(environment, settings);
		home = new File(moduleHome, "popularKeyword");
	}

	@Override
	protected boolean doLoad() throws ModuleException {
		categoryKeywordDictionaryMap = new HashMap<String, Map<KeywordDictionaryType, PopularKeywordDictionary>>();
		////
		Map<KeywordDictionaryType, PopularKeywordDictionary> map = new HashMap<KeywordDictionaryType, PopularKeywordDictionary>();
		int rank = 1;
		List<PopularKeywordVO> list = new ArrayList<PopularKeywordVO>();
		PopularKeywordDictionary dict = new PopularKeywordDictionary(list);
		categoryKeywordDictionaryMap.put("total", map);
		map.put(KeywordDictionaryType.POPULAR_KEYWORD_DAY, dict);
		
		
		
		///
		for (Category category : categoryList) {
			String categoryId = category.getId();
			if (category.isUseRealTimePopularKeyword()) {
				loadAndSetDictionary(categoryId, KeywordDictionaryType.POPULAR_KEYWORD_REALTIME);
			}
			if (category.isUsePopularKeyword()) {
				loadAndSetDictionary(categoryId, KeywordDictionaryType.POPULAR_KEYWORD_DAY);
				loadAndSetDictionary(categoryId, KeywordDictionaryType.POPULAR_KEYWORD_WEEK);
			}
		}

		return true;
	}

	public File getDictionaryFile(String categoryId, KeywordDictionaryType type){
		String filename = null;
		if(type == KeywordDictionaryType.POPULAR_KEYWORD_REALTIME){
			filename = PopularKeywordDictionary.realTimeFileName;
		}else if(type == KeywordDictionaryType.POPULAR_KEYWORD_DAY){
			filename = PopularKeywordDictionary.lastDayFileName;
		}else if(type == KeywordDictionaryType.POPULAR_KEYWORD_WEEK){
			filename = PopularKeywordDictionary.lastWeekFileName;
		}
		return new File(home, categoryId + "." + filename);
	}
	
	private void loadAndSetDictionary(String categoryId, KeywordDictionaryType type) {
		File dictionaryFile = getDictionaryFile(categoryId, type);
		try {
			PopularKeywordDictionary keywordDictionary = new PopularKeywordDictionary(dictionaryFile);
			putPopularKeywordDictionary(categoryId, type, keywordDictionary);
		} catch (Exception e) {
			logger.error("error loading popular keyword > " + dictionaryFile.getAbsolutePath(), e);
			putPopularKeywordDictionary(categoryId, type, new PopularKeywordDictionary());
		}

	}

	@Override
	protected boolean doUnload() throws ModuleException {
		categoryKeywordDictionaryMap.clear();
		return true;
	}

	public void setCategoryList(List<Category> categoryList) {
		this.categoryList = categoryList;
	}
	
	public PopularKeywordDictionary getKeywordDictionary(String categoryId, KeywordDictionaryType key){
		categoryId = categoryId.toUpperCase();
		Map<KeywordDictionaryType, PopularKeywordDictionary> map = categoryKeywordDictionaryMap.get(categoryId);
		if(map == null){
			return null;
		}else{
			return map.get(key);
		}
	}

	private void putPopularKeywordDictionary(String categoryId, KeywordDictionaryType key, PopularKeywordDictionary value) {
		categoryId = categoryId.toUpperCase();
		Map<KeywordDictionaryType, PopularKeywordDictionary> map = categoryKeywordDictionaryMap.get(categoryId);
		if(map == null){
			map = new HashMap<KeywordDictionaryType, PopularKeywordDictionary>();
			categoryKeywordDictionaryMap.put(categoryId, map);
		}
		map.put(key, value);
	}

	private void removePopularKeywordDictionary(String categoryId, KeywordDictionaryType key) {
		categoryId = categoryId.toUpperCase();
		Map<KeywordDictionaryType, PopularKeywordDictionary> map = categoryKeywordDictionaryMap.get(categoryId);
		if(map != null){
			map.remove(key);
		}
	}

}
