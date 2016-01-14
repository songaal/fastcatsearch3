package org.fastcatsearch.http.action.service.indexing;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.ActionMethod;

/**
 * Rest API를 통해 문서를 삭제한다.
 *
 * */
@ActionMapping(value = "/service/index", method = { ActionMethod.DELETE })
public class DeleteDocumentsAction extends IndexDocumentsAction {

    @Override
    protected String getType() {
        return DELETE_TYPE;
    }
}
