package org.fastcatsearch.statistics;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.fastcatsearch.settings.StatisticsSettings;
import org.fastcatsearch.settings.StatisticsSettings.PopularKeywordConfig;
import org.fastcatsearch.settings.StatisticsSettings.RealtimePopularKeywordConfig;
import org.fastcatsearch.settings.StatisticsSettings.RelateKeywordConfig;
import org.fastcatsearch.statistics.vo.RankKeyword;
import org.junit.Test;

public class RealtimePopularKeywordGeneratorTest {

	@Test
	public void test() throws IOException {
		String banwords = "무료배송, 쿠폰, 특별할인";
		File targetDir = new File("src/test/resources/statistics/rt");
		StatisticsSettings statisticsSettings = getStatisticsSettings(banwords, 1, 1, 1);
		String fileEncoding = "utf-8";
		
		File initDir = new File(targetDir, "init");
		File[] inFileList = initDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				try {
					return FilenameUtils.getExtension(name).equals("log");
				} catch (Exception e) {
					return false;
				}
			}
		});
		
		RealtimePopularKeywordGenerator g = new RealtimePopularKeywordGenerator(targetDir, inFileList, statisticsSettings, fileEncoding);
		
		List<RankKeyword> result = g.generate();
		for(RankKeyword keyword : result){
			System.out.println(keyword);
		}
		
	}
	
	@Test
	public void testRolling() throws IOException {
		File targetDir = new File("src/test/resources/statistics/rt/test");
		RealtimePopularKeywordGenerator g = new RealtimePopularKeywordGenerator(new File("."), null, null, "");
		g.rollingByNumber(targetDir, 7);
		new File(targetDir, "0.log").createNewFile();
	}
	public StatisticsSettings getStatisticsSettings(String banwords, int rtPopMinHit, int popMinHit, int relMinHit){
		RealtimePopularKeywordConfig realTimePopularKeywordConfig = new RealtimePopularKeywordConfig();
		PopularKeywordConfig popularKeywordConfig = new PopularKeywordConfig();
		RelateKeywordConfig relateKeywordConfig = new RelateKeywordConfig();
		realTimePopularKeywordConfig.setMinimumHitCount(rtPopMinHit);
		realTimePopularKeywordConfig.setRecentLogUsingCount(6);
		realTimePopularKeywordConfig.setTopCount(10);
		popularKeywordConfig.setMinimumHitCount(popMinHit);
		relateKeywordConfig.setMinimumHitCount(relMinHit);
		
		StatisticsSettings statisticsSettings = new StatisticsSettings();
		statisticsSettings.setBanwords(banwords);
		statisticsSettings.setRealTimePopularKeywordConfig(realTimePopularKeywordConfig);
		statisticsSettings.setPopularKeywordConfig(popularKeywordConfig);
		statisticsSettings.setRelateKeywordConfig(relateKeywordConfig);
		
		return statisticsSettings;
	}

}
