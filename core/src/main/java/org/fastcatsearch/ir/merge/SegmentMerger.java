package org.fastcatsearch.ir.merge;

import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.search.SegmentReader;
import org.fastcatsearch.ir.settings.Schema;

import java.io.File;

/**
 * Created by swsong on 2015. 11. 16..
 */
public class SegmentMerger {
    public SegmentMerger(Schema schema) {

    }

    public DataInfo.SegmentInfo merge(File... segmentDirs) {
        int i = 0;
        for (File dir : segmentDirs) {

        }

        return null;
    }
}
