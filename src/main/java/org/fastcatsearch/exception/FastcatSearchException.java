package org.fastcatsearch.exception;

import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.fastcatsearch.common.ResourceBundleControl;

public class FastcatSearchException extends Exception {

	private static final long serialVersionUID = 6912432335359997648L;

	private String errorCode;
	private Object[] params;
	
	public FastcatSearchException() {
	}

	public FastcatSearchException(String errorCode, Object... params) {
		this.errorCode = errorCode;
		this.params = params;
	}

	public FastcatSearchException(String errorCode, Throwable cause, Object... params) {
		super(cause);
		this.errorCode = errorCode;
		this.params = params;
	}

	public String errorCode() {
		return errorCode;
	}

	@Override
	public String toString() {
		ResourceBundle resourceBundle = ResourceBundle.getBundle("org.fastcatsearch.exception.FastcatSearchErrorCode_ko_KR", new ResourceBundleControl(Charset.forName("UTF-8")));
		if (resourceBundle != null) {
			try{
				String errorMessage = "";
				if(params != null){
					errorMessage += errorCode + ": " + MessageFormat.format(resourceBundle.getString(errorCode), params);
				}else{
					errorMessage += errorCode + ": " + resourceBundle.getString(errorCode);
				}
//				errorMessage += "\n\tat " + getStackTrace()[0];
//				if(getCause() != null){
//					errorMessage += (", " + getCause());
//				}
				return errorMessage;
			}catch(Exception e){
				return "";
			}
		} else {
			return null;
		}
	}
}
