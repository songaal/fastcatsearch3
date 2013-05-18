package org.fastcatsearch.common;

import java.nio.charset.Charset;
import java.util.ResourceBundle;

import org.junit.Test;

public class ResourceBundleReadTest {

	@Test
	public void test() {
		ResourceBundle bundle = ResourceBundle.getBundle("org.fastcatsearch.exception.FastcatSearchErrorCode_ko_KR", new ResourceBundleControl(Charset.forName("UTF-8")));
		String value = bundle.getString("ERR-00110");
		System.out.println(value);
	}

}
