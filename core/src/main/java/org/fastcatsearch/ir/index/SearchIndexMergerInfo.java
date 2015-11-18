package org.fastcatsearch.ir.index;

import java.io.File;

/**
 * Created by swsong on 2015. 11. 18..
 */
public class SearchIndexMergerInfo {

    private File dir;
    private int offset;

    public SearchIndexMergerInfo(File dir, int offset) {
        this.dir = dir;
        this.offset = offset;
    }

    public File getDir() {
        return dir;
    }

    public int getOffset() {
        return offset;
    }
}
