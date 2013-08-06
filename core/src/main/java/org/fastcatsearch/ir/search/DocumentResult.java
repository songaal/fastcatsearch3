package org.fastcatsearch.ir.search;

import org.fastcatsearch.ir.query.Row;

public class DocumentResult {
	private Row[] rows;
	private String[] fieldIdList;
	
	private int pos; //read position
	
	public DocumentResult(Row[] rows, String[] fieldIdList){
		this.rows = rows;
		this.fieldIdList = fieldIdList;
	}
	
	public Row[] rows(){
		return rows;
	}
	
	public String[] fieldIdList(){
		return fieldIdList;
	}

	public Row next() {
		return rows[pos++];
	}
}
