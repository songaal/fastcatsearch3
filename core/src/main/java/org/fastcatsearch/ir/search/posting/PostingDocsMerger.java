package org.fastcatsearch.ir.search.posting;

import java.util.List;

import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.FixedMinHeap;
import org.fastcatsearch.ir.search.PostingDoc;
import org.fastcatsearch.ir.search.PostingDocs;

public class PostingDocsMerger {
	
	private FixedMinHeap<PostingDocsReader> heap;

	public PostingDocsMerger(List<PostingDocs> termDocsList) {
		heap = new FixedMinHeap<PostingDocsReader>(termDocsList.size());
		for (PostingDocs termDocs : termDocsList) {
			PostingDocsReader r = termDocs.getReader();
			if (r.next()) {
				heap.push(r);
			}
		}
	}

	//initSize : 예상되는 termdoc 의 갯수.
	//TODO 차후에 reader로 변경하여 메모리 사용을 줄이도록한다.
	public PostingDocs merge(CharVector term, int initSize) {
		PostingDocs termDocs = new PostingDocs(term, initSize);
		PostingDoc prevTermDoc = null;
		while (heap.size() > 0) {
			PostingDocsReader r = heap.peek();
			PostingDoc postingDoc = r.read();
			if (prevTermDoc != null && postingDoc.docNo() != prevTermDoc.docNo()) {
				termDocs.addPostingDoc(prevTermDoc);
			}

			if (prevTermDoc != null) {
				// tf머지.
				prevTermDoc.addTf(postingDoc.tf());
				// positions 머지.
				prevTermDoc.addPositions(postingDoc.positions());
			}
			if (!r.next()) {
				// 다 읽은 것은 버린다.
				heap.pop();
			}
			heap.heapify();

			prevTermDoc = postingDoc;
		}

		termDocs.addPostingDoc(prevTermDoc);
		
		return termDocs;
		
	}

}
