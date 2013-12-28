package org.fastcatsearch.statistics;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.fastcatsearch.statistics.log.SearchLog;
import org.fastcatsearch.statistics.util.AggregationResultFileWriter;
import org.fastcatsearch.statistics.util.AggregationResultWriter;
import org.fastcatsearch.statistics.util.RunMerger;
import org.fastcatsearch.statistics.util.SortedRunFileMerger;
import org.fastcatsearch.util.FileUtils;

/**
 * 인기검색어를 만들기 위한 handler LogAggregateHandler를 구현하여 run파일의 위치와 최종 머저를 생성한다.
 * 
 * */
public class PopularKeywordLogAggregateHandler extends LogAggregateHandler<SearchLog> {

	private File targetDir;
	private File runTmpDir;

	public PopularKeywordLogAggregateHandler(File targetDir, int runKeySize, String outputEncoding, Set<String> banWords) {
		super(new SearchLogParser(), runKeySize, outputEncoding, banWords);
		this.targetDir = targetDir;
		this.runTmpDir = new File(targetDir, "_run");
		if (!targetDir.exists()) {
			targetDir.mkdir();
		}
		if (!runTmpDir.exists()) {
			runTmpDir.mkdir();
		}
	}

	@Override
	protected AggregationResultWriter newRunWriter(String encoding, int flushId) {
		File file = getRunFile(flushId);
		return new AggregationResultFileWriter(file, encoding);
	}

	@Override
	protected boolean checkNeedMerge(int flushCount) {
		if (flushCount == 1) {
			File srcFile = getRunFile(0);
			File destFile = new File(targetDir, "0.log");
			if(destFile.exists()){
				destFile.delete();
			}
			try {
				FileUtils.moveFile(srcFile, destFile);
			} catch (IOException e) {
				logger.error("", e);
			}
		}
		
		//flush가 1번이거나 없으면, 머징하지 않는다.
		return flushCount > 1;
	}

	@Override
	protected RunMerger newFinalMerger(String encoding, int flushCount) {
		File file = new File(targetDir, "0.log");

		File[] runFileList = new File[flushCount];
		for (int i = 0; i < flushCount; i++) {
			runFileList[i] = getRunFile(i);
		}

		AggregationResultWriter writer = new AggregationResultFileWriter(file, encoding);
		return new SortedRunFileMerger(runFileList, encoding, writer);
	}

	@Override
	protected void doDone() {
		FileUtils.deleteQuietly(runTmpDir);
	}

	private File getRunFile(int i) {
		return new File(runTmpDir, Integer.valueOf(i) + ".run");
	}
}
