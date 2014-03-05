package org.fastcatsearch.util;

import static org.junit.Assert.*;

import org.fastcatsearch.datasource.reader.annotation.SourceReader;
import org.fastcatsearch.util.DynamicClassLoader;
import org.junit.Test;

public class DynamicClassLoaderTest {

	@Test
	public void test() {
		fail("Not yet implemented");
	}
	
	@Test
	public void dynamicClassLoaderTest() {
		
		DynamicClassLoader.findClassByAnnotation("", SourceReader.class);
	}
}
