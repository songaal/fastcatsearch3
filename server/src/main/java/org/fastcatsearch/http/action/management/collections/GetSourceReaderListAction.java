package org.fastcatsearch.http.action.management.collections;

import java.util.List;

import org.fastcatsearch.datasource.reader.annotation.SourceReader;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.util.DynamicClassLoader;

@ActionMapping(value = "/management/collections/single-source-reader-list", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class GetSourceReaderListAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response)
			throws Exception {
		
		
		List<Class<?>> sourceReaderList = DynamicClassLoader.findClassByAnnotation(null, SourceReader.class);
		
		logger.debug("================================================================================");
		for(Class<?> sourceReader : sourceReaderList) {
			logger.debug("class:{}", sourceReader);
		}
		logger.debug("================================================================================");
	}
}
