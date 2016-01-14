package org.fastcatsearch.http.action.service.indexing;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.ActionMethod;

/**
 * Rest API를 통해 문서를 추가한다.
 *
 * */

@ActionMapping(value = "/service/index", method = { ActionMethod.POST })
public class InsertDocumentsAction extends IndexDocumentsAction {

    @Override
    protected String getType() {
        return INSERT_TYPE;
    }

}