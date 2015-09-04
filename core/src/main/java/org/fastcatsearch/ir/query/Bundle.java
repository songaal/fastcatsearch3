package org.fastcatsearch.ir.query;

public class Bundle {
	private String fieldIndexId;
	private Sorts sorts;
	private int rows;
	public Bundle(String fieldIndexId) {
		this.fieldIndexId = fieldIndexId;
	}
	
	public String getFieldIndexId() {
		return fieldIndexId;
	}
	
	@Override
	public String toString(){
		return fieldIndexId + ":" + rows + ";" + (sorts != null ? sorts.toString() : "");
	}

	public Sorts getSorts() {
		return sorts;
	}
	public void setSorts(Sorts sorts) {
		this.sorts = sorts;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

}
