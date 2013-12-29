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
 * 실시간 인기키워드를 만드는 클래스.
 * */
public class RealTimePopularKeywordGenerator {

	protected static Logger logger = LoggerFactory.getLogger(RealTimePopularKeywordGenerator.class);

	private Set<String> banWords;
	private String fileEncoding;
	
	private File targetDir;
	// STEP1_DIR: 각 노드들로 부터 전달받은 로그들이 쌓여있다. node1.log, node2.log, ..
	private final File STEP1_DIR;
	// STEP2_DIR: step1의 로그들을 key, count로 취합한 로그들이 단위시간별로 쌓여있다.
	// 예) 0.log, 1.log, 2.log, ...
	private final File STEP2_DIR;
	// STEP3_DIR: step2의 로드들을 가중치적용한 취합파일 key-count.log존재.
	// count로 정렬한 key-count-rank.log존재. 이전 정렬파일 key-count-rank-prev.log존재
	private final File STEP3_DIR;

	private final int recentLogUsingCount;
	private final int runKeySize;
	private final int minimumHitCount;
	private final int topCount;

	public RealTimePopularKeywordGenerator(File targetDir, StatisticsSettings statisticsSettings, String fileEncoding) {
		this.targetDir = targetDir;
		this.fileEncoding = fileEncoding;

		STEP1_DIR = new File(targetDir, "step1");
		STEP2_DIR = new File(targetDir, "step2");
		STEP3_DIR = new File(targetDir, "step3");

		int size = statisticsSettings.getWorkingMemoryKeySize();
		if (size > 0) {
			runKeySize = size;
		} else {
			runKeySize = 10 * 10000; // 디폴트로 메모리에서는 10만개만 취합한다.
		}

		int count = statisticsSettings.getRealTimePopularKeywordConfig().getRecentLogUsingCount();
		if (count > 0) {
			recentLogUsingCount = count;
		} else {
			recentLogUsingCount = 6; // 디폴트로 이전 통계를 합하여 최근 6개만 사용한다.
		}

		minimumHitCount = statisticsSettings.getRealTimePopularKeywordConfig().getMinimumHitCount();

		topCount = statisticsSettings.getRealTimePopularKeywordConfig().getTopCount();
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
		if(!STEP1_DIR.exists()){
			throw new IOException(STEP1_DIR + " not found.");
		}
		
		if(STEP2_DIR.exists()){
			FileUtils.deleteQuietly(STEP2_DIR);
		}
		STEP2_DIR.mkdir();
		
		//STEP3_DIR은 존재하면 삭제하지 않고 그대로 이용한다.
		if(!STEP3_DIR.exists()){
			STEP3_DIR.mkdir();
		}
		/*
		 * 1. 기존 파일 롤링. step2/0.log => step2/1.log.
		 */
		rollingByNumber(STEP2_DIR, recentLogUsingCount);

		/*
		 * 2. tmpDir내 모든 로그파일을 메모리에 담아 키워드순으로 정렬하여 파일기록(rt/0.log)후 tmp디렉토리 삭제.
		 */
		File[] inFileList = STEP1_DIR.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				try {
					return FilenameUtils.getExtension(name).equals("log");
				} catch (Exception e) {
					return false;
				}
			}
		});

		String outputFilename = "0.log";
		List<LogAggregateHandler<SearchLog>> handlerList = new ArrayList<LogAggregateHandler<SearchLog>>();
		handlerList.add(new PopularKeywordLogAggregateHandler(STEP2_DIR, outputFilename, runKeySize, fileEncoding, banWords, minimumHitCount));
		LogAggregator<SearchLog> tmpLogAggregator = new LogAggregator<SearchLog>(inFileList, fileEncoding, handlerList);
		tmpLogAggregator.aggregate();

		/*
		 * 3. STEP2_DIR내 파일 머장 합산(최근갯수에 decay factor 를 곱한다.)하여 STEP3_DIR/key-count.log로 기록. 키워드로 정렬. (제외어 파일 사용)
		 */
		final List<Float> weightArrayList = new ArrayList<Float>();
		inFileList = STEP2_DIR.listFiles(new FilenameFilter() {
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
		File keyCountFile = new File(STEP3_DIR, "key-count.log");
		AggregationResultFileWriter writer = new AggregationResultFileWriter(keyCountFile, "utf-8");
		WeightedSortedRunFileMerger merger = new WeightedSortedRunFileMerger(inFileList, weightList, "utf-8", writer);
		merger.merge();

		/*
		 * 4. 기존 key-count-rank.log를 key-count-rank-prev.log으로 이동하고 key-count.log를 count기준 정렬하여, key-count-rank.log 로 기록한다. 파일기반 소팅 사용.
		 */
		File rankFile = new File(STEP3_DIR, "key-count-rank.log");
		File prevRankFile = new File(STEP3_DIR, "key-count-rank-prev.log");
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
		File sortWorkDir = new File(STEP3_DIR, "tmp");
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
