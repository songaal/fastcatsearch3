package org.fastcatsearch.keyword.module;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.env.Environment;
import org.fastcatsearch.keyword.KeywordDictionary;
import org.fastcatsearch.keyword.KeywordDictionary.KeywordDictionaryType;
import org.fastcatsearch.keyword.PopularKeywordDictionary;
import org.fastcatsearch.module.AbstractModule;
import org.fastcatsearch.module.ModuleException;
import org.fastcatsearch.settings.KeywordServiceSettings.KeywordServiceCategory;
import org.fastcatsearch.settings.Settings;

public class PopularKeywordModule extends AbstractModule {

	private File home;
	private List<KeywordServiceCategory> categoryList;

	private Map<String, Map<String, PopularKeywordDictionary>> categoryKeywordDictionaryMap;

	public PopularKeywordModule(File moduleHome, Environment environment, Settings settings) {
		super(environment, settings);
		home = moduleHome;
	}

	public void setCategoryList(List<KeywordServiceCategory> categoryList) {
		this.categoryList = categoryList;
	}

	@Override
	protected boolean doLoad() throws ModuleException {
		categoryKeywordDictionaryMap = new HashMap<String, Map<String, PopularKeywordDictionary>>();

		for (KeywordServiceCategory category : categoryList) {
			String categoryId = category.getId();
			if (category.isServiceRealTimePopularKeyword()) {
				try {
					loadAndSetDictionary(categoryId, KeywordDictionaryType.POPULAR_KEYWORD_REALTIME, 1);
				} catch (IOException ignore) {
				}
			}
			if (category.isServicePopularKeyword()) {
				String serviceTypes = category.getPopularKeywordServiceType();

				// 서비스하는 타입만 올린다.
				String[] serviceTypeList = serviceTypes.split(",");
				for (String serviceType : serviceTypeList) {
					serviceType = serviceType.trim();
					int interval = 1;
					interval = Integer.parseInt(serviceType.substring(0, serviceType.length() - 1));
					/*
					 * 중요! interval 이하로 1까지 로딩한다. 1일전, 2일전 ,3일전 등을 로딩할수 있다.
					 */
					for (int i = interval; i > 0; i--) {
						try {
							if (serviceType.endsWith("D")) {
								loadAndSetDictionary(categoryId, KeywordDictionaryType.POPULAR_KEYWORD_DAY, i);
							} else if (serviceType.endsWith("W")) {
								loadAndSetDictionary(categoryId, KeywordDictionaryType.POPULAR_KEYWORD_WEEK, i);
							} else if (serviceType.endsWith("M")) {
								loadAndSetDictionary(categoryId, KeywordDictionaryType.POPULAR_KEYWORD_MONTH, i);
							}
						} catch (IOException ignore) {
						}
					}
				}
			}
		}

		return true;
	}

	public File getDictionaryFile(String categoryId, KeywordDictionaryType type, int interval) {
		String filename = null;
		if (type == KeywordDictionaryType.POPULAR_KEYWORD_REALTIME) {
			filename = PopularKeywordDictionary.realTimeFileName;
		} else if (type == KeywordDictionaryType.POPULAR_KEYWORD_DAY) {
			filename = PopularKeywordDictionary.dailyFileName + "." + interval;
		} else if (type == KeywordDictionaryType.POPULAR_KEYWORD_WEEK) {
			filename = PopularKeywordDictionary.weeklyFileName + "." + interval;
		} else if (type == KeywordDictionaryType.POPULAR_KEYWORD_MONTH) {
			filename = PopularKeywordDictionary.monthlyFileName + "." + interval;
		}
		File dictHome = new File(home, categoryId);
		return new File(dictHome, filename + KeywordDictionary.extension);
	}

	public void loadAndSetDictionary(String categoryId, KeywordDictionaryType type, int interval) throws IOException {
		File dictionaryFile = getDictionaryFile(categoryId, type, interval);
		try {
			PopularKeywordDictionary keywordDictionary = new PopularKeywordDictionary(dictionaryFile);
			putPopularKeywordDictionary(categoryId, type, interval, keywordDictionary);
			logger.info("Load Popular keyword dictionary {}:{}:{} > {}", categoryId, type, interval, dictionaryFile);
		} catch (IOException e) {
			logger.error("error loading popular keyword > {}", dictionaryFile.getAbsolutePath());
			throw e;
		}

	}

	@Override
	protected boolean doUnload() throws ModuleException {
		categoryKeywordDictionaryMap.clear();
		return true;
	}

	public PopularKeywordDictionary getKeywordDictionary(String categoryId, KeywordDictionaryType type, int interval) {
		categoryId = categoryId.toUpperCase();
		Map<String, PopularKeywordDictionary> map = categoryKeywordDictionaryMap.get(categoryId);
		if (map == null) {
			return null;
		} else {
			String key = null;
			if (type != KeywordDictionaryType.POPULAR_KEYWORD_REALTIME) {
				key = type.name();
			} else {
				key = type.name() + "-" + interval;
			}
			return map.get(key);
		}
	}

	private void putPopularKeywordDictionary(String categoryId, KeywordDictionaryType type, int interval, PopularKeywordDictionary value) {
		categoryId = categoryId.toUpperCase();
		Map<String, PopularKeywordDictionary> map = categoryKeywordDictionaryMap.get(categoryId);
		if (map == null) {
			map = new HashMap<String, PopularKeywordDictionary>();
			categoryKeywordDictionaryMap.put(categoryId, map);
		}
		String key = null;
		if (type != KeywordDictionaryType.POPULAR_KEYWORD_REALTIME) {
			key = type.name();
		} else {
			key = type.name() + "-" + interval;
		}
		map.put(key, value);
	}

	private void removePopularKeywordDictionary(String categoryId, KeywordDictionaryType type, int interval) {
		categoryId = categoryId.toUpperCase();
		Map<String, PopularKeywordDictionary> map = categoryKeywordDictionaryMap.get(categoryId);

		if (map != null) {
			String key = null;
			if (type != KeywordDictionaryType.POPULAR_KEYWORD_REALTIME) {
				key = type.name();
			} else {
				key = type.name() + "-" + interval;
			}
			map.remove(key);
		}
	}

}
