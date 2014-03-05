package org.fastcatsearch.util;

import static org.junit.Assert.fail;

import java.util.List;

import org.fastcatsearch.datasource.reader.SingleSourceReader;
import org.fastcatsearch.datasource.reader.annotation.SourceReader;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.MasterNodeJob;
import org.junit.Test;

public class DynamicClassLoaderTest {

	@Test
	public void test() {
		fail("Not yet implemented");
	}
	
	@Test
	public void dynamicClassLoaderTest() {
		
		List<Class<?>> classList = DynamicClassLoader.findClassByAnnotation("org.fastcatsearch.datasource", SourceReader.class);
		for(Class<?> clazz : classList){
			System.out.println(clazz);
		}
	}
	
	@Test
	public void testChildrenClass() {
		
		List<Class<?>> classList = DynamicClassLoader.findChildrenClass("org.fastcatsearch", SingleSourceReader.class);
		for(Class<?> clazz : classList){
			System.out.println("["+clazz+"]");
		}
	}
}
