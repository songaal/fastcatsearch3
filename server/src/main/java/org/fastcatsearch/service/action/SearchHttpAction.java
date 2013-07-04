package org.fastcatsearch.service.action;

import java.io.UnsupportedEncodingException;

import org.fastcatsearch.http.HttpChannel;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class SearchHttpAction extends HttpAction {

	public SearchHttpAction(HttpRequest request, HttpChannel httpChannel) {
		super(request, httpChannel);
	}

	@Override
	public void doAction(HttpRequest request, ActionResponse response) throws ActionException {
		String result = "결과JSON";
		byte[] buffer = null;
		try {
			buffer = result.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		response.setContentType("");
		response.setContent(buffer, 0, buffer.length);
	}

}
