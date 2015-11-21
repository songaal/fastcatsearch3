package org.fastcatsearch.ir.index;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by swsong on 2015. 11. 17..
 */
public class SearchIndexMergerTest {

    @Test
    public void testMerge() throws IOException {

        String indexId = "PRODUCTNAME";
        File dir = new File("/tmp/doc_merge");
        int indexInterval = 128;
//        if(dir.exists()) {
//            FileUtils.deleteDirectory(dir);
//        }
        dir.mkdirs();

        File seg1 = new File("/Users/swsong/TEST_HOME/fastcatsearch-dynamic/collections/prod/data/index0/a0");
        File seg2 = new File("/Users/swsong/TEST_HOME/fastcatsearch-dynamic/collections/prod/data/index0/a1");
        SearchIndexMerger merger = null;
        try {
            merger = new SearchIndexMerger(indexId, dir, indexInterval);
            merger.merge(seg1, seg2);
        }finally {
            if(merger != null) {
                merger.close();
            }
        }
    }
}
