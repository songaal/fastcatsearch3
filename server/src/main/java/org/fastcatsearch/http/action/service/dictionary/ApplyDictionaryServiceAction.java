package org.fastcatsearch.http.action.service.dictionary;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.ActionMethod;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.http.action.management.dictionary.ApplyDictionaryAction;

/**
 * Created by 전제현 on 2017. 5. 25.
 */
@ActionMapping(value="/service/dictionary/apply", method = { ActionMethod.POST })
public class ApplyDictionaryServiceAction extends ServiceAction {

    @Override
    public void doAction(ActionRequest request, ActionResponse response) throws Exception {

        ApplyDictionaryAction dictionaryAction = new ApplyDictionaryAction();
        dictionaryAction.init(this.resultType, request, response, null);
        writeHeader(response);
        dictionaryAction.doAuthAction(request, response);
    }
}
