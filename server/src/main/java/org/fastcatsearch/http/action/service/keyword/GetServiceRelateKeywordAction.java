package org.fastcatsearch.http.action.service.keyword;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.keyword.KeywordDictionary;
import org.fastcatsearch.keyword.KeywordService;
import org.fastcatsearch.keyword.RelateKeywordDictionary;
import org.fastcatsearch.keyword.KeywordDictionary.KeywordDictionaryType;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping("/service/keyword/relate")
public class GetServiceRelateKeywordAction extends ServiceAction {

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {
		KeywordService keywordService = ServiceManager.getInstance().getService(KeywordService.class);
		
		writeHeader(response);
		ResponseWriter responseWriter = getDefaultResponseWriter(response.getWriter());
		
		String keyword = request.getParameter("keyword");
		String category = request.getParameter("category");
		KeywordDictionaryType keywordDictionaryType = KeywordDictionaryType.RELATE_KEYWORD;
		
		KeywordDictionary keywordDictionary = keywordService.getKeywordDictionary(category, keywordDictionaryType);
		
		RelateKeywordDictionary relateKeywordDictionary = (RelateKeywordDictionary) keywordDictionary;
		String relateValue = relateKeywordDictionary.getRelateKeyword(keyword);
		
		responseWriter.object();
		responseWriter.key("keyword").value(keyword);
		responseWriter.key("relate").value(relateValue);
		responseWriter.endObject();
		
		responseWriter.done();
	}

}
