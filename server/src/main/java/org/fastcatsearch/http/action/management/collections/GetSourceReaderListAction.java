package org.fastcatsearch.http.action.management.collections;

import java.io.Writer;
import java.util.List;

import org.fastcatsearch.datasource.reader.SingleSourceReader;
import org.fastcatsearch.datasource.reader.annotation.SourceReader;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.util.DynamicClassLoader;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/management/collections/single-source-reader-list", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class GetSourceReaderListAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response)
			throws Exception {
		
		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		
		//FIXME:org.fastcatsearch 패키지로 일단 고정
		//패키지 안정화 이후 프로퍼티 등으로 교체 요망
		List<Class<?>> sourceReaderList = DynamicClassLoader.findChildrenClass("org.fastcatsearch", SingleSourceReader.class);
		
		responseWriter.object().key("sourceReaderList").array();
		for(Class<?> sourceReader : sourceReaderList) {
			logger.debug("class:{}", sourceReader);
			SourceReader annotation = sourceReader.getAnnotation(SourceReader.class);
			if(annotation!=null && annotation.name() != null) {
				responseWriter.object()
					.key("name")
					.value(annotation.name())
				.endObject();
			}
		}
		responseWriter.endArray().endObject();
		responseWriter.done();
	}
}
