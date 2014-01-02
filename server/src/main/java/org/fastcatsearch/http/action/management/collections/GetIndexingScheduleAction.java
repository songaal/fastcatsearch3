package org.fastcatsearch.http.action.management.collections;

import java.io.OutputStream;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.IndexingScheduleConfig;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.JAXBConfigs;

@ActionMapping(value = "/management/collections/indexing-schedule", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.READABLE)
public class GetIndexingScheduleAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		String collectionId = request.getParameter("collectionId");
		
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		
		CollectionContext collectionContext = irService.collectionContext(collectionId);
		IndexingScheduleConfig indexingScheduleConfig = collectionContext.indexingScheduleConfig();
		
		OutputStream os = response.getOutputStream();
		JAXBConfigs.writeRawConfig(os, indexingScheduleConfig, IndexingScheduleConfig.class);
		
	}

}
