package org.fastcatsearch.http.action.management.collections;

import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionConfig;
import org.fastcatsearch.ir.config.DataPlanConfig;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/management/collections/create", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class CreateCollectionAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		String collectionId = request.getParameter("collectionId");
		String collectionName = request.getParameter("name");
		String indexNode = request.getParameter("indexNode");
		String dataNodeListString = request.getParameter("dataNodeList");

		List<String> dataNodeList = new ArrayList<String>();
		if (dataNodeListString != null) {
			for (String nodeStr : dataNodeListString.split(",")) {
				nodeStr = nodeStr.trim();
				if (nodeStr.length() > 0) {
					dataNodeList.add(nodeStr);
				}
			}
		}

		boolean isSuccess = false;
		String errorMessage = null;

		try {
			IRService irService = ServiceManager.getInstance().getService(IRService.class);

			CollectionConfig collectionConfig = new CollectionConfig(collectionName, indexNode, dataNodeList, DataPlanConfig.DefaultDataPlanConfig);

			CollectionHandler collectionHandler = irService.createCollection(collectionId, collectionConfig);
			
			isSuccess = true;
		} catch (Exception e) {
			isSuccess = false;
			errorMessage = e.getMessage();
		} finally {
			ResponseWriter responseWriter = getDefaultResponseWriter(response.getWriter());
			responseWriter.object();
			responseWriter.key("success").value(isSuccess);
			if (errorMessage != null) {
				responseWriter.key("errorMessage").value(errorMessage);
			}
			responseWriter.endObject();
			responseWriter.done();
		}

	}

}
