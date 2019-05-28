package org.fastcatsearch.util;

import java.io.File;

public class FileUtils extends org.apache.commons.io.FileUtils {

	public static long sizeOfDirectorySafe(File directory) {
		String message;
		if (!directory.exists()) {
			return 0;
		} else if (!directory.isDirectory()) {
			return 0;
		} else {
			long size = 0L;
			File[] files = directory.listFiles();
			if (files == null) {
				return 0L;
			} else {
				File[] arr = files;
				int len$ = files.length;

				for(int i = 0; i < len$; ++i) {
					File file = arr[i];
					size += sizeOfSafe(file);
				}

				return size;
			}
		}
	}

	public static long sizeOfSafe(File file) {
		if (!file.exists()) {
			return 0;
		} else {
			return file.isDirectory() ? sizeOfDirectorySafe(file) : file.length();
		}
	}
}
