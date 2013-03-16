package org.fastcatsearch.transport.common;

import java.util.Map;

public class SendFileResultFuture extends ResultFuture {

	private boolean cancel;
	
	public SendFileResultFuture(long requestId,
			Map<Long, ResultFuture> resultFutureMap, long sentTime) {
		super(requestId, resultFutureMap, sentTime);
	}
	
	public void cancel(){
		cancel = true;
	}
	
	public boolean isCanceled(){
		return cancel;
	}
	
}
