package org.fastcatsearch.http.action.management.collections;

import java.io.Writer;
import java.util.List;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionConfig.Shard;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping("/management/collections/shard-list")
public class GetCollectionsShardListAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		String collectionId = request.getParameter("collectionId");
		
		if(collectionId == null){
			
			return;
		}
		
		
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		CollectionContext collectionContext = irService.collectionContext(collectionId);
		
		if(collectionContext == null){
			
			//no such collection.
		}
		
		List<Shard> shardList = collectionContext.collectionConfig().getShardConfigList();
		
		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		responseWriter.object().key("shardList").array("shard");
		for(Shard shard : shardList){
			responseWriter.object()
			.key("id").value(shard.getId())
//			.key("name").value(shard.getName())
			.endObject();
		}
		responseWriter.endArray().endObject();
		responseWriter.done();
	}

}
