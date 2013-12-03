package org.fastcatsearch.ir.settings;

public class SchemaInvalidateException extends Exception {

	private static final long serialVersionUID = -3011452458081007220L;
	
	private String section;
	private String field;
	private String type;

	public SchemaInvalidateException(String message){
		super(message);
	}
	
	public SchemaInvalidateException(String section, String field, String type) {
		this.section = section;
		this.field = field;
		this.type = type;
	}
	
	public String section() { return section; }
	public String field() { return field; }
	public String type() { return type; }
}
