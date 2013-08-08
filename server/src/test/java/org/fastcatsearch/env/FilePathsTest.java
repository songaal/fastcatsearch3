package org.fastcatsearch.env;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.fastcatsearch.util.FileUtils;
import org.junit.Test;

public class FilePathsTest {

	@Test
	public void testPath() {
		Path filepaths = new Path(new File("."));
		String home = "/tmp/";
		String filePath = filepaths.path("a", "b", "c.txt").toString();
		String FS = Environment.FILE_SEPARATOR;
		String expected = home+"a"+FS+"b"+FS+"c.txt";
		assertTrue(expected.equals(filePath));
		
		String filePath2 = filepaths.makeRelativePath("a").path("b", "c.txt").toString();
		String expected2 = "a"+FS+"b"+FS+"c.txt";
		assertTrue(expected2.equals(filePath2));
	}
	
	@Test
	public void testRelativePath() {
		File directory = new File("src/main/java");
		File home = new File("/Users/swsong/git-stable/fastcatsearch/server/src/main/java");
		Path homePath = new Path(home);
		Collection<File> files = FileUtils.listFiles(directory, null, true);
		Iterator<File> fileIterator = files.iterator();
		while(fileIterator.hasNext()){
			File file = fileIterator.next();
//			System.out.println(file+", "+file.toURI());
			File relativeFile = homePath.relativise(file);
			System.out.println(file+", "+relativeFile.getPath());
		}
		
//		Path path = new Path(new File("/home/search/"));
//		File file = new File("search/a.txt");
//		path.relativise(path);
	}

}
