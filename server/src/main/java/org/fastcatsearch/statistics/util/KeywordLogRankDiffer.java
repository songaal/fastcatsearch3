package org.fastcatsearch.statistics.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.db.vo.PopularKeywordVO.RankDiffType;
import org.fastcatsearch.statistics.KeyCountRunEntryReader;
import org.fastcatsearch.statistics.vo.RankKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeywordLogRankDiffer {

	protected static Logger logger = LoggerFactory.getLogger(KeywordLogRankDiffer.class);

	private File targetFile;
	private File compareFile;
	private int topCount;
	private String encoding;

	public KeywordLogRankDiffer(File targetFile, File compareFile, int topCount, String encoding) {
		this.targetFile = targetFile;
		this.compareFile = compareFile;
		this.topCount = topCount;
		this.encoding = encoding;
	}

	public List<RankKeyword> diff() {
		List<RankKeyword> result = new ArrayList<RankKeyword>();
		try {

			// 1. target 파일에서 top N개를 뽑아낸다.
			KeyCountRunEntryReader targetReader = new KeyCountRunEntryReader(targetFile, encoding);
			try {
				int rank = 1; // 1부터 시작한다.
				while (targetReader.next()) {
					if (rank > topCount) {
						break;
					}
					
					KeyCountRunEntry entry = targetReader.entry();

					result.add(new RankKeyword(entry.getKey(), rank++));
				}
			} finally {
				if (targetReader != null) {
					targetReader.close();
				}
			}

			// 2. compareFile를 순차로 읽으면서 해당 키워드가 있는지 확인한다.
			if (compareFile.exists()) {
				KeyCountRunEntryReader compareReader = new KeyCountRunEntryReader(compareFile, encoding);
				try {
					int foundCount = 0;
					int prevRank = 1; // 이전 인기검색어의 순위. 1부터 시작한다.

					while (compareReader.next()) {
						KeyCountRunEntry entry = compareReader.entry();
						String compareKeyword = entry.getKey();
						for (RankKeyword keyword : result) {
							String targetKeyword = keyword.getKeyword();
							if (compareKeyword.equals(targetKeyword)) {
								int rankDiff = prevRank - keyword.getRank();
								keyword.setRankDiff(Math.abs(rankDiff));
								if (rankDiff == 0) {
									keyword.setRankDiffType(RankDiffType.EQ);
								} else if (rankDiff > 0) {
									keyword.setRankDiffType(RankDiffType.UP);
								} else {
									keyword.setRankDiffType(RankDiffType.DN);
								}
								foundCount++;
								break;
							}

						}

						if (foundCount == result.size()) {
							break;// 모두 찾았다.
						}
						prevRank++;
					}

				} finally {
					if (compareReader != null) {
						compareReader.close();
					}
				}
			}

		} catch (IOException e) {
			logger.error("", e);
			return null;
		}
		return result;
	}
}
