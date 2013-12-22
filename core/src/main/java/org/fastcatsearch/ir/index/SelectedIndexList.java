package org.fastcatsearch.ir.index;

import java.util.ArrayList;
import java.util.List;

/**
 * 색인시 참조할 인덱스필드 선택사항.
 * 
 * 여기에 존재하는 필드만 색인하고, null이면 모든필드를 색인한다.
 * list가 null이 아니면서 비어있으면 아무 필드도 색인하지 않는다. 
 * 
 * */
public class SelectedIndexList {
	
	private boolean isPrimaryKeyIndexSelected;
	private List<String> searchIndexList;
	private List<String> fieldIndexList;
	private List<String> groupIndexList;
	
	//선택없이 모두 색인하고자 할때 사용.
	public static SelectedIndexList ALL_INDEXING = new SelectedIndexList(true, null, null, null);
	
	public SelectedIndexList(){
		searchIndexList = new ArrayList<String>();
		fieldIndexList = new ArrayList<String>();
		groupIndexList = new ArrayList<String>();
	}
	public SelectedIndexList(boolean isPrimaryKeyIndexSelected, List<String> searchIndexList, List<String> fieldIndexList, List<String> groupIndexList) {
		this.isPrimaryKeyIndexSelected = isPrimaryKeyIndexSelected;
		this.searchIndexList = searchIndexList;
		this.fieldIndexList = fieldIndexList;
		this.groupIndexList = groupIndexList;
	}

	public void addSearchIndex(String indexId){
		searchIndexList.add(indexId);
	}
	public void addFieldIndex(String indexId){
		fieldIndexList.add(indexId);
	}
	public void addGroupIndex(String indexId){
		groupIndexList.add(indexId);
	}
	
	public boolean isPrimaryKeyIndexSelected() {
		return isPrimaryKeyIndexSelected;
	}

	public void setPrimaryKeyIndexSelected(boolean isPrimaryKeyIndexSelected) {
		this.isPrimaryKeyIndexSelected = isPrimaryKeyIndexSelected;
	}

	public List<String> getSearchIndexList() {
		return searchIndexList;
	}

	public void setSearchIndexList(List<String> searchIndexList) {
		this.searchIndexList = searchIndexList;
	}

	public List<String> getFieldIndexList() {
		return fieldIndexList;
	}

	public void setFieldIndexList(List<String> fieldIndexList) {
		this.fieldIndexList = fieldIndexList;
	}

	public List<String> getGroupIndexList() {
		return groupIndexList;
	}

	public void setGroupIndexList(List<String> groupIndexList) {
		this.groupIndexList = groupIndexList;
	}
	
	
}
