package org.fastcatsearch.statistics;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.fastcatsearch.settings.StaticticsSettings;
import org.fastcatsearch.settings.StaticticsSettings.PopularKeywordConfig;
import org.fastcatsearch.settings.StaticticsSettings.RealTimePopularKeywordConfig;
import org.fastcatsearch.settings.StaticticsSettings.RelateKeywordConfig;
import org.junit.Test;

public class RealTimePopularKeywordGeneratorTest {

	@Test
	public void test() {
		String stopwords = "무료배송, 쿠폰, 특별할인";
		File targetDir = new File("src/test/resources/statistics/rt");
		File tmpDir = new File(targetDir, "tmp");
		StaticticsSettings staticticsSettings = getStaticticsSettings(stopwords, 1, 1, 1);
		
		RealTimePopularKeywordGenerator g = new RealTimePopularKeywordGenerator(tmpDir, targetDir, staticticsSettings);
		
		g.generate();
	}
	
	@Test
	public void testRolling() throws IOException {
		File targetDir = new File("src/test/resources/statistics/rt/test");
		RealTimePopularKeywordGenerator g = new RealTimePopularKeywordGenerator();
		g.rollingByNumber(targetDir, 7);
		new File(targetDir, "0.log").createNewFile();
	}
	public StaticticsSettings getStaticticsSettings(String stopwords, int rtPopMinHit, int popMinHit, int relMinHit){
		RealTimePopularKeywordConfig realTimePopularKeywordConfig = new RealTimePopularKeywordConfig();
		PopularKeywordConfig popularKeywordConfig = new PopularKeywordConfig();
		RelateKeywordConfig relateKeywordConfig = new RelateKeywordConfig();
		realTimePopularKeywordConfig.setMinimumHitCount(rtPopMinHit);
		popularKeywordConfig.setMinimumHitCount(popMinHit);
		relateKeywordConfig.setMinimumHitCount(relMinHit);
		
		StaticticsSettings staticticsSettings = new StaticticsSettings();
		staticticsSettings.setStopwords(stopwords);
		staticticsSettings.setRealTimePopularKeywordConfig(realTimePopularKeywordConfig);
		staticticsSettings.setPopularKeywordConfig(popularKeywordConfig);
		staticticsSettings.setRelateKeywordConfig(relateKeywordConfig);
		
		return staticticsSettings;
	}

}
