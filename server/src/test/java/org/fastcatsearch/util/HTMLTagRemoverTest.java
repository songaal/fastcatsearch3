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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.fastcatsearch.ir.common.IRException;
import org.junit.Test;


public class HTMLTagRemoverTest extends TestCase {
	public void test1(){
		HttpClient httpclient = new DefaultHttpClient();
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		HttpPost httpost = new HttpPost("http://www.fastcatsearch.org/");
		HttpGet httpGet = new HttpGet("http://www.fastcatsearch.org/");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		

		try {
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			
			String responseBody = httpclient.execute(httpGet, responseHandler);
			
            
			System.out.println(HTMLTagRemover.clean(responseBody));
            
            
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void test2(){
		
			try {
				System.out.println(HTMLTagRemover.clean("<img src=\"sdfsdfds.jp\"> 홍삼 저"));
			} catch (IRException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
         
		
	}
	
	@Test
	public void test3() {
		String str = "김치냉장고_스탠드형|232L|2룸|소비전력:17.4kwh(월)|나노항균|냉장+냉동겸용|야채,과일보관|색상:함연주화이트|<IMG src=\"http://office.danawa.com/prod_img/500000/975/502/img/1502975_1.jpg?time=1348054028\" style=\"FILTER: RevealTrans(duration=0,transition=X)\" OnmouseOver=\"this.filters[0].apply(); this.src='http:";
		
		try {
			str = HTMLTagRemover.clean(str);
			System.out.println(str);
		} catch (IRException e) {
			e.printStackTrace();
		}
	}

    @Test
    public void test4() {
        String str = "abc4.0  qwe 3.0 tyu 9.0 \n123   \n\n456\n789";

        try {
            str = HTMLTagRemover.clean(str);
            System.out.println(str);
        } catch (IRException e) {
            e.printStackTrace();
        }
    }

	@Test
	public void testfile() throws Exception {
		String strFilePath="/Users/swsong/Desktop/a.html";
		
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(strFilePath), "UTF-8") );
			String line = null;
			while((line = reader.readLine()) != null){
				sb.append(line).append("\r");
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String str = HTMLTagRemover.clean(sb.toString());
		System.out.println(str);
	}
}
