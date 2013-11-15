package org.fastcatsearch.http;

import java.util.Map;

public class SessionInfo {
	
	private String userId;

	private Map<ActionAuthority, ActionAuthorityLevel> authorityMap;
	
	public SessionInfo(String userId, Map<ActionAuthority, ActionAuthorityLevel> authorityMap){
		this.userId = userId;
		this.authorityMap = authorityMap;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public Map<ActionAuthority, ActionAuthorityLevel> getAuthorityMap() {
		return authorityMap;
	}

	public void setAuthorityMap(Map<ActionAuthority, ActionAuthorityLevel> authorityMap) {
		this.authorityMap = authorityMap;
	}
	
	public boolean hasAuthority(ActionAuthority actionAuthority, ActionAuthorityLevel actionAuthorityLevel){
		if(actionAuthority == null || actionAuthorityLevel == null){
			return false;
		}
		
		ActionAuthorityLevel groupAuthoritylevel = authorityMap.get(actionAuthority);
		if(groupAuthoritylevel != null){
			return groupAuthoritylevel.isLargerThan(actionAuthorityLevel);
		}
		
		//if authority is not defined, it has no authority.
		return false;
	}
}
