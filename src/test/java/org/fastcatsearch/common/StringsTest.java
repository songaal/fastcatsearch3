package org.fastcatsearch.common;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringsTest {

	@Test
	public void testHumanReadableTimeInterval() {
		for (int i = 0; i < Integer.MAX_VALUE / 1000; i++) {
			int time = i * 1000;
			System.out.println(i +"s : " + Strings.getHumanReadableTimeInterval(time));
		}
	}
	
	@Test
	public void testHumanReadableByteSize() {
		for (int i = 0; i < Integer.MAX_VALUE; i+=128) {
			System.out.println(i +" : " + Strings.getHumanReadableByteSize(i));
		}
	}

}
