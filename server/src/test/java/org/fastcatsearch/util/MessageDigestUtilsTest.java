package org.fastcatsearch.util;

import static org.junit.Assert.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.junit.Test;

public class MessageDigestUtilsTest {

	@Test
	public void test() throws NoSuchAlgorithmException {

		MessageDigest md = null;
		byte[] bytes = new byte[1024 * 1024 * 32];
		Random r = new Random(System.currentTimeMillis());
		r.nextBytes(bytes);

		getCode(MessageDigest.getInstance("MD5"), bytes);

		getCode(MessageDigest.getInstance("SHA-1"), bytes);

		getCode(MessageDigest.getInstance("SHA-256"), bytes);

	}

	private void getCode(MessageDigest md, byte[] bytes) {
		long st = System.nanoTime();
		String code = MessageDigestUtils.getMessageDigestString(md, bytes);
		System.out.println(md + " > " + code + " , time = " + (System.nanoTime() - st) / (1000 * 1000) + "ms");
	}
	
	@Test
	public void test2() {
		System.out.println(MessageDigestUtils.getSHA1String("1111"));
	}

}
