package org.fastcatsearch.http;

import org.fastcatsearch.http.action.ActionResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public interface HttpChannel {
	void sendResponse(ActionResponse response);
	void sendError(HttpResponseStatus status, Throwable e);
}
