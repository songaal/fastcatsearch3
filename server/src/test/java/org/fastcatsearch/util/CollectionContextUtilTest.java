package org.fastcatsearch.util;

import java.io.File;

import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionsConfig.Collection;
import org.junit.Test;

public class CollectionContextUtilTest {

	@Test
	public void test() throws SettingException {
		FilePaths paths = new FilePaths(new File("/Users/swsong/TEST_HOME/fastcatsearch2_shard/node1/collections/"), "sample");
		Collection collection = new Collection("sample");
		CollectionContext collectionContext = CollectionContextUtil.load(collection, paths);
		System.out.println(collectionContext.schema().getFieldSetting("id"));
	}

}
