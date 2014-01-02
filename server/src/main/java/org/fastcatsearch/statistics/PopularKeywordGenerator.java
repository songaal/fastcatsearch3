package org.fastcatsearch.statistics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.fastcatsearch.settings.StatisticsSettings;
import org.fastcatsearch.statistics.log.SearchLog;
import org.fastcatsearch.statistics.util.AggregationResultFileWriter;
import org.fastcatsearch.statistics.util.KeyCountRunEntry;
import org.fastcatsearch.statistics.util.KeywordLogRankDiffer;
import org.fastcatsearch.statistics.util.LogSorter;
import org.fastcatsearch.statistics.util.WeightedSortedRunFileMerger;
import org.fastcatsearch.statistics.vo.RankKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 인기검색어를 만드는 클래스.
 * 일/주/월/년 통계를 만든다.
 * */
public class PopularKeywordGenerator {

	protected static Logger logger = LoggerFactory.getLogger(PopularKeywordGenerator.class);

	private static final String WORKING_LOG_FILENAME = "0.log";
	private static final String KEY_COUNT_LOG_FILENAME = "key-count.log";
	private static final String KEY_COUNT_RANK_LOG_FILENAME = "key-count-rank.log";
	private static final String KEY_COUNT_RANK_PREV_LOG_FILENAME = "key-count-rank-prev.log";
	
	private Set<String> banWords;
	private String fileEncoding;
	
	//targetDir : 작업대상 디렉토리.
	private File targetDir;
	// logFileList: 각 노드들로 부터 전달받은 로그들. node1.log, node2.log, ..
	private File[] logFileList;
	
	// WORKING_DIR: logFileList의 로그들을 key, count로 취합한 로그들이 단위시간별로 쌓여있다.
	// 예) 0.log, 1.log, 2.log, ...
	private final File WORKING_DIR;
	// RESULT_DIR: WORKING_DIR의 로드들을 가중치적용한 취합파일 key-count.log존재.
	// count로 정렬한 key-count-rank.log존재. 이전 정렬파일 key-count-rank-prev.log존재
	private final File RESULT_DIR;

	private final int recentLogUsingCount;
	private final int runKeySize;
	private final int minimumHitCount;
	private final int topCount;

	public PopularKeywordGenerator(File targetDir, File[] logFileList, StatisticsSettings statisticsSettings, String fileEncoding) {
		this.targetDir = targetDir;
		this.logFileList = logFileList;
		this.fileEncoding = fileEncoding;

		WORKING_DIR = new File(targetDir, "working");
		RESULT_DIR = new File(targetDir, "result");

		runKeySize = 10 * 10000; // 디폴트로 메모리에서는 10만개만 취합한다.

		int count = statisticsSettings.getRealTimePopularKeywordConfig().getRecentLogUsingCount();
		if (count > 0) {
			recentLogUsingCount = count;
		} else {
			recentLogUsingCount = 6; // 디폴트로 이전 통계를 합하여 최근 6개만 사용한다.
		}

		minimumHitCount = statisticsSettings.getRealTimePopularKeywordConfig().getMinimumHitCount();

		int topCount = statisticsSettings.getRealTimePopularKeywordConfig().getTopCount();
		if(topCount == 0){
			this.topCount = 10; //디폴트 10개 뽑아냄.
		}else{
			this.topCount = topCount;
		}
	}

	private File getLogFile(File dir, int number) {
		return new File(dir, number + ".log");
	}

	public List<RankKeyword> generate() throws IOException {
		/*
		 * 0. 점검사항. 
		 * */
		if(!targetDir.exists()){
			throw new IOException("ROOT " + targetDir + " not found.");
		}
		
		if(WORKING_DIR.exists()){
			FileUtils.deleteQuietly(WORKING_DIR);
		}
		WORKING_DIR.mkdir();
		
		//STEP3_DIR는 이전 랭킹로그도 참조해야하므로, 삭제하지 않고 그대로 이용한다.
		if(!RESULT_DIR.exists()){
			RESULT_DIR.mkdir();
		}
		/*
		 * 1. 기존 파일 롤링. working/0.log => working/1.log....
		 */
		rollingByNumber(WORKING_DIR, recentLogUsingCount);

		/*
		 * 2. logFileList 로그파일을 메모리에 담아 키워드순으로 정렬하여 파일기록(rt/working/0.log). 제외어 파일 사용. 
		 * 내부적으로 _run임시 디렉토리 사용함.
		 */

		List<LogAggregateHandler<SearchLog>> handlerList = new ArrayList<LogAggregateHandler<SearchLog>>();
		handlerList.add(new PopularKeywordLogAggregateHandler(WORKING_DIR, WORKING_LOG_FILENAME, runKeySize, fileEncoding, banWords, minimumHitCount));
		LogAggregator<SearchLog> tmpLogAggregator = new LogAggregator<SearchLog>(logFileList, fileEncoding, handlerList);
		tmpLogAggregator.aggregate();

		/*
		 * 3. WORKING_DIR 내 파일 머장 합산(최근갯수에 decay factor 를 곱한다.)하여 RESULT_DIR/key-count.log로 기록. 키워드로 정렬. 
		 */
		final List<Float> weightArrayList = new ArrayList<Float>();
		File[] inFileList = WORKING_DIR.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				try {
					int number = Integer.parseInt(FilenameUtils.getBaseName(name));
					if (number < recentLogUsingCount) {
						// decay factor 계산 : 예를들어 MAX_FILE_COUNT=10일때, 0은 1, 1은 0.9, 2는 0.8 .. 식으로 decay factor가 적용된다.
						weightArrayList.add(((float) recentLogUsingCount - number) / (float) recentLogUsingCount);
//						logger.debug("File {} : {} >> {}", number, name, weightArrayList.get(weightArrayList.size() - 1));
						return true;
					}
					return false;
				} catch (Exception e) {
					return false;
				}
			}
		});
		float[] weightList = new float[weightArrayList.size()];
		for (int i = 0; i < weightArrayList.size(); i++) {
			weightList[i] = weightArrayList.get(i);
		}
		File keyCountFile = new File(RESULT_DIR, KEY_COUNT_LOG_FILENAME);
		AggregationResultFileWriter writer = new AggregationResultFileWriter(keyCountFile, fileEncoding);
		WeightedSortedRunFileMerger merger = new WeightedSortedRunFileMerger(inFileList, weightList, fileEncoding, writer);
		merger.merge();

		/*
		 * 4. 기존 key-count-rank.log를 key-count-rank-prev.log으로 이동하고 key-count.log를 count기준 정렬하여, key-count-rank.log 로 기록한다. 파일기반 소팅 사용.
		 */
		File rankFile = new File(RESULT_DIR, KEY_COUNT_RANK_LOG_FILENAME);
		File prevRankFile = new File(RESULT_DIR, KEY_COUNT_RANK_PREV_LOG_FILENAME);
		if (rankFile.exists()) {
			FileUtils.copyFile(rankFile, prevRankFile);
		}

		// count 내림차순정렬을 위한 comparator
		Comparator<KeyCountRunEntry> comparator = new Comparator<KeyCountRunEntry>() {

			@Override
			public int compare(KeyCountRunEntry o1, KeyCountRunEntry o2) {
				if (o1 == null && o2 == null) {
					return 0;
				} else if (o1 == null) {
					return -1;
				} else if (o2 == null) {
					return 1;
				}

				// 내림차순 정렬.
				return o2.getCount() - o1.getCount();
			}

		};

		// LogSorter를 사용해 keyCountFile -> rankFile 로 저장한다.
		File sortWorkDir = new File(RESULT_DIR, "tmp");
		InputStream is = new FileInputStream(keyCountFile);
		OutputStream os = new FileOutputStream(rankFile);
		try {
			LogSorter logSorter = new LogSorter(is, fileEncoding, runKeySize);
			logSorter.sort(os, comparator, sortWorkDir);
		} finally {
			if (is != null) {
				is.close();
			}
			if (os != null) {
				os.close();
			}
		}

		/*
		 * 5. key-count-rank.log의 상위 10개를 읽어들여, key-count-rank-prev.log에서의 순위변동을 계산한다. key-count-rank-prev.log 파일을 순차적으로 읽으면서 key-count-rank.log의 상위 10개 단어를 확인하고 모두 확인했으면 탐색종료하는
		 * 방법을 사용한다. 없으면 new, 있으면 +/- 순위변동값.
		 */
		KeywordLogRankDiffer differ = new KeywordLogRankDiffer(rankFile, prevRankFile, topCount, fileEncoding);
		List<RankKeyword> result = differ.diff();

		/*
		 * 6. 순위변동이 계산된 키워드리스트를 리턴한다.
		 */

		return result;
	}

	/*
	 * 0.log -> 1.log 로 {maxNumber}.log 까지 파일명을 shift한다. 최종적으로 0.log파일은 없어진다.
	 */
	protected void rollingByNumber(File dir, int maxNumber) {
		File lastFile = getLogFile(dir, maxNumber - 1);
		if (lastFile.exists()) {
			lastFile.delete();
		}

		for (int i = maxNumber - 1; i >= 0; i--) {
			File file = getLogFile(dir, i);
			// logger.debug("check file > {}", file.getAbsolutePath());

			if (file.exists()) {
				File destFile = getLogFile(dir, i + 1);
				file.renameTo(destFile);
				// logger.debug("rename {} > {}", file.getAbsolutePath(),
				// destFile.getAbsolutePath());
			}
		}
	}
}
