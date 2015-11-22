package org.fastcatsearch.ir.merge;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.ir.document.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by swsong on 2015. 11. 22..
 */
public class SegmentMergerTest {

    @Test
    public void test1() throws IOException {
        File segment1Dir = new File("/Users/swsong/TEST_HOME/fastcatsearch-dynamic/collections/prod/data/index0/a0");
        File segment2Dir = new File("/Users/swsong/TEST_HOME/fastcatsearch-dynamic/collections/prod/data/index0/a1");
        File newSegmentDir = new File("/tmp/doc_merge");
        if(newSegmentDir.exists()) {
            FileUtils.deleteDirectory(newSegmentDir);
        }
        newSegmentDir.mkdirs();
        SegmentMerger merger = null;

        try {
            merger = new SegmentMerger(newSegmentDir);
            merger.merge(segment1Dir, segment2Dir);
        } finally {
            if(merger != null) {
                merger.close();
            }
        }
    }
}
