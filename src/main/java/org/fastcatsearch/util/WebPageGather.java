package org.fastcatsearch.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebPageGather {
	private final static Logger logger = LoggerFactory.getLogger(WebPageGather.class);
	private static String LINK_METHOD_GET = "GET";
	private static String LINK_METHOD_POST = "POST";
	private HttpClient httpClient;
	private static String newLine = System.getProperty("line.separator");
	private Map<String, HttpContext> contextMap;
	public WebPageGather(){
		this.contextMap = new ConcurrentHashMap<String, HttpContext>();
		this.httpClient = HttpClientWrapper.wrapClient(new DefaultHttpClient());
	}
	
	public String getLinkPageContent(String strUrl, String encoding, String method) {
		logger.debug("Link Page url={}, encoding={}, method={}", new Object[]{strUrl, encoding, method});
		HttpUriRequest request = null;
		HttpContext httpContext = null;
		
		int startPos = strUrl.indexOf("//") + 2;
		int pos = strUrl.indexOf('/', startPos);
		String domain = null;
		if (pos < 0) {
			domain = strUrl.substring(0);
		} else {
			domain = strUrl.substring(0, pos);
		}
		
		httpContext = contextMap.get(domain);
		if (httpContext == null) {
			httpContext = new BasicHttpContext();
			contextMap.put(domain, httpContext);
		}
		
		if (LINK_METHOD_POST.equalsIgnoreCase(method)) {
			request = new HttpPost(getEncodedUrl(strUrl, encoding));
		} else if (LINK_METHOD_GET.equalsIgnoreCase(method)){
			request = new HttpGet(getEncodedUrl(strUrl, encoding));
		}

		BufferedReader br = null;
		InputStream is = null;
		try {
			CookieStore cookieStore = (CookieStore)httpContext.getAttribute(ClientContext.COOKIE_STORE);
			List<Cookie> cookieList = null;
			if(cookieStore != null)
				cookieList = cookieStore.getCookies();
			if(cookieList != null){
				logger.trace("LinkPage Cookie size : {}", cookieList.size());
				for(Cookie c : cookieList){
					logger.trace("Cookie : {}", c.toString());
				}
			}
			HttpResponse response = httpClient.execute(request, httpContext);
			logger.debug("Link Status >> {}", response.getStatusLine());
			if(response.getStatusLine().getStatusCode() >= 400){
				//문제발생.
				logger.error("링크페이지에서 에러가 발생하였습니다. status={}", response.getStatusLine());
				response.getEntity().getContent().close();
				return "";
			}
			is = new BufferedHttpEntity(response.getEntity()).getContent();
			br = new BufferedReader(new InputStreamReader(is,encoding));
			String s = "";
			StringBuffer sb = new StringBuffer();
			while ((s = br.readLine()) != null) {
				sb.append(s);
				sb.append(newLine);
			}
			br.close();
			is.close();
			return sb.toString();
		} catch (ClientProtocolException e) {
			logger.error("",e);
			try {
				if(br != null)
					br.close();
				if(is != null)
					is.close();
			} catch (IOException e1) { }
		} catch (IOException e) {
			logger.error("",e);
			try {
				if(br != null)
					br.close();
				if(is != null)
					is.close();
			} catch (IOException e1) { }
		} 
		return "";
	}

	private String getEncodedUrl(String urlString, String encoding){
		int p = urlString.indexOf("?");
		if(p < 0){
			return urlString;
		}else{
			StringBuffer urlBuffer = new StringBuffer();
			String frontUrl = urlString.substring(0, p);
			urlBuffer.append(frontUrl);
			String paramString = urlString.substring(p + 1);
			String[] params = paramString.split("&");
			int i = 0;
			for (String param : params) {
				int p2 = param.indexOf("=");
				String name = null;
				String value = null;
				if(p2 < 0){
					continue;
				}else{
					name = param.substring(0, p2).trim();
					value = param.substring(p2 + 1).trim();
				}
				
		        if(value.length() > 0){
			        try {
						value = URLEncoder.encode(value, encoding);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
			        if(i == 0){
			        	urlBuffer.append("?");
			        }else{
			        	urlBuffer.append("&");
			        }
			        urlBuffer.append(name);
			        urlBuffer.append("=");
			        urlBuffer.append(value);
			        i++;
		        }
		    }
			return urlBuffer.toString();
		
		}
	}
}
