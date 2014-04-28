package org.fastcatsearch.http.action;

import java.io.Writer;
import java.util.Map;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.SessionInfo;
import org.fastcatsearch.util.ResponseWriter;

public abstract class AuthAction extends ServiceAction {

	public final static String AUTH_KEY = "__auth";
	
	private ActionAuthority authority;
	private ActionAuthorityLevel authorityLevel;
	
	abstract public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception;

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {
		writeHeader(response);
		
		if(session == null){
			return;
		}
		Object obj = session.getAttribute(AUTH_KEY);
		
		logger.trace("session obj = {}", obj);
		
		try {
			
			if (obj == null) {
				// 인증 안되어 있음.
				doNotAuthenticatedResult(request, response, "Not Authenticated.");
			} else {
				
				SessionInfo sessionInfo = (SessionInfo)obj;
				
				Map<ActionAuthority, ActionAuthorityLevel> authorityMap = sessionInfo.getAuthorityMap();
				
				ActionAuthorityLevel currentLevel = authorityMap.get(authority);
				
				if (currentLevel == null) {
					currentLevel = ActionAuthorityLevel.NONE;
				}
				
				if(logger.isTraceEnabled()) {
					logger.trace(
							"authority:{} requireLevel:{} / currentLevel:{} [{}]",
							authority, authorityLevel, currentLevel,
							currentLevel.isLargerThan(authorityLevel));
				}
				
				if (authority == ActionAuthority.NULL
						|| (authority != ActionAuthority.NULL && currentLevel
								.isLargerThan(authorityLevel))) {
					logger.trace("authorized");					
					doAuthAction(request, response);
				} else {
					doNotAuthenticatedResult(request, response, "Not Authorized.");
				}
				
			}
		} finally {
			Writer writer = response.getExistWriter();
			if(writer != null) {
				writer.close();
			}
		}
	}

	private void doNotAuthenticatedResult(ActionRequest request, ActionResponse response, String message) throws Exception {
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		resultWriter.object().key("error").value(message).endObject();
		resultWriter.done();
		writer.close();
	}

	public void setAuthority(ActionAuthority authority, ActionAuthorityLevel authorityLevel) {
		this.authority = authority;
		this.authorityLevel = authorityLevel;
	}
}
