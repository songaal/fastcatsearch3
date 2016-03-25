package org.fastcatsearch.ir.search.clause;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Random;

import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.search.DataPostingReader;
import org.fastcatsearch.ir.search.PostingDoc;
import org.fastcatsearch.ir.search.PostingReader;
import org.fastcatsearch.ir.search.posting.NodeReader;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiTermOperatedClauseTest {
	protected static Logger logger = LoggerFactory.getLogger(NodeReader.class);
	@Test
	public void test() throws IOException {
		MultiTermOperatedClause multiTermOperatedClause = new MultiTermOperatedClause();

		int queryPosition = 0;
		int weight = 1;

		PostingDoc[] data = createData(new int[] { 1, 2, 3, 4, 7, 8 });
		PostingReader reader = new DataPostingReader(new CharVector("abc"), queryPosition++, weight, data, data.length, 10);
		
		assertTrue(reader.hasNext());
		
		multiTermOperatedClause.addTerm(reader);

		data = createData(new int[] { 2, 3, 7, 10, 11 });
		
		reader = new DataPostingReader(new CharVector("123"), queryPosition++, weight, data, data.length, 10);
		multiTermOperatedClause.addTerm(reader);

		data = createData(new int[] { 1, 2, 3, 9, 10, 11, 12, 13 });
		reader = new DataPostingReader(new CharVector("qqq"), queryPosition++, weight, data, data.length, 10);
		multiTermOperatedClause.addTerm(reader);

		RankInfo rankInfo = new RankInfo();
		while (multiTermOperatedClause.next(rankInfo)) {
			System.out.println(rankInfo);
		}

	}

	private PostingDoc[] createData(int[] array) {
		PostingDoc[] result = new PostingDoc[array.length];
		for (int i = 0; i < array.length; i++) {
			int[] positions = createPositions(10);
			result[i] = new PostingDoc(array[i], positions.length, positions);
		}
		return result;
	}

	Random r = new Random(System.currentTimeMillis());

	private int[] createPositions(int count) {
		int[] positions = new int[count];
		for (int i = 0; i < count; i++) {
			positions[i] = r.nextInt(10) + 1;
		}
		return positions;

	}

}
