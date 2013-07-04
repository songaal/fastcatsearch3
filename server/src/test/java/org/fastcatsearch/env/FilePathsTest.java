package org.fastcatsearch.env;

import static org.junit.Assert.*;

import org.fastcatsearch.common.FastcatSearchTest;
import org.junit.Test;

public class FilePathsTest extends FastcatSearchTest {

	@Test
	public void testPath() {
		FilePaths filepaths = new FilePaths(environment);
		String home = environment.home();
		String filePath = filepaths.makePath("a").append("b").append("c.txt").toString();
		String FS = Environment.FILE_SEPARATOR;
		String expected = home+"a"+FS+"b"+FS+"c.txt";
		assertTrue(expected.equals(filePath));
		
		String filePath2 = filepaths.makeRelativePath("a").append("b").append("c.txt").toString();
		String expected2 = "a"+FS+"b"+FS+"c.txt";
		assertTrue(expected2.equals(filePath2));
	}

}
