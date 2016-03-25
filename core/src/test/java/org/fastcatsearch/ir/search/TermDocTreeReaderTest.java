package org.fastcatsearch.ir.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Random;

import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.search.posting.PostingDocsTreeNode;
import org.junit.Test;

public class TermDocTreeReaderTest {

	@Test
	public void test() throws IOException {
		
		int COUNT = 100000;
		
		int expectedCount = 0;
		PostingDoc[] termDocList = null;
		
		TermDocTreeReader reader = new TermDocTreeReader();
		
		int weight = 1;
		int termPosition = 0;
		termDocList = makeTermDocList(COUNT);
		PostingReader termDocs = new DataPostingReader(new CharVector("aa"), termPosition, weight, termDocList, termDocList.length);
		PostingDocsTreeNode node = new PostingDocsTreeNode(termDocs, false);
		reader.addNode(node);
		expectedCount += COUNT;
		
		termDocList = makeTermDocList(COUNT);
		termDocs = new DataPostingReader(new CharVector("bb"), termPosition, weight, termDocList, termDocList.length);
		node = new PostingDocsTreeNode(termDocs, false);
		reader.addNode(node);
		expectedCount += COUNT;
		
		termDocList = makeTermDocList(COUNT);
		termDocs = new DataPostingReader(new CharVector("cc"), termPosition, weight, termDocList, termDocList.length);
		node = new PostingDocsTreeNode(termDocs, false);
		reader.addNode(node);
		expectedCount += COUNT;
		
		TermDocCollector termDocCollector = new TermDocCollector(3);
		int docNo = -1;
		int prevDocNo = -1;
//		System.out.println("======================");
		int totalCount = 0;
		long st = System.currentTimeMillis();
		while((docNo = reader.next(termDocCollector)) >= 0){
			for (int i = 0; i < termDocCollector.size(); i++) {
				CollectedEntry entry = termDocCollector.get(i);
				totalCount++;
			}
			termDocCollector.clear();
			assertTrue(prevDocNo <= docNo);
			prevDocNo = docNo;
		}
		
		System.out.println("Time = "+(System.currentTimeMillis() - st));
		
		assertEquals(expectedCount, totalCount);
	}
	
	private PostingDoc[] makeTermDocList(int count){
		Random r = new Random(System.currentTimeMillis());
		PostingDoc[] termDocList = new PostingDoc[count];
		int prevDocNo = 0;
//		System.out.println("-------------------");
		for (int i = 0; i < count; i++) {
			int docNo = prevDocNo + r.nextInt(5);
			int tf = r.nextInt(3);
			int[] positions = new int[tf];
			termDocList[i] = new PostingDoc(docNo, tf, positions);
//			System.out.println(">"+termDocList[i]);
			prevDocNo = docNo;
		}
		return termDocList;
	}

}
