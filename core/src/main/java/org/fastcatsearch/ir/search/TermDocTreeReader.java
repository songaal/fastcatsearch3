package org.fastcatsearch.ir.search;

import org.fastcatsearch.ir.search.posting.NodeReader;

import java.io.IOException;

public class TermDocTreeReader {

	private NodeReader root;

	public void addNode(NodeReader node) throws IOException {
		
		
		if (root == null) {
			root = node;
		} else {
			root = new BigramTreeNode(root, node);
		}
	}

	
	public int next(TermDocCollector termDocCollector) throws IOException {
		
		if(root == null){
			return -1;
		}
		
		int docNo = root.next();

		if (docNo != -1) {
			root.fill(termDocCollector);
			return docNo;
		}

		return -1;
	}
	
	class BigramTreeNode extends NodeReader {

		private NodeReader node1;
		private NodeReader node2;

		private int docNo1;
		private int docNo2;

		private TermDocCollector tempTermDocCollector;

		public BigramTreeNode(NodeReader node1, NodeReader node2) throws IOException {
			this.node1 = node1;
			this.node2 = node2;

			tempTermDocCollector = new TermDocCollector(2);
			docNo1 = node1.next();
			docNo2 = node2.next();
			
			logger.debug("BigramTreeNode doc >> {} : {}", docNo1, docNo2);
		}

		@Override
		public int next() throws IOException {
			tempTermDocCollector.clear();
			if (docNo1 == -1 && docNo2 == -1) {
				return -1;
			} else if (docNo1 == docNo2) {
				node1.fill(tempTermDocCollector);
				node2.fill(tempTermDocCollector);
				int docNo = docNo1;
				docNo1 = node1.next();
				docNo2 = node2.next();
				return docNo;
			} else if ((docNo1 >= 0 && docNo1 < docNo2) || docNo2 == -1) {
				node1.fill(tempTermDocCollector);
				int docNo = docNo1;
				docNo1 = node1.next();
				return docNo;
			} else if ((docNo2 >= 0 && docNo1 > docNo2) || docNo1 == -1) {
				node2.fill(tempTermDocCollector);
				int docNo = docNo2;
				docNo2 = node2.next();
				return docNo;
			}

			return -1;
		}

		@Override
		public void fill(TermDocCollector termDocCollector) {
			termDocCollector.addAll(tempTermDocCollector);
		}

		@Override
		public void close() {
			node1.close();
			node2.close();
		}
	}

	public void close() {
		root.close();
	}
}