//package org.fastcatsearch.ir.search;
//
//import org.fastcatsearch.ir.io.CharVector;
//
//public class TermDocsWithPosition extends TermDocs {
//
//	private int[][] positions;
//
//	public TermDocsWithPosition(int indexFieldNum, CharVector term, int size) {
//		super(indexFieldNum, term, size);
//		positions = new int[size][];
//	}
//
//	public TermDocsWithPosition(int indexFieldNum, CharVector term, int count, int[] docs, int[] tfs, int[][] positions) {
//		super(indexFieldNum, term, count, docs, tfs);
//		this.positions = positions;
//	}
//	
//	public int[][] positions(){
//		return positions;
//	}
//	
//	public void setPositions(int[][] positions){
//		this.positions = positions;
//	}
//}
