package org.fastcatsearch.job.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.Strings;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.error.SearchError;
import org.fastcatsearch.error.ServerErrorCode;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.group.GroupsData;
import org.fastcatsearch.ir.query.Groups;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.search.GroupResultAggregator;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.internal.InternalGroupSearchJob;
import org.fastcatsearch.query.QueryMap;
import org.fastcatsearch.query.QueryParser;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableGroupsData;

public class ClusterGroupSearchJob extends Job {

	private static final long serialVersionUID = -1051204900646595240L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		long st = System.currentTimeMillis();
		QueryMap queryMap = (QueryMap) getArgs();
		GroupResults groupResults = null;
		boolean noCache = false;
		
		
		Query q = QueryParser.getInstance().parseQuery(queryMap);

		//no cache 옵션이 없으면 캐시를 확인한다.
		if(q.getMeta().isSearchOption(Query.SEARCH_OPT_NOCACHE)){
			noCache = true;
		}
		
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		if(!noCache){
			groupResults = irService.groupingCache().get(queryMap.queryString());
//			logger.debug("CACHE_GET result>>{}, qr >>{}", groupResults, queryMap.queryString());
			if(groupResults != null){
				return new JobResult(groupResults);
			}
		}
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		
		String collectionId = q.getMeta().collectionId();
		Groups groups = q.getGroups();
		
		String[] collectionIdList = collectionId.split(",");
		if(collectionIdList.length > 1) {
			shuffleCollectionList(collectionIdList);
		}
		
		ResultFuture[] resultFutureList = new ResultFuture[collectionIdList.length];
		
		for (int i = 0; i < collectionIdList.length; i++) {
			String id = collectionIdList[i];
			
			Node dataNode = nodeService.getBalancedNode(id);
			logger.debug("shard [{}] search at {}", id, dataNode);
			QueryMap newQueryMap = queryMap.clone();
			newQueryMap.setId(id);
			
			//보내는 곳마다 collectionId를 재 셋팅한다. (collection group명일수 있기때문에) 
			InternalGroupSearchJob job = new InternalGroupSearchJob(newQueryMap);
			resultFutureList[i] = nodeService.sendRequest(dataNode, job);
            // 노드 접속불가일경우 resultFutureList[i]가 null로 리턴됨.
            if (resultFutureList[i] == null) {
                throw new SearchError(ServerErrorCode.DATA_NODE_CONNECTION_ERROR, dataNode.toString() );
            }
		}
		
		List<GroupsData> resultList = new ArrayList<GroupsData>(collectionIdList.length);
		
		for (int i = 0; i < collectionIdList.length; i++) {
			Object obj = resultFutureList[i].take();
			if(!resultFutureList[i].isSuccess()){
                if (obj instanceof SearchError) {
                    throw (SearchError) obj;
                } else if (obj instanceof Throwable) {
                    throw new FastcatSearchException((Throwable) obj);
                } else {
                    throw new FastcatSearchException("Error while searching.", obj);
                }
			}
			
			StreamableGroupsData obj2 = (StreamableGroupsData) obj;
			resultList.add(obj2.groupData());
			
		}
		
		
		GroupResultAggregator aggregator = new GroupResultAggregator(groups);
		groupResults = aggregator.aggregate(resultList);
		
		if(groupResults != null && !noCache){
			irService.groupingCache().put(queryMap.queryString(), groupResults);
		}
		
		logger.debug("ClusterGroupSearchJob 수행시간 : {}", Strings.getHumanReadableTimeInterval(System.currentTimeMillis() - st));
		return new JobResult(groupResults);
	}
	
	// Fisher-Yates shuffle
	Random random = new Random(System.nanoTime());
	private void shuffleCollectionList(String[] collectionId) {
		for (int i = collectionId.length - 1; i > 0; i--) {
			int index = random.nextInt(i + 1);
			// Simple swap
			String t = collectionId[index];
			collectionId[index] = collectionId[i];
			collectionId[i] = t;
		}
	}
}
