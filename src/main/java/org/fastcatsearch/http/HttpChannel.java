package org.fastcatsearch.http;

import org.fastcatsearch.service.action.ActionResponse;

public interface HttpChannel {
	void sendResponse(ActionResponse response);
}
