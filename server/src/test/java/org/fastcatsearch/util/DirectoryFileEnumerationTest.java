package org.fastcatsearch.util;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryFileEnumerationTest {

	Logger logger = LoggerFactory.getLogger(DirectoryFileEnumerationTest.class);
	
	FileFilter fileAndDirFilter = new FileFilter(){
		
		@Override
		public boolean accept(File pathname) {
			return true;
		}
	};
	
	FileFilter onlyFileFilter = new FileFilter(){
		
		@Override
		public boolean accept(File pathname) {
			return !pathname.isDirectory();
		}
	};
	
	FileFilter onlyDirectoryFilter = new FileFilter(){
		
		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}
	};
	
	FileFilter testFilter = new FileFilter(){
		
		@Override
		public boolean accept(File pathname) {
			return pathname.getName().startsWith("$");
		}
	};
	
	
	
	@Test
	public void testListAllFiles() {
		String rootPath = "/Users/swsong/Documents";
		File rootFile = new File(rootPath);
		
		DirectoryFileEnumeration e = new DirectoryFileEnumeration(rootFile, onlyFileFilter);
		
		int count = 0;
		while(e.hasMoreElements()) {
			count++;
			File f = e.nextElement();
			logger.debug("{}] {} >> {}", count, f.isDirectory() ? "<Dir>" : f.length() + "b", f.getAbsolutePath());
		}
		
	}
	
	@Test
	public void testListManyFiles() {
		String rootPath = "/Volumes/fileserver";
		File rootFile = new File(rootPath);
		
		//특정시간 이후에 수정된 파일목록을 가져온다.
		Calendar cal = Calendar.getInstance();
		cal.set(2014, 5, 10);
		final long lastTime = cal.getTimeInMillis();
		FileFilter modifyTimeFilter = new FileFilter(){
			
			@Override
			public boolean accept(File pathname) {
				return pathname.lastModified() > lastTime;
			}
		};
		
		
		DirectoryFileEnumeration e = new DirectoryFileEnumeration(rootFile, modifyTimeFilter);
		
		int count = 0;
		long st = System.currentTimeMillis();
		long lap = System.currentTimeMillis();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		while(e.hasMoreElements()) {
			count++;
			File f = e.nextElement();
			logger.debug("{}] {} > {} >> {}", count, format.format(new Date(f.lastModified())), f.isDirectory() ? "<Dir>" : f.length() + "b", f.getAbsolutePath());
//			logger.debug("{}", e.getDirectoryQueueSize());
			if(count % 4000 == 0) {
//				logger.debug("{}] {} >> {}", count, f.isDirectory() ? "<Dir>" : f.length() + "b", f.getAbsolutePath());
				logger.debug("count {} > {} ms / {} ms", count, System.currentTimeMillis() - lap, System.currentTimeMillis() - st);
				lap = System.currentTimeMillis();
			}
		}
		logger.debug("Total count {} > {} ms / {} ms", count, System.currentTimeMillis() - lap, System.currentTimeMillis() - st);
		
	}

}
