package org.fastcatsearch.ir.search.clause;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Random;

import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.search.ClauseExplanation;
import org.junit.Test;

public class OperatedClauseExplainTest {

	private Random r = new Random(System.currentTimeMillis());
	
	@Test
	public void test() throws IOException {
		
		int count1 = 100;
		int[] docs1 = new int[count1];
		int[] score1 = new int[count1];
		makeDocs(count1, docs1, score1);
		
		int count2 = 100;
		int[] docs2 = new int[count2];
		int[] score2 = new int[count2];
		makeDocs(count2, docs2, score2);
		
		
		ClauseExplanation explanation = new ClauseExplanation();
		UserOperatedClause c1 = new UserOperatedClause(count1, docs1, score1);
		UserOperatedClause c2 = new UserOperatedClause(count2, docs2, score2);
		RankInfo docInfo = new RankInfo();
		
		WeightedOperatedClause weightedOperatedClause = new WeightedOperatedClause(c1, c2);
		weightedOperatedClause.init(explanation);
		
		int i = 0;
		while(weightedOperatedClause.next(docInfo)){
			System.out.println((i+1)+"] "+docInfo);
			i++;
		}
		
		System.out.println(explanation);
	}
	
	private void makeDocs(int count, int[] docs, int[] score){
		int d = 0;
		int prev = 0;
		for(int i=0; i<count;i++){
			d = r.nextInt(20);
			docs[i] = (prev + d);
			prev = docs[i];
			score[i] = i;
		}
	}
}
