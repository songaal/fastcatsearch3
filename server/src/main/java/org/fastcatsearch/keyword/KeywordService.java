package org.fastcatsearch.keyword;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.db.AbstractDBService;
import org.fastcatsearch.db.mapper.ADKeywordMapper;
import org.fastcatsearch.db.mapper.KeywordSuggestionMapper;
import org.fastcatsearch.db.mapper.ManagedMapper;
import org.fastcatsearch.db.mapper.PopularKeywordMapper;
import org.fastcatsearch.db.mapper.RelateKeywordMapper;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.job.statistics.CollectSearchStatisticsLogsJob;
import org.fastcatsearch.job.statistics.MakePopularKeywordJob;
import org.fastcatsearch.job.statistics.MakeRealtimePopularKeywordJob;
import org.fastcatsearch.keyword.KeywordDictionary.KeywordDictionaryType;
import org.fastcatsearch.keyword.module.PopularKeywordModule;
import org.fastcatsearch.keyword.module.RelateKeywordModule;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.KeywordServiceSettings;
import org.fastcatsearch.settings.KeywordServiceSettings.KeywordServiceCategory;
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.util.DateUtils;
import org.fastcatsearch.util.JAXBConfigs;

/**
 * 인기키워드 등의 키워드서비스를 제공한다.
 * */
public class KeywordService extends AbstractDBService {

	private KeywordServiceSettings keywordServiceSettings;

	private boolean isMaster;

	private PopularKeywordModule popularKeywordModule;
	private RelateKeywordModule relateKeywordModule;

	private File moduleHome;

	private static Class<?>[] mapperList = new Class<?>[] { PopularKeywordMapper.class, RelateKeywordMapper.class, KeywordSuggestionMapper.class, ADKeywordMapper.class };

	public KeywordService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super("db/keyword", KeywordService.mapperList, environment, settings, serviceManager);

		moduleHome = environment.filePaths().getKeywordsRoot().file();
		popularKeywordModule = new PopularKeywordModule(moduleHome, environment, settings);
		relateKeywordModule = new RelateKeywordModule(moduleHome, environment, settings);
	}

	public File getFile(String categoryId, KeywordDictionaryType type) {
		return getFile(categoryId, type, 1);
	}

	public File getFile(String categoryId, KeywordDictionaryType type, int interval) {

		if (type == KeywordDictionaryType.POPULAR_KEYWORD_REALTIME || type == KeywordDictionaryType.POPULAR_KEYWORD_DAY || type == KeywordDictionaryType.POPULAR_KEYWORD_WEEK
				|| type == KeywordDictionaryType.POPULAR_KEYWORD_MONTH) {
			return popularKeywordModule.getDictionaryFile(categoryId, type, interval);
		} else if (type == KeywordDictionaryType.RELATE_KEYWORD) {
			return relateKeywordModule.getDictionaryFile(categoryId);
		} else {
			// TODO ad keyword, keyword suggestion
		}

		return null;
	}

	public KeywordServiceSettings keywordServiceSettings() {
		return keywordServiceSettings;
	}

	@Override
	protected boolean doStart() throws FastcatSearchException {

		File keywordServiceConfigFile = environment.filePaths().configPath().path(SettingFileNames.keywordServiceConfig).file();
		try {
			keywordServiceSettings = JAXBConfigs.readConfig(keywordServiceConfigFile, KeywordServiceSettings.class);
		} catch (JAXBException e) {
			logger.error("KeywordService setting file read error.", e);
			return false;
		}
		if (keywordServiceSettings == null) {
			logger.error("Cannot load KeywordService setting file >> {}", keywordServiceSettings);
			return false;
		}

		boolean isServiceNode = keywordServiceSettings.getServiceNodeList().contains(environment.myNodeId());
		isMaster = environment.isMasterNode();
		// /서비스 노드나 ,마스터 노드가 아니면 서비스를 시작하지 않는다.
		if (!isServiceNode && !isMaster) {
			logger.info("This node does not provide KeywordService.");
			return false;
		}

		// 키워드 서비스노드이면..
		logger.info("This node provides KeywordService. isMaster > {}", isMaster);

		// 모듈 로딩.
		loadKeywordModules();

		// 마스터 노드만 통계를 낸다.
		if (isMaster) {
			// 수집 스케쥴을 건다.
			// Realtime 정시에서 시작하여 5분단위.
			Calendar calendar = DateUtils.getLatestTimeLargerThanNow(5);
			calendar.add(Calendar.MINUTE, 2); // +2분 여유.
			Date nextTimeForRealtimeLog = calendar.getTime();
			JobService.getInstance().schedule(new MakeRealtimePopularKeywordJob(), nextTimeForRealtimeLog, DateUtils.getSecondsByMinutes(5)); // 5분주기.
			// Daily 매 정시기준으로 1일 단위.
			calendar = DateUtils.getNextDayHour(0); // 다음날 0시.
			calendar.add(Calendar.MINUTE, 2); // +2분 여유.
			Date nextTimeForDailyLog = calendar.getTime();
			JobService.getInstance().schedule(new MakePopularKeywordJob(), nextTimeForDailyLog, DateUtils.getSecondsByDays(1)); // 1일
		}

		if (isMaster) {
			// 마스터서버이면, 자동완성, 연관키워드, 인기검색어 등의 db를 연다.
			return super.doStart();
		} else {

			return true;
		}

	}

	public void loadPopularKeywordDictionary(String categoryId, KeywordDictionaryType type, int interval) throws IOException {
		popularKeywordModule.loadAndSetDictionary(categoryId, type, interval);
	}

	public void loadRelateKeywordDictionary(String categoryId) throws IOException {
		relateKeywordModule.loadAndSetDictionary(categoryId);
	}

	private void loadKeywordModules() {
		List<KeywordServiceCategory> categoryList = keywordServiceSettings.getCategoryList();
		popularKeywordModule.setCategoryList(categoryList);
		popularKeywordModule.load();
		relateKeywordModule.setCategoryList(categoryList);
		relateKeywordModule.load();
	}

	private void unloadKeywordModules() {
		popularKeywordModule.unload();
		relateKeywordModule.unload();
	}

	@Override
	protected boolean doStop() throws FastcatSearchException {

		unloadKeywordModules();

		if (isMaster) {
			return super.doStop();
		} else {

			return true;
		}
	}

	@Override
	protected boolean doClose() throws FastcatSearchException {
		if (isMaster) {
			return super.doClose();
		} else {

			return true;
		}
	}

	public KeywordDictionary getKeywordDictionary(String categoryId, KeywordDictionaryType key) {
		return getKeywordDictionary(categoryId, key, 1);
	}

	public KeywordDictionary getKeywordDictionary(String categoryId, KeywordDictionaryType key, int interval) {
		if (key == KeywordDictionaryType.POPULAR_KEYWORD_REALTIME || key == KeywordDictionaryType.POPULAR_KEYWORD_DAY || key == KeywordDictionaryType.POPULAR_KEYWORD_WEEK
				|| key == KeywordDictionaryType.POPULAR_KEYWORD_MONTH) {
			return popularKeywordModule.getKeywordDictionary(categoryId, key, interval);
		} else if (key == KeywordDictionaryType.RELATE_KEYWORD) {
			return relateKeywordModule.getKeywordDictionary(categoryId);
		} else {
			// TODO ad keyword, keyword suggestion
		}
		return null;
	}

	public KeywordDictionary getKeywordDictionary(KeywordDictionaryType key) {
		return getKeywordDictionary(null, key);
	}

	@Override
	protected void initMapper(ManagedMapper managedMapper) throws Exception {
		// do nothing
	}

}
