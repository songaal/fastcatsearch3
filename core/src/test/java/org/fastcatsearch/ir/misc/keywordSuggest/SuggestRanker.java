package org.fastcatsearch.ir.misc.keywordSuggest;

import org.fastcatsearch.ir.io.FixedMaxPriorityQueue;
import org.fastcatsearch.ir.misc.keywordSuggest.SearchMemoryIndex.RankKeywordInfo;

public class SuggestRanker extends FixedMaxPriorityQueue<RankKeywordInfo> {

	public SuggestRanker(int maxsize) {
		super(maxsize);
	}

	@Override
	protected int compare(RankKeywordInfo one, RankKeywordInfo two) {
		//작은게 우선.
		int a = one.score() - two.score();
		if(a != 0){
			return a;
		}else{
			//작은게 우선.
			int b = one.keywordInfo().wordSize - two.keywordInfo().wordSize;
			if(b != 0){
				return b;
			}else{
				//점수큰게 우선.
				return two.keywordInfo().score - one.keywordInfo().score;
			}
		}
	}

}
