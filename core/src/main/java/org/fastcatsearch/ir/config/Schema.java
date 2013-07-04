///*
// * Copyright 2013 Websquared, Inc.
// * 
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * 
// *   http://www.apache.org/licenses/LICENSE-2.0
// * 
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.fastcatsearch.ir.config;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.fastcatsearch.ir.io.AsciiCharTrie;
//
//
///**
// * 각 컬렉션의 스키마를 정의하는 클래스이다.
// * 각 필드를 정의한 FieldSetting의 배열을 가지고 있고, 셋팅을 필드이름으로 검색하도록 해주는  fieldIndex를 유지한다.
// * AsciiCharTrie클래스를 사용하기 때문에 필드이름은 ascii 문자만 받아들일수 있다.
// * id필드를 가리키는 idIndex필드도 갖는다.
// * @author sangwook.song
// *
// */
//public class Schema {
//	private List<FieldSetting> fieldSettingList;
//	private List<IndexSetting> indexSettingList;
//	private List<SortSetting> sortSettingList;
//	private List<ColumnSetting> columnSettingList;
//	private List<GroupSetting> groupSettingList;
//	private List<FilterSetting> filterSettingList;
//	private int indexID;
//	public String collection; 
//	public AsciiCharTrie fieldnames;
//	public AsciiCharTrie indexnames;
//	public AsciiCharTrie sortnames;
//	public AsciiCharTrie columnnames;
//	public AsciiCharTrie groupnames;
//	public AsciiCharTrie filternames;
//	
//	public Schema(){
//		fieldSettingList = new ArrayList<FieldSetting>();
//		indexSettingList = new ArrayList<IndexSetting>();
//		sortSettingList = new ArrayList<SortSetting>();
//		columnSettingList = new ArrayList<ColumnSetting>();
//		groupSettingList = new ArrayList<GroupSetting>();
//		filterSettingList = new ArrayList<FilterSetting>();
//		indexID = -1;
//		fieldnames = new AsciiCharTrie();
//		indexnames = new AsciiCharTrie();
//		sortnames = new AsciiCharTrie();
//		columnnames = new AsciiCharTrie();
//		groupnames = new AsciiCharTrie();
//		filternames = new AsciiCharTrie();
//	}
//	
//	public void addFieldSetting(FieldSetting s){
//		fieldSettingList.add(s);
//	}
//	public void addIndexSetting(IndexSetting s){
//		indexSettingList.add(s);
//	}
//	public void addSortSetting(SortSetting s){
//		sortSettingList.add(s);
//	}
//	public void addColumnSetting(ColumnSetting s){
//		columnSettingList.add(s);
//	}
//	public void addGroupSetting(GroupSetting s){
//		groupSettingList.add(s);
//	}
//	public void addFilterSetting(FilterSetting s){
//		filterSettingList.add(s);
//	}
//	public int getIndexID(){
//		return indexID;
//	}
//	public void setIndexID(int indexID){
//		this.indexID = indexID;
//	}
//	
//	public FieldSetting getIDFieldSetting(){
//		return fieldSettingList.get(indexID);
//	}
//	public int getFieldSize(){
//		return fieldSettingList.size();
//	}
//	public List<FieldSetting> getFieldSettingList(){
//		return fieldSettingList;
//	}
//	
//	public List<IndexSetting> getIndexSettingList(){
//		return indexSettingList;
//	}
//	
//	public List<SortSetting> getSortSettingList(){
//		return sortSettingList;
//	}
//	
//	public List<ColumnSetting> getColumnSettingList(){
//		return columnSettingList;
//	}
//	
//	public List<FilterSetting> getFilterSettingList(){
//		return filterSettingList;
//	}
//	
//	public List<GroupSetting> getGroupSettingList(){
//		return groupSettingList;
//	}
//	
//}
//
