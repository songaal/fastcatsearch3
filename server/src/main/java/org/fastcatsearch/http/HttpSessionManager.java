package org.fastcatsearch.http;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.fastcatsearch.http.action.ActionResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * single thread 동작.
 * */
public class HttpSessionManager {
	private static final Logger logger = LoggerFactory.getLogger(HttpSessionManager.class);
	
	public final static String DefaultSessionCookie = "JSESSIONID";

	private final Map<String, HttpSession> sessionObjMap;
	private int expireTimeInHour;
	
	public HttpSessionManager() {
		sessionObjMap = new HashMap<String, HttpSession>();
	}

	public HttpSession getSessionObj(String sessionId) {
		HttpSession sessionObj = sessionObjMap.get(sessionId);
		if (sessionObj == null) {
			sessionObj = new HttpSession(sessionId);
			sessionObjMap.put(sessionId, sessionObj);
		}

		return sessionObj;
	}

	public HttpSession handleCookie(HttpRequest request, ActionResponse actionResponse) {
		String cookieString = request.getHeader(HttpHeaders.Names.COOKIE);
//		logger.debug("cookie >> {}", cookieString);
		HttpSession sessionObj = null;
		String responseCookie = "";
		boolean hasSessionCookie = false;
		
		if(cookieString != null){
			String[] cookieArray = cookieString.split(";");
			for (int i = 0; i < cookieArray.length; i++) {
				String cookie = cookieArray[i].trim();
				String[] kv = cookie.split("=");
				String key = null;
				String value = null;
				
				if(kv.length > 0){
					key = kv[0];
				}
				if(kv.length > 1){
					value = kv[1];
				}
				
				if (DefaultSessionCookie.equalsIgnoreCase(key) && value != null && value.length() > 0) {
					// 세션쿠키 발견.
					hasSessionCookie = true;
					sessionObj = sessionObjMap.get(value);
					if (sessionObj == null) {
						sessionObj = new HttpSession(value);
						sessionObjMap.put(value, sessionObj);
					}else{
//						logger.debug("세션객체 찾음. {} >> {}", kv[i], sessionObj.map());
					}
				} else {
					if (responseCookie.length() > 0) {
						responseCookie += ";";
					}
					responseCookie += cookie;
				}
	
			}
		}
		
		if (responseCookie.length() > 0) {
			actionResponse.setResponseCookie(responseCookie);
		}
		if (!hasSessionCookie) {
			// cookie가 없거나, 쿠키내에 JSESSIONID가 없다면 생성해서 Set-Cookie로 돌려준다.JSESSIONID=1bqe4dww377gy1c3pwqjzxbedj;Path=/admin
			//TODO expireTimeInHour를 날짜로 바꿔서 추가. ; Expires=Wed, 09 Jun 2021 10:18:14 GMT
			String newSessionId = newSessionId();
			sessionObj = new HttpSession(newSessionId);
			sessionObjMap.put(newSessionId, sessionObj);
			logger.debug("New Session Created! {} >> {}", newSessionId, sessionObj);
			actionResponse.setResponseSetCookie(DefaultSessionCookie + "=" + newSessionId +";Path=/"); //경로구분없이 모두 동일한 session을 타도록 함.
		}

		return sessionObj;
	}

	public void removeSession(String sessionId) {
		sessionObjMap.remove(sessionId);
	}

	private String newSessionId() {
		return UUID.randomUUID().toString();
	}

	public void setExpireTimeInHour(int expireTimeInHour) {
		this.expireTimeInHour = expireTimeInHour;
	}
}
