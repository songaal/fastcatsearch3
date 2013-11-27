package org.fastcatsearch.http.action.management.logs;

import java.io.Writer;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.mapper.ExceptionHistoryMapper;
import org.fastcatsearch.db.vo.ExceptionVO;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value="/management/logs/exception-info", authority=ActionAuthority.Logs)
public class GetExceptionInfoAction extends AuthAction {
	
	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		DBService dbService = DBService.getInstance();
		
		MapperSession<ExceptionHistoryMapper> session = null;
		
		int id = request.getIntParameter("id",0);
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		
		try {
		
			session = dbService.getMapperSession(ExceptionHistoryMapper.class);
			
			ExceptionHistoryMapper mapper = session.getMapper();
			
			ExceptionVO entry = mapper.getEntry(id);
			
			resultWriter.object()
				.key("id").value(entry.id)
				.key("node").value(entry.node)
				.key("message").value(entry.message)
				.key("regtime").value(entry.regtime)
				.endObject();
				
		
		} finally {
			
			if(session!=null) {
				session.closeSession();
			}
		}
		
		resultWriter.done();
	}
}
