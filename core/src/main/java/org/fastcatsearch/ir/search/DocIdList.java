package org.fastcatsearch.ir.search;

public class DocIdList {
	private int[] segmentSequenceList;
	private int[] docNoList;
	private int size;
	
	private DocIdList[] bundleDocIdListArray;
	
	public DocIdList(){
		this(32);
	}
	public DocIdList(int initSize){
		segmentSequenceList = new int[initSize];
		docNoList = new int[initSize];
	}
	
	public void add(int segmentSequence, int docNo){
		add(segmentSequence, docNo, null);
	}
	
	public void add(int segmentSequence, int docNo, DocIdList bundleDocIdList){
		if(bundleDocIdList != null) {
			if(bundleDocIdListArray == null){
				bundleDocIdListArray = new DocIdList[segmentSequenceList.length];
			}
		}
		
		if(docNoList.length == size){
			int newSize = size * 2;
			int[] newSegmentSequenceList = new int[newSize];
			int[] newDocNoList = new int[newSize];
			System.arraycopy(segmentSequenceList, 0, newSegmentSequenceList, 0, size);
			System.arraycopy(docNoList, 0, newDocNoList, 0, size);
			segmentSequenceList = newSegmentSequenceList;
			docNoList = newDocNoList;
			
			if(bundleDocIdListArray != null) {
				DocIdList[] newBundleDocIdListArray = new DocIdList[newSize];
				System.arraycopy(bundleDocIdListArray, 0, newBundleDocIdListArray, 0, size);
				bundleDocIdListArray = newBundleDocIdListArray;
			}
			
			size = newSize;
		}
		
		segmentSequenceList[size] = segmentSequence;
		docNoList[size] = docNo;
		if(bundleDocIdList != null) {
			bundleDocIdListArray[size] = bundleDocIdList;
		}
		
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
	
	public DocIdList bundleDocIdList(int i) {
		return bundleDocIdListArray[i];
	}
}
