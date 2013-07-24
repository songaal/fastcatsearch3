package org.fastcatsearch.util;

import java.io.File;

import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.CollectionContext;
import org.junit.Test;

public class CollectionContextUtilTest {

	@Test
	public void test() throws SettingException {
		CollectionFilePaths paths = new CollectionFilePaths(new File("/Users/swsong/TEST_HOME/fastcatsearch2.13.7/collections/"), "sample");
		CollectionContext collectionContext = CollectionContextUtil.load(paths, 0);
		System.out.println(collectionContext.schema().getFieldSetting("id"));
	}

}
