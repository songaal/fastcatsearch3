package org.fastcatsearch.ir.search;

import org.fastcatsearch.ir.search.posting.NodeReader;

public class TermDocTreeReader {

	private NodeReader root;

	// private
	public void addNode(NodeReader node) {
		if (root == null) {
			root = node;
		} else {
			root = new BigramTreeNode(root, node);
		}
	}

	
	public int next(TermDocCollector termDocCollector) {
		
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
//	public int next(List<TermDoc> totalTermDocList) {
//		
//		if(root == null){
//			return -1;
//		}
//		
//		int docNo = root.next();
//
//		if (docNo != -1) {
//			root.fill(totalTermDocList);
//			return docNo;
//		}
//
//		return -1;
//	}

	
	class BigramTreeNode extends NodeReader {

		private NodeReader node1;
		private NodeReader node2;

		private int docNo1;
		private int docNo2;

//		private List<CollectedEntry> termDocList;
		private TermDocCollector tempTermDocCollector;

		public BigramTreeNode(NodeReader node1, NodeReader node2) {
			this.node1 = node1;
			this.node2 = node2;

//			termDocList = new ArrayList<CollectedEntry>(2);
			tempTermDocCollector = new TermDocCollector(2);
			docNo1 = node1.next();
			docNo2 = node2.next();
		}

		@Override
		public int next() {
//			termDocList.clear();
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
//		public void fill(List<TermDoc> totalTermDocList) {
//			totalTermDocList.addAll(termDocList);
//		}
	}
}