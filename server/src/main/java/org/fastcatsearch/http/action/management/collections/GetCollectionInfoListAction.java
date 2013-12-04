package org.fastcatsearch.http.action.management.collections;

import java.io.Writer;
import java.util.List;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionConfig;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionsConfig.Collection;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping("/management/collections/collection-info-list")
public class GetCollectionInfoListAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		
		List<Collection> collectionList = irService.getCollectionList();
		
		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		responseWriter.object().key("collectionInfoList").array("collectionInfo");
		for(Collection collection : collectionList){
			String collectionId = collection.getId();
			CollectionContext collectionContext = irService.collectionContext(collectionId);
			if(collectionContext == null){
				continue;
			}
			CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
			boolean isActive = collectionHandler != null && collectionHandler.isLoaded();
			CollectionConfig collectionConfig = collectionContext.collectionConfig();
			
			responseWriter.object()
			.key("id").value(collectionId)
			.key("isActive").value(isActive)
			.key("name").value(collectionConfig.getName())
			.key("indexNode").value(collectionConfig.getIndexNode())
			.key("dataNodeList").value(join(collectionConfig.getDataNodeList()))
			.endObject();
		}
		responseWriter.endArray().endObject();
		responseWriter.done();
	}

	public String join(List<String> list) {
		String joinString = "";
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				if(i > 0){
					joinString += ", ";
				}
				joinString += list.get(i);
			}
		}

		return joinString;
	}
}
