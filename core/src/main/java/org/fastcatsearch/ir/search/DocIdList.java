package org.fastcatsearch.ir.search;

public class DocIdList {
	private int[] segmentSequenceList;
	private int[] docNoList;
	private int size;
	public DocIdList(){
		this(32);
	}
	public DocIdList(int initSize){
		segmentSequenceList = new int[initSize];
		docNoList = new int[initSize];
	}
	
	public void add(int segmentSequence, int docNo){
		if(docNoList.length == size){
			int newSize = size * 2;
			int[] newSegmentSequenceList = new int[newSize];
			int[] newDocNoList = new int[newSize];
			System.arraycopy(segmentSequenceList, 0, newSegmentSequenceList, 0, size);
			System.arraycopy(docNoList, 0, newDocNoList, 0, size);
			size = newSize;
		}
		segmentSequenceList[size] = segmentSequence;
		docNoList[size] = docNo;
		size++;
	}
	
	public int segmentSequence(int i){
		return segmentSequenceList[i];
	}
	
	public int docNo(int i){
		return docNoList[i];
	}
	
	public int size(){
		return size;
	}
}
