package org.fastcatsearch.statistics;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.statistics.CategoryStatistics;
import org.junit.Test;

public class CategoryStatisticsTest {

	@Test
	public void test() {
		

	}

	private void sleepShort() {
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
		}
	}

	private Query getUserKeywordQuery(String keyword) {
		keyword += ("_" + Long.toString(System.currentTimeMillis()));
		Query q = new Query();
		Map<String, String> userData = new HashMap<String, String>();
		userData.put(Metadata.UD_KEYWORD, keyword);
		q.getMeta().setUserData(userData);
		return q;
	}
}
