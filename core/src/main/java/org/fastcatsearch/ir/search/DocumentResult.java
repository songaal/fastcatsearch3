package org.fastcatsearch.ir.search;

import org.fastcatsearch.ir.query.Row;

public class DocumentResult {
	private Row[] rows;
	// 하위 묶음문서. 문서가 여러개이다.
	private Row[][] bundleRows;
	private String[] fieldIdList;

	private int pos; // read position

	public DocumentResult(Row[] rows, String[] fieldIdList) {
		this.rows = rows;
		this.fieldIdList = fieldIdList;
	}

	public DocumentResult(Row[] rows, Row[][] bundleRows, String[] fieldIdList) {
		this.rows = rows;
		this.bundleRows = bundleRows;
		this.fieldIdList = fieldIdList;
	}

	public Row[] rows() {
		return rows;
	}

	public Row[][] bundleRows() {
		return bundleRows;
	}

	public String[] fieldIdList() {
		return fieldIdList;
	}

	public Row row() {
		return rows[pos];
	}

	public Row[] bundleRow() {
		if (bundleRows != null) {
			return bundleRows[pos];
		} else {
			return null;
		}
	}

	public boolean next() {
		if (pos < rows.length) {
			pos++;
			return true;
		} else {
			return false;
		}
	}
}
