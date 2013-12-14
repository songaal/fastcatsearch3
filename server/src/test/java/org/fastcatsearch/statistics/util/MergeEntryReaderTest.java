package org.fastcatsearch.statistics.util;

import static org.junit.Assert.*;

import java.io.File;

import org.fastcatsearch.statistics.LogFileRunEntryReader;
import org.junit.Test;

public class MergeEntryReaderTest {

	@Test
	public void testLogFileMerge() {
		File[] fileList = null;
		
		
		LogFileRunEntryReader[] entryReaderList = getReaderList(fileList); 
		MergeEntryReader<LogFileRunEntry> reader = new MergeEntryReader<LogFileRunEntry>(entryReaderList);
		
		try{
			LogFileRunEntry entry = null;
			
			while((entry = reader.read()) != null){
				System.out.println(entry);
			}
			
		}finally{
			for(LogFileRunEntryReader r : entryReaderList){
				r.close();
			}
		}
	}

	private LogFileRunEntryReader[] getReaderList(File[] fileList){
		LogFileRunEntryReader[] list = new LogFileRunEntryReader[fileList.length];
		for(int i =0;i<fileList.length; i++){
			File f = fileList[i];
			list[i] = new LogFileRunEntryReader(f);
		}
		return list;
	}
}
