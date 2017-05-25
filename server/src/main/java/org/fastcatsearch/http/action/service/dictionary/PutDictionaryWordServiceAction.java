package org.fastcatsearch.http.action.service.dictionary;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.ActionMethod;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.http.action.management.dictionary.PutDictionaryWordAction;

/**
 * Created by 전제현 on 2017. 5. 25.
 */
@ActionMapping(value="/service/dictionary/put", method = { ActionMethod.POST })
public class PutDictionaryWordServiceAction extends ServiceAction {

    @Override
    public void doAction(ActionRequest request, ActionResponse response) throws Exception {

        PutDictionaryWordAction action = new PutDictionaryWordAction();
        action.init(this.resultType, request, response, null);
        writeHeader(response);
        action.doAuthAction(request, response);
    }
}


