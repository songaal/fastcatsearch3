package org.fastcatsearch.additional.module;

import java.io.File;

import org.fastcatsearch.additional.KeywordDictionary.KeywordDictionaryType;
import org.fastcatsearch.additional.KeywordService;
import org.fastcatsearch.additional.PopularKeywordDictionary;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.module.AbstractModule;
import org.fastcatsearch.module.ModuleException;
import org.fastcatsearch.settings.Settings;

public class PopularKeywordModule extends AbstractModule {

	private KeywordService keywordService;

	private File home;
	
	public PopularKeywordModule(File moduleHome, KeywordService keywordService, Environment environment, Settings settings) {
		super(environment, settings);
		this.keywordService = keywordService;
		home = new File(moduleHome, "popularKeyword");
	}

	@Override
	protected boolean doLoad() throws ModuleException {

		File realTimeFile = new File(home, PopularKeywordDictionary.realTimeFileName);
		loadAndSetDictionary(KeywordDictionaryType.POPULAR_KEYWORD_REALTIME, realTimeFile);
		File lastDayFile = new File(home, PopularKeywordDictionary.lastDayFileName);
		loadAndSetDictionary(KeywordDictionaryType.POPULAR_KEYWORD_DAY, lastDayFile);
		File lastWeekFile = new File(home, PopularKeywordDictionary.lastWeekFileName);
		loadAndSetDictionary(KeywordDictionaryType.POPULAR_KEYWORD_WEEK, lastWeekFile);

		return true;
	}

	private void loadAndSetDictionary(KeywordDictionaryType type, File dictionaryFile) {
		if (dictionaryFile.exists()) {
			try {
				PopularKeywordDictionary keywordDictionary = new PopularKeywordDictionary(dictionaryFile);
				keywordService.putKeywordDictionary(type, keywordDictionary);
			} catch (Exception e) {
				logger.error("error loading popular keyword > " + dictionaryFile.getAbsolutePath(), e);
			}

		}
	}

	@Override
	protected boolean doUnload() throws ModuleException {
		keywordService.removeKeywordDictionary(KeywordDictionaryType.POPULAR_KEYWORD_REALTIME);
		keywordService.removeKeywordDictionary(KeywordDictionaryType.POPULAR_KEYWORD_DAY);
		keywordService.removeKeywordDictionary(KeywordDictionaryType.POPULAR_KEYWORD_WEEK);
		return true;
	}

}
