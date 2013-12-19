package org.fastcatsearch.keyword.module;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.env.Environment;
import org.fastcatsearch.keyword.PopularKeywordDictionary;
import org.fastcatsearch.keyword.RelateKeywordDictionary;
import org.fastcatsearch.module.AbstractModule;
import org.fastcatsearch.module.ModuleException;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.settings.StatisticsSettings.Category;

public class RelateKeywordModule extends AbstractModule {
	
	private File home;
	private List<Category> categoryList;
	
	private Map<String, RelateKeywordDictionary> categoryKeywordDictionaryMap;
	
	public RelateKeywordModule(File moduleHome, Environment environment, Settings settings) {
		super(environment, settings);
		home = new File(moduleHome, "relateKeyword");
	}

	@Override
	protected boolean doLoad() throws ModuleException {
		categoryKeywordDictionaryMap = new HashMap<String, RelateKeywordDictionary>();
		for (Category category : categoryList) {
			String categoryId = category.getId();
			File dictionaryFile = getDictionaryFile(categoryId);
			try {
				RelateKeywordDictionary keywordDictionary = new RelateKeywordDictionary(dictionaryFile);
				putRelateKeywordDictionary(categoryId, keywordDictionary);
			} catch (Exception e) {
				logger.error("error loading relate keyword > " + dictionaryFile.getAbsolutePath(), e);
				putRelateKeywordDictionary(categoryId, new RelateKeywordDictionary());
			}

		}
		
		return true;
	}
	
	public File getDictionaryFile(String categoryId) {
		return new File(home, categoryId + "." + PopularKeywordDictionary.realTimeFileName);
	}
	
	@Override
	protected boolean doUnload() throws ModuleException {
		categoryKeywordDictionaryMap.clear();
		return true;
	}

	public void setCategoryList(List<Category> categoryList) {
		this.categoryList = categoryList;
	}

	public RelateKeywordDictionary getKeywordDictionary(String categoryId){
		RelateKeywordDictionary dictionary = categoryKeywordDictionaryMap.get(categoryId);
		return dictionary;
	}

	private void putRelateKeywordDictionary(String categoryId, RelateKeywordDictionary value) {
		categoryKeywordDictionaryMap.put(categoryId, value);
	}

	private void removeRelateKeywordDictionary(String categoryId) {
		categoryKeywordDictionaryMap.remove(categoryId);
		
	}

	
}
