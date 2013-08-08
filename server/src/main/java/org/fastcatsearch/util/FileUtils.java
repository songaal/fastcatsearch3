package org.fastcatsearch.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class FileUtils extends org.apache.commons.io.FileUtils {
	
	public static void cleanCollectionDataDirectorys(File collectionDir) throws IOException{
		
		File[] dataDirList = collectionDir.listFiles(new FileFilter(){

			@Override
			public boolean accept(File pathname) {
				if(pathname.getName().startsWith("data") && pathname.isDirectory()){
					return true;
				}
				return false;
			}
			
		});
		
		for(File dataDir : dataDirList){
			org.apache.commons.io.FileUtils.deleteDirectory(dataDir);
		}
		
	}
}
