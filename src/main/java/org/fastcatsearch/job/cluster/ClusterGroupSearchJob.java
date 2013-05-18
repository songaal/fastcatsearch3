package org.fastcatsearch.job.cluster;

import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.Strings;

import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.data.DataService;
import org.fastcatsearch.data.DataStrategy;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.group.GroupData;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.query.Groups;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.search.GroupResultAggregator;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.internal.InternalGroupSearchJob;
import org.fastcatsearch.query.QueryParseException;
import org.fastcatsearch.query.QueryParser;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableGroupData;

public class ClusterGroupSearchJob extends Job {

	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		long st = System.currentTimeMillis();
		String[] args = getStringArrayArgs();
		String queryString = args[0];
		GroupResults groupResults = null;
		boolean noCache = false;
		
		
		Query q = null;
		try {
			q = QueryParser.getInstance().parseQuery(queryString);
		} catch (QueryParseException e) {
			throw new FastcatSearchException("[Query Parsing Error] "+e.getMessage());
		}
		
		//no cache 옵션이 없으면 캐시를 확인한다.
		if((q.getMeta().option() & Query.SEARCH_OPT_NOCACHE) > 0){
			noCache = true;
		}
		
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		if(!noCache){
			groupResults = irService.groupingCache().get(queryString);
			logger.debug("CACHE_GET result>>{}, qr >>{}", groupResults, queryString);
			if(groupResults != null){
				logger.debug("Cached Result!");
				return new JobResult(groupResults);
			}
		}
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		DataService dataService = ServiceManager.getInstance().getService(DataService.class);
		
		String collectionId = q.getMeta().collectionName();
		Groups groups = q.getGroups();
		
		String[] collectionIdList = collectionId.split(",");
		ResultFuture[] resultFutureList = new ResultFuture[collectionIdList.length];
		
		for (int i = 0; i < collectionIdList.length; i++) {
			String cId = collectionIdList[i];
			
			DataStrategy dataStrategy = dataService.getCollectionDataStrategy(cId);
			List<Node> nodeList = dataStrategy.dataNodes();
			//TODO shard 갯수를 확인하고 각 shard에 해당하는 노드들을 가져온다.
			//TODO 여러개의 replaica로 분산되어있을 경우, 적합한 노드를 찾아서 리턴한다.
			
			Node dataNode = nodeList.get(0);
			logger.debug("collection [{}] search at {}", cId, dataNode);
			String queryStr = queryString.replace("cn="+collectionId, "cn="+cId);
			logger.debug("query-{} >> {}", i, queryStr);
			InternalGroupSearchJob job = new InternalGroupSearchJob(queryStr);
			resultFutureList[i] = nodeService.sendRequest(dataNode, job);
		}
		
		List<GroupData> resultList = new ArrayList<GroupData>(collectionIdList.length);
		
		for (int i = 0; i < collectionIdList.length; i++) {
			//TODO 노드 접속불가일경우 resultFutureList[i]가 null로 리턴됨.
			if(resultFutureList[i] == null){
				throw new FastcatSearchException("요청메시지 전송불가에러.");
			}
			Object obj = resultFutureList[i].take();
			if(!resultFutureList[i].isSuccess()){
				if(obj instanceof Throwable){
					throw new FastcatSearchException("검색수행중 에러발생.", (Throwable) obj);
				}else{
					throw new FastcatSearchException("검색수행중 에러발생.");
				}
			}
			
			StreamableGroupData obj2 = (StreamableGroupData) obj;
			resultList.add(obj2.groupData());
			
		}
		
		
		GroupResultAggregator aggregator = new GroupResultAggregator(groups);
		groupResults = aggregator.aggregate(resultList);
		
		if(groupResults != null){
			irService.groupingCache().put(queryString, groupResults);
			logger.debug("CACHE_PUT result>>{}, qr >>{}", groupResults, queryString);
		}
		
		logger.debug("ClusterGroupSearchJob 수행시간 : {}", Strings.getHumanReadableTimeInterval(System.currentTimeMillis() - st));
		return new JobResult(groupResults);
	}

}
