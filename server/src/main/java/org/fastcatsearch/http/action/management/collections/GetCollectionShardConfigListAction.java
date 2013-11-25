package org.fastcatsearch.http.action.management.collections;

import java.io.OutputStream;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.ShardConfig;
import org.fastcatsearch.ir.config.ShardContext;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.JAXBConfigs;

@ActionMapping("/management/collections/shard-config")
public class GetCollectionShardConfigListAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		String collectionId = request.getParameter("collectionId");
		String shardId = request.getParameter("shardId");
		
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		
		CollectionContext collectionContext = irService.collectionContext(collectionId);
		OutputStream os = response.getOutputStream();
		String startTag = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<shard-config-list>";
		String endTag = "</shard-config-list>";
		os.write(startTag.getBytes());
		for(ShardContext shardContext : collectionContext.getShardContextList()){
			//shardId가 없거나 존재한다면 일치하는 것만 리턴. 
			if(shardId == null || shardId.length() == 0 || shardId.equalsIgnoreCase(shardContext.shardId())){
				JAXBConfigs.writeRawConfig(os, shardContext.shardConfig(), ShardConfig.class, true);
			}
		}
		os.write(endTag.getBytes());
		
		
	}

}
