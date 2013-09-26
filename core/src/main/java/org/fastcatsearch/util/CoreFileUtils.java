package org.fastcatsearch.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class CoreFileUtils {
	public static void removeDirectoryCascade(File dirNumber) throws IOException {
		FileUtils.deleteDirectory(dirNumber);
		try {
			File dir = dirNumber.getParentFile();
			String num = dirNumber.getName();
			int i = Integer.parseInt(num) + 1;
			while (i <= Integer.MAX_VALUE) {
				File f = new File(dir, Integer.toString(i));
				if (f.exists()) {
					FileUtils.deleteDirectory(f);
					i++;
				} else {
					// 순차번호이므로 없으면 loop 탈출.
					break;
				}
			}
		} catch (NumberFormatException ignore) {
		}
	}
}
