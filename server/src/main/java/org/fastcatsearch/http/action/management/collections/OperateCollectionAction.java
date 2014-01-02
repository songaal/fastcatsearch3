package org.fastcatsearch.http.action.management.collections;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/management/collections/operate", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class OperateCollectionAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		String collectionId = request.getParameter("collectionId");
		String command = request.getParameter("command");

		boolean isSuccess = false;
		String errorMessage = null;

		try {

			IRService irService = ServiceManager.getInstance().getService(IRService.class);

			CollectionHandler collectionHandler = irService.collectionHandler(collectionId);

			if (collectionHandler == null) {
				errorMessage = "Collection [" + collectionId + "] is not exist.";
				return;
			}

			if ("START".equalsIgnoreCase(command)) {
				if(collectionHandler.isLoaded()){
					errorMessage = "Collection [" + collectionId + "] is already started.";
					return;
				}
				collectionHandler.load();
			} else if ("STOP".equalsIgnoreCase(command)) {
				if(!collectionHandler.isLoaded()){
					errorMessage = "Collection [" + collectionId + "] is already stoped.";
					return;
				}
				collectionHandler.close();
			} else {
				isSuccess = false;
				errorMessage = "Cannot understand command > " + command;
				return;
			}

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
