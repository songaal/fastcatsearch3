package org.fastcatsearch.http.action.service.indexing;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.ActionMethod;

/**
 * Rest API를 통해 문서를 업데이트한다.
 *
 * */
@ActionMapping(value = "/service/index", method = { ActionMethod.PUT })
public class UpdateDocumentsAction extends IndexDocumentsAction {

    @Override
    protected String getType() {
        return UPDATE_TYPE;
    }
}
