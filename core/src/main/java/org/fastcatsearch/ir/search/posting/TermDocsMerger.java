package org.fastcatsearch.ir.search.posting;

import java.util.List;

import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.FixedMinHeap;
import org.fastcatsearch.ir.search.TermDoc;
import org.fastcatsearch.ir.search.CompositeTermDoc;

public class TermDocsMerger {
	
	private FixedMinHeap<TermDocsReader> heap;

	public TermDocsMerger(List<CompositeTermDoc> termDocsList) {
		heap = new FixedMinHeap<TermDocsReader>(termDocsList.size());
		for (CompositeTermDoc termDocs : termDocsList) {
			TermDocsReader r = termDocs.getReader();
			if (r.next()) {
				heap.push(r);
			}
		}
	}

	//initSize : 예상되는 termdoc 의 갯수.
	//TODO 차후에 reader로 변경하여 메모리 사용을 줄이도록한다.
	public CompositeTermDoc merge(int indexFieldNum, CharVector term, int initSize) {
		CompositeTermDoc termDocs = new CompositeTermDoc(indexFieldNum, term, initSize);
		TermDoc prevTermDoc = null;
		while (heap.size() > 0) {
			TermDocsReader r = heap.peek();
			TermDoc termDoc = r.read();
			if (prevTermDoc != null && termDoc.docNo() != prevTermDoc.docNo()) {
				termDocs.addTermDoc(prevTermDoc);
			}

			if (prevTermDoc != null) {
				// tf머지.
				prevTermDoc.addTf(termDoc.tf());
				// positions 머지.
				prevTermDoc.addPositions(termDoc.positions());
			}
			if (!r.next()) {
				// 다 읽은 것은 버린다.
				heap.pop();
			}
			heap.heapify();

			prevTermDoc = termDoc;
		}

		termDocs.addTermDoc(prevTermDoc);
		
		return termDocs;
		
	}

}
