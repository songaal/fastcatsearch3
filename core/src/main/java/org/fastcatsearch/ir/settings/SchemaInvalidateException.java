package org.fastcatsearch.ir.settings;

public class SchemaInvalidateException extends Exception {

	private static final long serialVersionUID = -3011452458081007220L;
	
	private String section;
	private String fieldId;
	private String attributeId;
	private String value;
	private String type;

	public SchemaInvalidateException(String message){
		super(message);
	}
	
	public SchemaInvalidateException(String section, String fieldId, String attributeId, String value, String type) {
		this.section = section;
		this.fieldId = fieldId;
		this.attributeId = attributeId;
		this.value = value;
		this.type = type;
	}
	
	@Override
	public String getMessage() {
		return "["+section + "] " + fieldId + " " + attributeId + " \"" +value+ "\" " + type + ".";
		
	}

}
