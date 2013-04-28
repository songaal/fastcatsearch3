package org.fastcatsearch.exception;

import java.io.IOException;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.common.io.Streamable;

public class SystemException extends Exception implements Streamable {
	
	private SystemExceptionCode exceptionCode;
	
	public SystemException(){ }
	
	public SystemException(SystemExceptionCode exceptionCode, String message, Throwable cause){
		super(message, cause);
		this.exceptionCode = exceptionCode;
	}

	@Override
	public void readFrom(StreamInput input) throws IOException {
		
	}

	@Override
	public void writeTo(StreamOutput output) throws IOException {
		
	}
	
	
}
