package org.fastcatsearch.ir.merge;

import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.index.SearchIndexMerger;
import org.fastcatsearch.ir.search.SegmentReader;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.document.DocumentMerger;

import java.io.File;
import java.io.IOException;

/**
 * Created by swsong on 2015. 11. 16..
 */
public class SegmentMerger {
    private File newSegmentDir;

    public SegmentMerger(File dir) {
        this.newSegmentDir = dir;

    }

    public DataInfo.SegmentInfo merge(File... dirs) throws IOException {
        DocumentMerger documentMerger = null;

        try {
            documentMerger = new DocumentMerger(newSegmentDir);
            documentMerger.merge(dirs);
        } finally {
            if(documentMerger != null) {
                documentMerger.close();
            }
        }

        //TODO search index 필드만큼 loop를 돈다
        String indexId = "productname";
        int indexInterval = 128;
        SearchIndexMerger searchIndexMerger = null;
        try {
            searchIndexMerger = new SearchIndexMerger(indexId, newSegmentDir, indexInterval);
            searchIndexMerger.merge(dirs);
        }finally {
            if(searchIndexMerger != null) {
                searchIndexMerger.close();
            }
        }




        return null;
    }

    public void close() {

    }
}
