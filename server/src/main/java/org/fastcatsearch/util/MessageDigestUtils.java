package org.fastcatsearch.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageDigestUtils {

	protected static final Logger logger = LoggerFactory.getLogger(MessageDigestUtils.class);

	public static String getMD5String(String str) {
		try {
			return getMessageDigestString(MessageDigest.getInstance("MD5"), str);
		} catch (NoSuchAlgorithmException e) {
			logger.error("", e);
			return null;
		}
	}

	public static String getSHA1String(String str) {
		try {
			return getMessageDigestString(MessageDigest.getInstance("SHA-1"), str);
		} catch (NoSuchAlgorithmException e) {
			logger.error("", e);
			return null;
		}
	}

	public static String getSHA256String(String str) {
		try {
			return getMessageDigestString(MessageDigest.getInstance("SHA-256"), str);
		} catch (NoSuchAlgorithmException e) {
			logger.error("", e);
			return null;
		}
	}

	public static String getMessageDigestString(MessageDigest md, String str) {
		return getMessageDigestString(md, str.getBytes());
	}

	public static String getMessageDigestString(MessageDigest md, byte[] bytes) {
		md.update(bytes);
		byte byteData[] = md.digest();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) {
			String hex = Integer.toHexString(byteData[i] & 0xff);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();

	}
}
