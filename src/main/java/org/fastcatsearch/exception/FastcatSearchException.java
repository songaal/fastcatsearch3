package org.fastcatsearch.exception;

import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.fastcatsearch.common.ResourceBundleControl;

public class FastcatSearchException extends Exception {

	private static final long serialVersionUID = 6912432335359997648L;

	private String errorCode;
	private Object[] params;
	private Throwable cause;
	
	public FastcatSearchException() {
	}

	public FastcatSearchException(Throwable cause){
		this(null, cause);
	}
			
	public FastcatSearchException(String errorCode, Object... params) {
		this.errorCode = errorCode;
		this.params = params;
	}

	public FastcatSearchException(String errorCode, Throwable cause, Object... params) {
		//FastcatSearchException을 다시한번 감싸는 것은 허용하지 않는다.
		//원인 코드가 가려질수 있기때문에..
		if(cause instanceof FastcatSearchException){
			FastcatSearchException exception = (FastcatSearchException) cause;
			this.errorCode = exception.errorCode;
			this.cause = exception.cause;
			this.params = exception.params;
		}else{
			this.errorCode = errorCode;
			this.cause = cause;
			this.params = params;
		}
	}

	@Override
	public Throwable getCause() {
        return (cause==this ? null : cause);
    }
	
	public String errorCode() {
		return errorCode;
	}

	@Override
	public String toString() {
		ResourceBundle resourceBundle = ResourceBundle.getBundle("org.fastcatsearch.exception.FastcatSearchErrorCode_ko_KR", new ResourceBundleControl(Charset.forName("UTF-8")));
		if (resourceBundle != null) {
			
			if(errorCode != null){
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
			}else{
				//errorCode를 정의하지 않았다면 즉, throwable을 그대로 감싸서 던졌다면..
				return cause.toString();
			}
		} else {
			return null;
		}
	}
}
