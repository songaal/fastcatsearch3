package org.fastcatsearch.statistics;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.fastcatsearch.settings.StatisticsSettings;
import org.fastcatsearch.settings.StatisticsSettings.PopularKeywordConfig;
import org.fastcatsearch.settings.StatisticsSettings.RealTimePopularKeywordConfig;
import org.fastcatsearch.settings.StatisticsSettings.RelateKeywordConfig;
import org.junit.Test;

public class RealTimePopularKeywordGeneratorTest {

	@Test
	public void test() {
		String stopwords = "무료배송, 쿠폰, 특별할인";
		File targetDir = new File("src/test/resources/statistics/rt");
		File tmpDir = new File(targetDir, "tmp");
		StatisticsSettings statisticsSettings = getStatisticsSettings(stopwords, 1, 1, 1);
		String fileEncoding = "utf-8";
		
		RealTimePopularKeywordGenerator g = new RealTimePopularKeywordGenerator(tmpDir, targetDir, statisticsSettings, fileEncoding);
		
		g.generate();
	}
	
	@Test
	public void testRolling() throws IOException {
		File targetDir = new File("src/test/resources/statistics/rt/test");
		RealTimePopularKeywordGenerator g = new RealTimePopularKeywordGenerator();
		g.rollingByNumber(targetDir, 7);
		new File(targetDir, "0.log").createNewFile();
	}
	public StatisticsSettings getStatisticsSettings(String stopwords, int rtPopMinHit, int popMinHit, int relMinHit){
		RealTimePopularKeywordConfig realTimePopularKeywordConfig = new RealTimePopularKeywordConfig();
		PopularKeywordConfig popularKeywordConfig = new PopularKeywordConfig();
		RelateKeywordConfig relateKeywordConfig = new RelateKeywordConfig();
		realTimePopularKeywordConfig.setMinimumHitCount(rtPopMinHit);
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
