package org.fastcatsearch.http.action.management.keyword;

import java.io.Writer;
import java.util.List;

import org.fastcatsearch.db.mapper.RelateKeywordMapper;
import org.fastcatsearch.db.vo.RelateKeywordVO;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.keyword.KeywordService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value="/management/keyword/relate/list", authority=ActionAuthority.Dictionary)
public class GetRecommendKeyWordListAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		String categoryId = request.getParameter("category");
		String search = request.getParameter("search");
		int start = request.getIntParameter("start");
		int length = request.getIntParameter("length");
		
		KeywordService keywordService = ServiceManager.getInstance().getService(KeywordService.class);
		
		RelateKeywordMapper relateKeywordMapper = keywordService.getMapperSession(RelateKeywordMapper.class).getMapper();
		
		String whereCondition = "";
		
		if(search!=null && !"".equals(search)) {
			whereCondition = " and keyword = '"+search+"' ";
		}
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		
		int totalSize = 0;
		int filteredSize = 0;
		
		resultWriter.object().key("list").array();
		
		if(relateKeywordMapper!=null) {
			totalSize = relateKeywordMapper.getCount(categoryId);
			filteredSize = relateKeywordMapper.getCountByWhereCondition(categoryId, whereCondition);
			
			if(length==-1) {
				length = totalSize;
			}
			
			if(totalSize > 0) {
			
				List<RelateKeywordVO> list = relateKeywordMapper.getEntryListByWhereCondition(categoryId, whereCondition, start, start+length);
				for(RelateKeywordVO vo : list) {
					
					resultWriter.object().key("keyword").value(vo.getKeyword())
						.key("value").value(vo.getValue()).endObject();
				}
			}
		}
		
		resultWriter.endArray();
		
		resultWriter.key("totalSize").value(totalSize).key("filteredSize").value(filteredSize);
		resultWriter.endObject();
		resultWriter.done();
	}
}