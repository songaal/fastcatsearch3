package org.fastcatsearch.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.junit.Test;

public class SearchProxyTest {

	@Test
	public void test() throws IOException {
		String stringToReverse = URLEncoder.encode("", "UTF-8");

		String urlString = "http://localhost:8090/search/json";
		String paramString = "cn=sample&sn=1&ln=10&fl=title&se={title:약관으로}";
		
		URL url = new URL(urlString);
		URLConnection connection = url.openConnection();
		connection.setDoOutput(true);

		OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
		out.write(paramString);
		out.close();

		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String decodedString = null;
		while ((decodedString = in.readLine()) != null) {
			System.out.println(decodedString);
		}
		in.close();
	}

}
