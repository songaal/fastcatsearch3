package org.fastcatsearch.transport.common;

import java.util.Map;

import org.fastcatsearch.control.ResultFuture;

public class SendFileResultFuture extends ResultFuture {

	private boolean cancel;
	
	public SendFileResultFuture(long requestId, Map<Long, ResultFuture> resultFutureMap) {
		super(requestId, resultFutureMap);
	}
	
	public void cancel(){
		cancel = true;
	}
	
	public boolean isCanceled(){
		return cancel;
	}
	
}
