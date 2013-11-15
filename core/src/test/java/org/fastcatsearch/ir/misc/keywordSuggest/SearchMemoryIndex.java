package org.fastcatsearch.ir.misc.keywordSuggest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fastcatsearch.ir.misc.keywordSuggest.IdPosListScorer.IdPosScore;
import org.fastcatsearch.util.lang.ko.HangulUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * dataArray의 일부를 memoryIndex 에 올려서 binary search 후 가까운 위치부터  dataArray를 순차적으로 조사한다.
 * exact는 정확히 매칭하면 리턴하고, prefix는 startsWith로 검사후 결과에 add해준후 최종리턴.  
 * */
public class SearchMemoryIndex {
	private static Logger logger = LoggerFactory.getLogger(SearchMemoryIndex.class);

	private List<TermPointer> memoryIndex = new ArrayList<TermPointer>();

	private List<IndexTerm> tempIndexList = new ArrayList<IndexTerm>();
	private List<IndexTermPosting> indexList = new ArrayList<IndexTermPosting>();
	private List<KeywordInfo> keywordInfoList = new ArrayList<KeywordInfo>();

	private int memoryLoadInterval = 1;

	private int keywordIdGenarator;

	public SearchMemoryIndex() {

	}

	public void add(String keyword) {

		keyword = keyword.trim();
		String[] eojeolList = keyword.split("\\s+");
		int wordSize = eojeolList.length;

		// keyword 별 id부여.
		keywordInfoList.add(new KeywordInfo(keyword, wordSize));
		int keywordId = keywordIdGenarator++;

		// tmp map에 저장, 계속확장.
		String decomposed = HangulUtil.decomposeHangul(keyword);
		logger.debug("# {} > {}", keyword, decomposed);
		tempIndexList.add(new IndexTerm(decomposed, keywordId, 0));

		for (int i = 0; i < eojeolList.length; i++) {
			String decomposedEojeol = HangulUtil.decomposeHangul(eojeolList[i]);
			logger.debug("# {} : {} > {}", eojeolList[i], i, decomposedEojeol);
			tempIndexList.add(new IndexTerm(decomposedEojeol, keywordId, i));
		}
	}

	public static class KeywordInfo {
		String keyword; // 원래 키워드
		int wordSize; // 어절 갯수.
		int score; // 키워드 점수.

		public KeywordInfo(String keyword, int wordSize) {
			this.keyword = keyword;
			this.wordSize = wordSize;
		}

		public KeywordInfo(String keyword, int wordSize, int score) {
			this.keyword = keyword;
			this.wordSize = wordSize;
			this.score = score;
		}
	}
	
	public static class RankKeywordInfo {
		private KeywordInfo keywordInfo;
		private int score;
		
		public RankKeywordInfo(KeywordInfo keywordInfo, int score) {
			this.keywordInfo = keywordInfo;
			this.score = score;
		}
		
		public KeywordInfo keywordInfo(){
			return keywordInfo;
		}
		
		public int score(){
			return score;
		}
	}

	public static class IndexTerm implements Comparable<IndexTerm> {
		private String indexKeyword; // 원래 키워드
		private IdPos idPos; // 키워드 id와 위치.

		public IndexTerm(String indexKeyword, int keywordId, int position) {
			this.indexKeyword = indexKeyword;
			this.idPos = new IdPos(keywordId, position);
		}

		public String indexKeyword() {
			return indexKeyword;
		}

		public IdPos idPos() {
			return idPos;
		}

		@Override
		public int compareTo(IndexTerm o) {
			return indexKeyword.compareToIgnoreCase(o.indexKeyword);
		}
	}

	public static class IndexTermPosting {
		private String indexKeyword; // 원래 키워드
		private List<IdPos> idPosList; // 키워드 id와 위치.

		public IndexTermPosting(String indexKeyword, List<IdPos> idPosList) {
			this.indexKeyword = indexKeyword;
			this.idPosList = idPosList;
		}

		public String indexKeyword() {
			return indexKeyword;
		}

		public List<IdPos> idPosList() {
			return idPosList;
		}

		public IdPosIterator idPosIterator() {
			return new IdPosIterator(idPosList);
		}

		@Override
		public String toString() {
			String postingList = "";
			for (int i = 0; i < idPosList.size(); i++) {
				IdPos idPos = idPosList.get(i);
				postingList += idPos;
				if (i < idPosList.size() - 1) {
					postingList += ", ";
				}
			}

			return "[TermPosting] " + indexKeyword + " : " + postingList;
		}
	}

	public void makeIndex() {

		// 1. indexList를 정렬하여 positing 생성.
		Collections.sort(tempIndexList);

		// indexList를 iterate하면서 positing 생성.
		String prevIndexKeyword = null;
		List<IdPos> list = new ArrayList<IdPos>();
		for (IndexTerm indexTerm : tempIndexList) {

			// 달라지면 기록.
			if (!indexTerm.indexKeyword().equalsIgnoreCase(prevIndexKeyword) && prevIndexKeyword != null) {
				indexList.add(new IndexTermPosting(prevIndexKeyword, list));
				logger.debug("Add {} : {}", prevIndexKeyword, list);
				list = new ArrayList<IdPos>();
			}
			list.add(indexTerm.idPos());
			prevIndexKeyword = indexTerm.indexKeyword();
		}
		logger.debug("## Total {} words exist.", indexList.size());

		// 2. 1/n을 memoryIndex 로 올림.
		for (int i = 0; i < indexList.size(); i++) {
			logger.debug("{}", indexList.get(i));
			if (i % memoryLoadInterval == 0) {
				IndexTermPosting indexTermPosting = indexList.get(i);
				memoryIndex.add(new TermPointer(indexTermPosting.indexKeyword(), i));
			}
		}

	}

	public List<String> getSuggestKeywordList(String keyword) {

		char lastChar = keyword.charAt(keyword.length() - 1);
		boolean isLastWhitespace = Character.isWhitespace(lastChar);

		keyword = keyword.trim();
		String[] eojeolList = keyword.split("\\s+");
		int wordSize = eojeolList.length;
		IdPosListScorer idPosListScorer = new IdPosListScorer();
		for (int i = 0; i < eojeolList.length; i++) {
			// 마지막 단어는
			IdPosIterator idPosIterator = null;
			if (i < eojeolList.length - 1) {
				idPosIterator = exactSearch(eojeolList[i]);
			} else {
				if(isLastWhitespace){
					idPosIterator = exactSearch(eojeolList[i]);
				}else{
					idPosIterator = prefixSearch(eojeolList[i]);
				}
			}
			
			
			if(idPosIterator != null){
				idPosListScorer.add(idPosIterator);
			}
		}

		//RANKER : score정렬후, word size(어절개수)로 이중정렬한다.
		SuggestRanker ranker = new SuggestRanker(10);
		List<String> result = new ArrayList<String>(); 
		IdPosScore idPosScore = new IdPosScore();
		while(idPosListScorer.next(idPosScore)){
			int keywordId = idPosScore.id();
			KeywordInfo keywordInfo = keywordInfoList.get(keywordId);
			RankKeywordInfo rankKeywordInfo = new RankKeywordInfo(keywordInfo, idPosScore.score());
			ranker.push(rankKeywordInfo);
			
		}
		Object[] sortedList = ranker.getSortedList();
		for(Object obj :sortedList){
			RankKeywordInfo info = (RankKeywordInfo) obj;
			logger.debug(">> {}", info.keywordInfo().keyword);
			result.add(info.keywordInfo().keyword);
		}
		return result;
	}

	public boolean binsearch(String singleTerm, TermPointer termPointer) {
		if (memoryIndex.size() == 0)
			return false;

		int left = 0;
		int right = memoryIndex.size() - 1;
		int mid = -1;

		boolean found = false;

		while (left <= right) {
			mid = (left + right) / 2;

			int cmp = memoryIndex.get(mid).indexTerm.compareTo(singleTerm);

			if (cmp == 0) {
				found = true;
				break;
			} else if (cmp < 0) {
				left = mid + 1;
			} else {
				right = mid - 1;
			}
		}

		if (found) {
			termPointer.indexTerm = singleTerm;
			termPointer.pointer = mid;
			return true;
		}

		// mid = Min(mid, right)
		mid = right < mid ? right : mid;

		if (mid == -1)
			mid = 0;

		if (mid > 0 && memoryIndex.get(mid).indexTerm.compareTo(singleTerm) > 0) {
			mid--;
		}
		if (mid < 0)
			mid = 0;

		termPointer.indexTerm = memoryIndex.get(mid).indexTerm;
		termPointer.pointer = memoryIndex.get(mid).pointer;
		return false;
	}

	public IdPosIterator prefixSearch(String keyword) {
		CompositeIdPosIterator compositeIdPosIterator = new CompositeIdPosIterator();
		keyword = HangulUtil.decomposeHangul(keyword);

		TermPointer termPointer = new TermPointer();
		boolean found = binsearch(keyword, termPointer);
		// prefix search는 found와 상관없이 진행.
		int pointer = termPointer.pointer;
		logger.debug("check " + keyword + ", found = " + found + ", pointer=" + pointer);
		if (pointer != -1) {
			// 최근 위치찾음. 순차검색시작.
			for (int i = pointer; i < indexList.size(); i++) {
				IndexTermPosting posting = indexList.get(i);
				String target = posting.indexKeyword();
				int compares = comparePrefix(keyword, target);
				logger.debug("target =" + target + ", keyword = " + keyword + ", compares=" + compares);

				if (compares == 0) {
					// 일치.
					compositeIdPosIterator.add(posting.idPosIterator());
					continue;
				}

				if (target.startsWith(keyword)) {
					compositeIdPosIterator.add(posting.idPosIterator());
				}

				if (compares < 0) {
					break;
				}

			}
		}
		return compositeIdPosIterator;
	}

	private int comparePrefix(String keyword, String target) {
		if (keyword.length() <= target.length()) {
			for (int i = 0; i < keyword.length(); i++) {
				int d = keyword.charAt(i) - target.charAt(i);
				if (d != 0) {
					return d;
				}
			}
		} else {
			return 1;
		}

		return 0;
	}

	public IdPosIterator exactSearch(String keyword) {
		keyword = HangulUtil.decomposeHangul(keyword);

		TermPointer termPointer = new TermPointer();
		boolean found = binsearch(keyword, termPointer);
		int pointer = termPointer.pointer;
		logger.debug("check " + keyword + ", found = " + found + ", pointer=" + pointer);
		if (pointer != -1) {
			// 최근 위치찾음. 순차검색시작.
			for (int i = pointer; i < indexList.size(); i++) {
				IndexTermPosting posting = indexList.get(i);
				String target = posting.indexKeyword();
				int compares = keyword.compareToIgnoreCase(target);//comparePrefix(keyword, target);
				logger.debug("target =" + target + ", keyword = " + keyword + ", compares=" + compares);

				if (compares == 0) {
					// 일치.
					return posting.idPosIterator();
				}else if (compares < 0) {
					break;
				}

			}
		}
		
		return null;
	}

	public static class TermPointer {
		private String indexTerm;
		private int pointer;

		public TermPointer() {
		}

		public TermPointer(String indexTerm, int pointer) {
			this.indexTerm = indexTerm;
			this.pointer = pointer;
		}

		public String getIndexTerm() {
			return indexTerm;
		}

		public int getPointer() {
			return pointer;
		}

	}

}
