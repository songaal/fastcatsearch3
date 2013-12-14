package org.fastcatsearch.statistics;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

public class LogAggregatorTest {

	@Test
	public void test() {
		
		File tmpDir = new File("");
		File targetDir = new File("");
		
		int runSize = 1000;
		File[] inFileList = tmpDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				try{
					return FilenameUtils.getExtension(name).equals("log");
				}catch(Exception e){
					return false;
				}
			}
		});
		File outputFile = new File(targetDir, "0.log");
		
		LogAggregator tmpLogAggregator = null;//new LogAggregator(inFileList, new SearchLogFormatReader(), new LogCompatator(), runSize);
		tmpLogAggregator.aggregate(outputFile); //0.log
		
		
		
	}
	
	@Test
	public void test3() {
		String a = "	a	b	";
		int i = 0;
		for(String s : a.split("\t")){
			System.out.println(i + ": [" + s + "]");
		}
	}
}
