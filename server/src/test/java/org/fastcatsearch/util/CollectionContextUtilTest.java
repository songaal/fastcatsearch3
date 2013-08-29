package org.fastcatsearch.util;

import java.io.File;

import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionsConfig.Collection;
import org.junit.Test;

public class CollectionContextUtilTest {

	@Test
	public void test() throws SettingException {
		IndexFilePaths paths = new IndexFilePaths(new File("/Users/swsong/TEST_HOME/fastcatsearch2.13.7/collections/"), "sample");
		Collection collection = new Collection("sample", true);
		CollectionContext collectionContext = CollectionContextUtil.load(collection, paths);
		System.out.println(collectionContext.schema().getFieldSetting("id"));
	}

}
