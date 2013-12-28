package org.fastcatsearch.statistics.util;

import java.io.File;
import java.io.IOException;

import org.fastcatsearch.statistics.KeyCountRunEntryReader;
import org.junit.Test;

public class LogFileRunEntryReaderTest {

	@Test
	public void test() throws IOException {
		String destDir = "src/test/resources/statistics/rt/test";
		File file = new File(destDir, "1.log");
		KeyCountRunEntryReader reader = new KeyCountRunEntryReader(file, "utf-8");
		
		
		while(reader.next()){
			System.out.println("> "+reader.entry());
		}
		
		reader.close();
	}

}
