package org.fastcatsearch.statistics;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.fastcatsearch.settings.StatisticsSettings;
import org.fastcatsearch.settings.StatisticsSettings.PopularKeywordConfig;
import org.fastcatsearch.settings.StatisticsSettings.RealTimePopularKeywordConfig;
import org.fastcatsearch.settings.StatisticsSettings.RelateKeywordConfig;
import org.fastcatsearch.statistics.vo.RankKeyword;
import org.junit.Test;

public class RealTimePopularKeywordGeneratorTest {

	@Test
	public void test() throws IOException {
		String stopwords = "무료배송, 쿠폰, 특별할인";
		File targetDir = new File("src/test/resources/statistics/rt");
		StatisticsSettings statisticsSettings = getStatisticsSettings(stopwords, 1, 1, 1);
		String fileEncoding = "utf-8";
		
		RealTimePopularKeywordGenerator g = new RealTimePopularKeywordGenerator(targetDir, statisticsSettings, fileEncoding);
		
		List<RankKeyword> result = g.generate();
		for(RankKeyword keyword : result){
			System.out.println(keyword);
		}
		
	}
	
	@Test
	public void testRolling() throws IOException {
		File targetDir = new File("src/test/resources/statistics/rt/test");
		RealTimePopularKeywordGenerator g = new RealTimePopularKeywordGenerator(new File("."), null, "");
		g.rollingByNumber(targetDir, 7);
		new File(targetDir, "0.log").createNewFile();
	}
	public StatisticsSettings getStatisticsSettings(String stopwords, int rtPopMinHit, int popMinHit, int relMinHit){
		RealTimePopularKeywordConfig realTimePopularKeywordConfig = new RealTimePopularKeywordConfig();
		PopularKeywordConfig popularKeywordConfig = new PopularKeywordConfig();
		RelateKeywordConfig relateKeywordConfig = new RelateKeywordConfig();
		realTimePopularKeywordConfig.setMinimumHitCount(rtPopMinHit);
		realTimePopularKeywordConfig.setRecentLogUsingCount(6);
		realTimePopularKeywordConfig.setTopCount(10);
		popularKeywordConfig.setMinimumHitCount(popMinHit);
		relateKeywordConfig.setMinimumHitCount(relMinHit);
		
		StatisticsSettings statisticsSettings = new StatisticsSettings();
		statisticsSettings.setStopwords(stopwords);
		statisticsSettings.setRealTimePopularKeywordConfig(realTimePopularKeywordConfig);
		statisticsSettings.setPopularKeywordConfig(popularKeywordConfig);
		statisticsSettings.setRelateKeywordConfig(relateKeywordConfig);
		
		return statisticsSettings;
	}

}
