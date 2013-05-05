/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.util;

import org.fastcatsearch.ir.common.IRException;

import junit.framework.TestCase;


public class HTMLTagRemoverTest extends TestCase {
	public void test1(){
//		HttpClient httpclient = new DefaultHttpClient();
//		ResponseHandler<String> responseHandler = new BasicResponseHandler();
//		HttpPost httpost = new HttpPost("http://www.naver.com/");
//
//		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
//		
//
//		try {
//			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
//			
//			String responseBody = httpclient.execute(httpost,responseHandler);
//			
//            
//			System.out.println(HTMLTagRemover.clean(responseBody));
//            
//            
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClientProtocolException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IRException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	public void test2(){
		
			try {
				System.out.println(HTMLTagRemover.clean("<img src=\"sdfsdfds.jp\"> 홍삼 저"));
			} catch (IRException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
         
		
	}
}
