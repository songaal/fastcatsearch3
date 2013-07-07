package org.fastcatsearch.env;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.fastcatsearch.common.FastcatSearchTest;
import org.junit.Test;

public class FilePathsTest extends FastcatSearchTest {

	@Test
	public void testPath() {
		Path filepaths = new Path(new File("."));
		String home = environment.home();
		String filePath = filepaths.path("a", "b", "c.txt").toString();
		String FS = Environment.FILE_SEPARATOR;
		String expected = home+"a"+FS+"b"+FS+"c.txt";
		assertTrue(expected.equals(filePath));
		
		String filePath2 = filepaths.makeRelativePath("a").path("b", "c.txt").toString();
		String expected2 = "a"+FS+"b"+FS+"c.txt";
		assertTrue(expected2.equals(filePath2));
	}

}
