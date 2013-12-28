package org.fastcatsearch.statistics.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 시간별로 쌓아놓은 로그를 취합하여 하나로 만든다. 로그들은 이미 키워드 순으로 정렬되어있다.
 * 단순 머징만 하면 되므로, aggregator가 아닌 SortedRunFileMerger 를 사용함.  
 * */
public class SortedRunFileMergerTest {
	public static Logger logger = LoggerFactory.getLogger(SortedRunFileMergerTest.class);
	@Test
	public void test() throws IOException {
		File targetDir = new File("src/test/resources/statistics/rt");
		File testDir = new File(targetDir, "test");
		File[] inFileList = testDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				try{
					return FilenameUtils.getExtension(name).equals("log");
				}catch(Exception e){
					return false;
				}
			}
		});
		
		File outputFile = new File(targetDir, "rank.log");
		AggregationResultFileWriter writer = new AggregationResultFileWriter(outputFile, "utf-8");
		logger.debug(">> {} {}", "", inFileList);
		SortedRunFileMerger merger = new SortedRunFileMerger(inFileList, "utf-8", writer);
		merger.merge();
	}

}
