package org.fastcatsearch.additional.module;

import java.io.File;

import org.fastcatsearch.additional.KeywordService;
import org.fastcatsearch.additional.PopularKeywordDictionary;
import org.fastcatsearch.additional.RelateKeywordDictionary;
import org.fastcatsearch.additional.KeywordDictionary.KeywordDictionaryType;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.module.AbstractModule;
import org.fastcatsearch.module.ModuleException;
import org.fastcatsearch.settings.Settings;

public class RelateKeywordModule extends AbstractModule {
	
	private KeywordService keywordService;
	private File home;
	
	public RelateKeywordModule(File moduleHome, KeywordService keywordService, Environment environment, Settings settings) {
		super(environment, settings);
		this.keywordService = keywordService;
		home = new File(moduleHome, "relateKeyword");
	}

	@Override
	protected boolean doLoad() throws ModuleException {
		
		File dictionaryFile = new File(home, RelateKeywordDictionary.fileName);
		if (dictionaryFile.exists()) {
			try {
				PopularKeywordDictionary keywordDictionary = new PopularKeywordDictionary(dictionaryFile);
				keywordService.putKeywordDictionary(KeywordDictionaryType.RELATE_KEYWORD, keywordDictionary);
			} catch (Exception e) {
				logger.error("error loading relate keyword > " + dictionaryFile.getAbsolutePath(), e);
			}

		}
		
		
		return true;
	}

	@Override
	protected boolean doUnload() throws ModuleException {
		keywordService.removeKeywordDictionary(KeywordDictionaryType.RELATE_KEYWORD);
		return true;
	}

}
