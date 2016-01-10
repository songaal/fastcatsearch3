package org.fastcatsearch.ir.index;

import org.fastcatsearch.ir.search.CollectionHandler;

import java.util.Map;

/**
 * Created by swsong on 2016. 1. 10..
 */
public class DynamicIndexer {
    private CollectionHandler collectionHandler;

    public DynamicIndexer(CollectionHandler collectionHandler) {
        this.collectionHandler = collectionHandler;
    }

    public void addDocument(Map<String, Object> documentMap) {

    }

    public void updateDocument(Map<String, Object> documentMap) {

    }

    public void deleteDocument(Map<String, Object> documentMap) {

    }

    public void finish() {
    }
}
