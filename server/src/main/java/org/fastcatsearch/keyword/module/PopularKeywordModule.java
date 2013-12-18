package org.fastcatsearch.keyword.module;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.env.Environment;
import org.fastcatsearch.keyword.KeywordService;
import org.fastcatsearch.keyword.PopularKeywordDictionary;
import org.fastcatsearch.keyword.KeywordDictionary.KeywordDictionaryType;
import org.fastcatsearch.module.AbstractModule;
import org.fastcatsearch.module.ModuleException;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.settings.StaticticsSettings.Category;

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
		for (Category category : categoryList) {
			String categoryId = category.getId();
			if (category.isUseRealTimePopularKeyword()) {
				loadAndSetDictionary(categoryId, KeywordDictionaryType.POPULAR_KEYWORD_REALTIME, getKeywordFile(categoryId, PopularKeywordDictionary.realTimeFileName));
			}
			if (category.isUsePopularKeyword()) {
				loadAndSetDictionary(categoryId, KeywordDictionaryType.POPULAR_KEYWORD_DAY, getKeywordFile(categoryId, PopularKeywordDictionary.lastDayFileName));
				loadAndSetDictionary(categoryId, KeywordDictionaryType.POPULAR_KEYWORD_WEEK, getKeywordFile(categoryId, PopularKeywordDictionary.lastWeekFileName));
			}
		}

		return true;
	}

	private File getKeywordFile(String categoryId, String filename){
		return new File(home, categoryId + "." + PopularKeywordDictionary.realTimeFileName);
	}
	private void loadAndSetDictionary(String categoryId, KeywordDictionaryType type, File dictionaryFile) {
		if (dictionaryFile.exists()) {
			try {
				PopularKeywordDictionary keywordDictionary = new PopularKeywordDictionary(dictionaryFile);
				putPopularKeywordDictionary(categoryId, type, keywordDictionary);
			} catch (Exception e) {
				logger.error("error loading popular keyword > " + dictionaryFile.getAbsolutePath(), e);
			}

		} else {
			logger.warn("Cannot find keyword dictionary file. > {}", dictionaryFile.getAbsolutePath());
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
		Map<KeywordDictionaryType, PopularKeywordDictionary> map = categoryKeywordDictionaryMap.get(categoryId);
		if(map == null){
			return null;
		}else{
			return map.get(key);
		}
	}

	private void putPopularKeywordDictionary(String categoryId, KeywordDictionaryType key, PopularKeywordDictionary value) {
		Map<KeywordDictionaryType, PopularKeywordDictionary> map = categoryKeywordDictionaryMap.get(categoryId);
		if(map == null){
			map = new HashMap<KeywordDictionaryType, PopularKeywordDictionary>();
			categoryKeywordDictionaryMap.put(categoryId, map);
		}
		map.put(key, value);
	}

	private void removePopularKeywordDictionary(String categoryId, KeywordDictionaryType key) {
		Map<KeywordDictionaryType, PopularKeywordDictionary> map = categoryKeywordDictionaryMap.get(categoryId);
		if(map != null){
			map.remove(key);
		}
	}

}
