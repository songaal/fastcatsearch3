package org.fastcatsearch.http.action.management.logs;

import java.io.Writer;
import java.util.List;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.mapper.TaskHistoryMapper;
import org.fastcatsearch.db.vo.TaskHistoryVO;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value="/management/logs/task-history", authority=ActionAuthority.Logs)
public class GetTaskHistoryAction extends AuthAction {
	
	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		DBService dbService = DBService.getInstance();
		
		MapperSession<TaskHistoryMapper> session = null;
		
		int pageNum = request.getIntParameter("pageNum",1);
		
		if(pageNum < 1) { pageNum = 1; }
		
		int start = request.getIntParameter("start",0);
		
		int end = request.getIntParameter("end",start);
		
		int totalCount = 0;
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		
		try {
		
			session = dbService.getMapperSession(TaskHistoryMapper.class);
			
			TaskHistoryMapper mapper = session.getMapper();
			
			List<TaskHistoryVO> entryList = null;
			
			totalCount = mapper.getCount();
			
			entryList = mapper.getEntryList(start, end);
			
			resultWriter.object().key("totalCount").value(totalCount)
				.key("pageNum").value(pageNum)
				.key("start").value(start)
				.key("end").value(end)
				.key("notifications").array();
			
			for(int inx=0; inx < entryList.size(); inx++) {
				
				TaskHistoryVO entry = entryList.get(inx);
				
				resultWriter.object()
					.key("id").value(entry.id)
					.key("taskId").value(entry.taskId)
					.key("executable").value(entry.executable)
					.key("args").value(entry.args)
					.key("status").value(entry.status.name())
					.key("result").value(entry.resultStr)
					.key("isScheduled").value(entry.isScheduled)
					.key("startTime").value(entry.startTime)
					.key("endTime").value(entry.endTime)
					.key("duration").value(entry.duration)
					.endObject();
			}
			resultWriter.endArray().endObject();
		
		} finally {
			
			if(session!=null) {
				session.closeSession();
			}
		}
		
		resultWriter.done();
	}
}
