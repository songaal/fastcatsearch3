package org.fastcatsearch.ir.util.lang.ko;

import static org.junit.Assert.*;

import org.fastcatsearch.util.lang.ko.HangulUtil;
import org.junit.Test;

public class HangulUtilTest {

	@Test
	public void test() {
		String s = HangulUtil.makeHangulSuffix("빙그레", '\t');
		String[] ss = s.split("\t");
		for (int i = 0; i < ss.length; i++) {
			System.out.println(ss[i]);
		}
	}

}
