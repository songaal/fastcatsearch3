package org.fastcatsearch.datasource.reader;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.lang.reflect.Constructor;

import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.ir.config.SingleSourceConfig;
import org.fastcatsearch.util.DynamicClassLoader;
import org.junit.Test;

public class DataSourceReaderTest {

	@Test
	public void testLoadSingleSourceReaderClass() throws SecurityException, NoSuchMethodException {
		String sourceReaderType = "org.fastcatsearch.datasource.reader.FastcatSearchCollectFileParser";
		
		Class<?> clazz = DynamicClassLoader.loadClass(sourceReaderType);
		
		Constructor<?> constructor = clazz.getConstructor(File.class, SingleSourceConfig.class, SourceModifier.class, String.class, boolean.class);
		assertNotNull(constructor);
		
	}

}
