package org.fastcatsearch.statistics;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.search.SearchStatistics;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.settings.StaticticsSettings;
import org.fastcatsearch.settings.StaticticsSettings.Category;
import org.fastcatsearch.util.JAXBConfigs;

public class SearchStatisticsService extends AbstractService {

	private static final DummySearchStatistics fallBackSearchStatistics = new DummySearchStatistics();
	private File staticsticsHome;
	private SearchStatistics searchStatistics;
	private StaticticsSettings staticticsSettings;
	private Map<String, CategoryStatistics> categoryStatisticsMap;
	
	public SearchStatisticsService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
		staticsticsHome = environment.filePaths().file("staticstics");
		staticsticsHome.mkdir();
	}

	public Collection<CategoryStatistics> getCategoryStatisticsList(){
		return categoryStatisticsMap.values();
	}
	@Override
	protected boolean doStart() throws FastcatSearchException {
		categoryStatisticsMap = new HashMap<String, CategoryStatistics>();
		File staticticsSettingFile = environment.filePaths().configPath().path(SettingFileNames.statisticsConfig).file();
		try {
			staticticsSettings = JAXBConfigs.readConfig(staticticsSettingFile, StaticticsSettings.class);
		} catch (JAXBException e) {
			logger.error("staticticsSetting read error.", e);
			return false;
		}
		if(staticticsSettings == null){
			logger.error("Cannot load statictics setting file >> {}", staticticsSettingFile);
			return false;
		}
		
		List<Category> categoryList = staticticsSettings.getCategoryList();
		for(Category category : categoryList){
			String categoryId = category.getId();
			CategoryStatistics categoryStatistics = new CategoryStatistics(category, staticsticsHome);
			categoryStatisticsMap.put(categoryId, categoryStatistics);
		}
		
		searchStatistics = new SearchStatisticsImpl(staticsticsHome, categoryStatisticsMap);
		
		
		
		//TODO 마스터이면, stat db를 연다.
		
		
		
		return true;
	}

	@Override
	protected boolean doStop() throws FastcatSearchException {
		for(CategoryStatistics categoryStatistics : categoryStatisticsMap.values()){
			categoryStatistics.close();
		}
		return true;
	}

	@Override
	protected boolean doClose() throws FastcatSearchException {
		
		return true;
	}
	
	public SearchStatistics searchStatistics(){
		return searchStatistics != null ? searchStatistics : fallBackSearchStatistics;
	}
	
	
	private static class DummySearchStatistics implements SearchStatistics {

		@Override
		public void add(Query q) {
			//ignore.
		}
		
	}

}
