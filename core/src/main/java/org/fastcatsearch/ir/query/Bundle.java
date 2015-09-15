package org.fastcatsearch.ir.query;

public class Bundle {

    public static final int OPT_PARENT_NOT_INCLUDE = 0;
    public static final int OPT_PARENT_INCLUDE = 1;

	private String fieldIndexId;
	private Sorts sorts;
	private int rows;
    private int option = OPT_PARENT_INCLUDE; ///대표포함 1, 미포함 0

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

    public int getOption() {
        return option;
    }

    public void setOption(int option) {
        this.option = option;
    }
}
