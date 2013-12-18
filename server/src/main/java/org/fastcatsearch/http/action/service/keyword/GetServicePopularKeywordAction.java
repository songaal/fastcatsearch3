package org.fastcatsearch.http.action.service.keyword;

import java.util.List;

import org.fastcatsearch.db.vo.PopularKeywordVO;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.keyword.KeywordDictionary;
import org.fastcatsearch.keyword.KeywordService;
import org.fastcatsearch.keyword.PopularKeywordDictionary;
import org.fastcatsearch.keyword.KeywordDictionary.KeywordDictionaryType;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping("/service/keyword/popular")
public class GetServicePopularKeywordAction extends ServiceAction {

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {
		KeywordService keywordService = ServiceManager.getInstance().getService(KeywordService.class);

		writeHeader(response);
		ResponseWriter responseWriter = getDefaultResponseWriter(response.getWriter());
		responseWriter.object();

		String type = request.getParameter("type");
		String categoryId = request.getParameter("category");
		String errorMessage = null;

		try {
			KeywordDictionaryType keywordDictionaryType = KeywordDictionaryType.POPULAR_KEYWORD_REALTIME;
			
			if ("D".equalsIgnoreCase(type)) {
				keywordDictionaryType = KeywordDictionaryType.POPULAR_KEYWORD_DAY;
			} else if ("W".equalsIgnoreCase(type)) {
				keywordDictionaryType = KeywordDictionaryType.POPULAR_KEYWORD_WEEK;
			}

			responseWriter.key("category").value(categoryId);
			responseWriter.key("type").value(keywordDictionaryType.name());
			
			
			KeywordDictionary keywordDictionary = keywordService.getKeywordDictionary(categoryId, keywordDictionaryType);

			PopularKeywordDictionary popularKeywordDictionary = (PopularKeywordDictionary) keywordDictionary;
			if (popularKeywordDictionary != null) {

				List<PopularKeywordVO> keywordList = popularKeywordDictionary.getKeywordList();
				if (popularKeywordDictionary.getCreateTime() != null) {
					responseWriter.key("time").value(Formatter.formatDate(popularKeywordDictionary.getCreateTime()));
				}
				responseWriter.key("list").array();
				for (PopularKeywordVO vo : keywordList) {
					responseWriter.object();
					responseWriter.key("word").value(vo.getWord());
					responseWriter.key("rank").value(vo.getRank());
					responseWriter.key("diffType").value(vo.getRankDiffType().name());
					responseWriter.key("diff").value(vo.getRankDiff());
					responseWriter.endObject();
				}
				responseWriter.endArray();

			} else {
				throw new Exception("Request popular keyword is not in service");
			}
		} catch (Exception e) {
			errorMessage = e.getMessage();
		} finally {
			if (errorMessage != null) {
				responseWriter.key("errorMessage").value(errorMessage);
			}
			responseWriter.endObject();
			responseWriter.done();
		}
	}

}
