package org.fastcatsearch.statistics;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.fastcatsearch.settings.StatisticsSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RealTimePopularKeywordGenerator {

	protected static Logger logger = LoggerFactory.getLogger(RealTimePopularKeywordGenerator.class);

	private File tmpDir;
	private File targetDir;
	private StatisticsSettings statisticsSettings;

	private Set<String> stopWords;
	private String fileEncoding;
	
	public RealTimePopularKeywordGenerator() {
	}

	public RealTimePopularKeywordGenerator(File tmpDir, File targetDir, StatisticsSettings statisticsSettings) {
		this.tmpDir = tmpDir;
		this.targetDir = targetDir;
		this.statisticsSettings = statisticsSettings;
	}

	private File getLogFile(File dir, int number) {
		return new File(dir, number + ".log");
	}

	protected void rollingByNumber(File targetDir, int maxNumber) {
		File lastFile = getLogFile(targetDir, maxNumber - 1);
		if (lastFile.exists()) {
			lastFile.delete();
		}

		for (int i = maxNumber - 1; i >= 0; i--) {
			File file = getLogFile(targetDir, i);
			// logger.debug("check file > {}", file.getAbsolutePath());

			if (file.exists()) {
				File destFile = getLogFile(targetDir, i + 1);
				file.renameTo(destFile);
				// logger.debug("rename {} > {}", file.getAbsolutePath(),
				// destFile.getAbsolutePath());
			}
		}
	}

	public void generate() {

		final int MAX_FILE_COUNT = 6; // 30분.
		
		// 1. 기존 파일 롤링. rt/0.log => 1.log.
		rollingByNumber(targetDir, MAX_FILE_COUNT);

		// 2. tmpDir내 모든 로그파일을 메모리에 담아 키워드순으로 정렬하여 파일기록(rt/0.log)후 tmp디렉토리 삭제.
		int runSize = 1000;
		File[] inFileList = tmpDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				try{
					return FilenameUtils.getExtension(name).equals("log");
				}catch(Exception e){
					return false;
				}
			}
		});
		
		File outputFile = new File(targetDir, "0.log");
		List<LogAggregateHandler> handlerList = new ArrayList<LogAggregateHandler>();
		handlerList.add(new PopularKeywordLogAggregateHandler(runSize, fileEncoding));
		LogAggregator tmpLogAggregator = new LogAggregator(inFileList, handlerList, runSize, fileEncoding, stopWords);
		tmpLogAggregator.aggregate(outputFile); //0.log
		

		// 3. targetDir내 파일 머장 합산(최근갯수에 가중치를 곱한다.*6 *5 *4 ...)하여 tmp.log로 기. 키워드정렬. 
		// (제외어 파일 사용)
		inFileList = targetDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				try{
					return Integer.parseInt(FilenameUtils.getBaseName(name)) < MAX_FILE_COUNT; 
				}catch(Exception e){
					return false;
				}
			}
		});
		File tmpFile = new File(targetDir, "tmp.log");
		List<LogAggregateHandler> handlerList2 = new ArrayList<LogAggregateHandler>();
		handlerList2.add(new PopularKeywordLogAggregateHandler(runSize, fileEncoding));
		LogAggregator logAggregator = new LogAggregator(inFileList, handlerList2, runSize, fileEncoding, stopWords);
		logAggregator.aggregate(tmpFile); //tmp.log

		// 4. 기존 last.log를 last.log.bak으로 이동하고,
		// tmp.log를 갯수로 재정렬(머징정렬이용) 하여 파일 last.log로 기록한다. (상위 300개만 사용)
		

		// 5. last.log의 상위 10개를 읽어들여, last.log.bak에서의 순위변동을 계산한다. (last.log.bak을
		// Map<key,순위>에 띄워서 사용)
		// 없으면 new, 있으면 +/- 순위변동값.

		// 6. 순위변동이 계산된 메모리의 키워드리스트를 db에 insert한다.
	}

}
