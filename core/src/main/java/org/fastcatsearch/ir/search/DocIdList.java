package org.fastcatsearch.ir.search;

public class DocIdList {
	private String[] segmentIdList;
	private int[] docNoList;
	private int size;
	
	private DocIdList[] bundleDocIdListArray;

	public DocIdList(){
		this(32);
	}
	public DocIdList(int initSize){
		segmentIdList = new String[initSize];
		docNoList = new int[initSize];
	}
	
	public void add(String segmentId, int docNo){
		add(segmentId, docNo, null);
	}
	
	public void add(String segmentId, int docNo, DocIdList bundleDocIdList){
		if(bundleDocIdList != null) {
			if(bundleDocIdListArray == null){
				bundleDocIdListArray = new DocIdList[segmentIdList.length];
			}
		}

		if(docNoList.length == size){
			int newSize = size * 2;
            String[] newSegmentIdList = new String[newSize];
			int[] newDocNoList = new int[newSize];
			System.arraycopy(segmentIdList, 0, newSegmentIdList, 0, size);
			System.arraycopy(docNoList, 0, newDocNoList, 0, size);
			segmentIdList = newSegmentIdList;
			docNoList = newDocNoList;
			
			if(bundleDocIdListArray != null) {
				DocIdList[] newBundleDocIdListArray = new DocIdList[newSize];
				System.arraycopy(bundleDocIdListArray, 0, newBundleDocIdListArray, 0, size);
				bundleDocIdListArray = newBundleDocIdListArray;
			}
		}
		
		segmentIdList[size] = segmentId;
		docNoList[size] = docNo;
		if(bundleDocIdList != null) {
			bundleDocIdListArray[size] = bundleDocIdList;
		}
		
		size++;
	}
	
	
	public String segmentId(int i){
		return segmentIdList[i];
	}
	
	public int docNo(int i){
		return docNoList[i];
	}
	
	public int size(){
		return size;
	}
	
	public DocIdList bundleDocIdList(int i) {
		if(bundleDocIdListArray == null) {
			return null;
		}
		return bundleDocIdListArray[i];
	}

}
