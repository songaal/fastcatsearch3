package org.fastcatsearch.ir.group;

/**
 * 그룹결과의 완전체.
 * 중간결과는 GroupResult 참조.
 * */
public class GroupResults {
	private int totalSearchCount;
	private int groupSize;
	private GroupResult[] groupResultList;
	
	public GroupResults(int initSize){
		groupResultList = new GroupResult[initSize];
	}
	
	public GroupResults(int initSize, int totalSearchCount){
		this(initSize);
		this.totalSearchCount = totalSearchCount;
	}
	
	public void add(GroupResult groupResult){
		groupResultList[groupSize++] = groupResult;
	}
	
	public GroupResult[] groupResultList(){
		return groupResultList;
	}
	
	public GroupResult getGroupResult(int i){
		return groupResultList[i];
	}
	public int totalSearchCount(){
		return totalSearchCount;
	}

	public int groupSize() {
		return groupSize;
	}
}
