package org.fastcatsearch.exception;

import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.fastcatsearch.common.ResourceBundleControl;

public class FastcatSearchException extends Exception {

	private static final long serialVersionUID = 6912432335359997648L;

	private Throwable cause;
	private String errorMessage;
	
	public FastcatSearchException() {
	}

	public FastcatSearchException(Throwable cause){
		this(null, cause);
	}
	
	public FastcatSearchException(String errorCode, Object... params) {
		this(errorCode, null, params);
	}

	public FastcatSearchException(String errorCode, Throwable cause, Object... params) {
		//FastcatSearchException을 다시한번 감싸는 것은 허용하지 않는다.
		//원인 코드가 가려질수 있기때문에..
//		if(cause instanceof FastcatSearchException){
//			FastcatSearchException exception = (FastcatSearchException) cause;
//			this.cause = exception.cause;
//		}else{
//			this.cause = cause;
//		}
		
		
		ResourceBundle resourceBundle = ResourceBundle.getBundle("org.fastcatsearch.exception.FastcatSearchErrorCode", new ResourceBundleControl(Charset.forName("UTF-8")));
		if (resourceBundle != null) {
			
			if(errorCode != null){
				try{
					if(params != null){
						errorMessage = errorCode + ": " + MessageFormat.format(resourceBundle.getString(errorCode), params);
					}else{
						errorMessage = errorCode + ": " + resourceBundle.getString(errorCode);
					}
				}catch(Exception e){
				}
			}else{
				//errorCode를 정의하지 않았다면 즉, throwable을 그대로 감싸서 던졌다면..
			}
		}
		
		if(errorMessage == null){
			errorMessage = "Uncategorized Error: "+errorCode;
		}
		this.cause = cause;
			
	}

	@Override
	public String getMessage(){
		return errorMessage;
	}
	@Override
	public Throwable getCause() {
        return (cause==this ? null : cause);
    }

	@Override
	public String toString(){
		StringBuffer causeString = null;
		Throwable cause = this.cause;
		int i = 0;
		while(cause != null) {
			if(causeString == null) {
				causeString = new StringBuffer();
			}
			if(i++ > 0) {
				causeString.append(" >> ");	
			}
			causeString.append(cause.getMessage());
			cause = cause.getCause();
		}
		return getClass().getName() + ": " +errorMessage + (causeString != null ? " [Cause]" + causeString : "");
	}
}
