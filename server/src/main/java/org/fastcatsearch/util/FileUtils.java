package org.fastcatsearch.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class FileUtils extends org.apache.commons.io.FileUtils {

	
	public static void copyPrefixFileToDirectory(final String pattern, File sourceDir, File targetDir) throws IOException{
		File[] sourceFileList = sourceDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				if (file.getName().startsWith(pattern) && !file.isDirectory()) {
					return true;
				}
				return false;
			}

		});
		
		for (File srcFile : sourceFileList) {
			copyFileToDirectory(srcFile, targetDir);
		}
	}
	
	public static void cleanCollectionDataDirectorys(File collectionDir) throws IOException {

		File[] dataDirList = collectionDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				if (pathname.getName().startsWith("data") && pathname.isDirectory()) {
					return true;
				}
				return false;
			}

		});

		for (File dataDir : dataDirList) {
			//deleteDirectory(dataDir);
			forceDelete(dataDir);
		}

	}

	// 숫자명의 디렉토리 삭제.
	// 1이면 2,3,4 도 존재시 모두 삭제.
	public static void removeDirectoryCascade(File dirNumber) throws IOException {
		//FileUtils.deleteDirectory(dirNumber);
		FileUtils.forceDelete(dirNumber);
		try {
			File dir = dirNumber.getParentFile();
			String num = dirNumber.getName();
			int i = Integer.parseInt(num) + 1;
			while (i <= Integer.MAX_VALUE) {
				File f = new File(dir, Integer.toString(i));
				if (f.exists()) {
					//FileUtils.deleteDirectory(f);
					FileUtils.forceDelete(f);
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
