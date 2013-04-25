package org.fastcatsearch.job.cluster;

import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.Strings;
import org.fastcatsearch.control.JobException;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.data.DataService;
import org.fastcatsearch.data.DataStrategy;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.ir.config.Schema;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.io.FixedHitReader;
import org.fastcatsearch.ir.query.Groups;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.QueryParseException;
import org.fastcatsearch.ir.query.QueryParser;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.query.ShardSearchResult;
import org.fastcatsearch.ir.search.SearchResultAggregator;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.internal.InternalDocumentRequestJob;
import org.fastcatsearch.job.internal.InternalSearchJob;
import org.fastcatsearch.service.IRService;
import org.fastcatsearch.service.ServiceException;
import org.fastcatsearch.transport.vo.StreamableShardSearchResult;

public class ClusterSearchJob extends Job {

	@Override
	public JobResult doRun() throws JobException, ServiceException {
		
		long st = System.currentTimeMillis();
		String[] args = getStringArrayArgs();
		String queryString = args[0];
		Result searchResult = null;
		ShardSearchResult internalSearchResult = null;
		boolean noCache = false;
		
		
		Query q = null;
		try {
			q = QueryParser.getInstance().parseQuery(queryString);
		} catch (QueryParseException e) {
			throw new JobException("[Query Parsing Error] "+e.getMessage());
		}
		
		//no cache 옵션이 없으면 캐시를 확인한다.
		if((q.getMeta().option() & Query.SEARCH_OPT_NOCACHE) > 0){
			noCache = true;
		}
		
		if(!noCache){
			Result result = IRService.getInstance().searchCache().get(queryString);
			logger.debug("CACHE_GET result>>{}, qr >>{}", result, queryString);
			if(result != null){
				logger.debug("Cached Result!");
				return new JobResult(result);
			}
		}
		
				
		String collectionId = q.getMeta().collectionName();
		Groups groups = q.getGroups();
		
		String[] collectionIdList = collectionId.split(",");
		ResultFuture[] resultFutureList = new ResultFuture[collectionIdList.length];
		
		for (int i = 0; i < collectionIdList.length; i++) {
			String cId = collectionIdList[i];
			DataStrategy dataStrategy = DataService.getInstance().getCollectionDataStrategy(cId);
			List<Node> nodeList = dataStrategy.dataNodes();
			//TODO shard 갯수를 확인하고 각 shard에 해당하는 노드들을 가져온다.
			//TODO 여러개의 replaica로 분산되어있을 경우, 적합한 노드를 찾아서 리턴한다.
			
			Node dataNode = nodeList.get(0);
			logger.debug("collection [{}] search at {}", cId, dataNode);
			String queryStr = queryString.replace("cn="+collectionId, "cn="+cId);
			logger.debug("query-{} >> {}", i, queryStr);
			InternalSearchJob job = new InternalSearchJob(queryStr);
			resultFutureList[i] = NodeService.getInstance().sendRequest(dataNode, job);
		}
		
		List<ShardSearchResult> resultList = new ArrayList<ShardSearchResult>(collectionIdList.length);
		
		for (int i = 0; i < collectionIdList.length; i++) {
			//TODO 노드 접속불가일경우 resultFutureList[i]가 null로 리턴됨.
			if(resultFutureList[i] == null){
				throw new JobException("요청메시지 전송불가에러.");
			}
			Object obj = resultFutureList[i].take();
			if(!resultFutureList[i].isSuccess()){
				if(obj instanceof Throwable){
					throw new JobException("검색수행중 에러발생.", (Throwable) obj);
				}else{
					throw new JobException("검색수행중 에러발생.");
				}
			}
			
			StreamableShardSearchResult obj2 = (StreamableShardSearchResult) obj;
			resultList.add(obj2.shardSearchResult());
			
		}
		
		//
		//collectionIdList 내의 스키마는 동일하다는 가정하에 진행한다.
		//
		
		Schema schema = null;
		try {
			schema = IRSettings.getSchema(collectionIdList[0], false);
		} catch (SettingException e) {
			logger.error("", e);
		}
		SearchResultAggregator aggregator = new SearchResultAggregator(q, schema);
		internalSearchResult = aggregator.aggregate(resultList);
		
		
		///
		/// 컬렉션별 도큐먼트를 가져와서 완전한 결과객체를 만든다.
		//
		
		//internalSearchResult의 결과를 보면서 컬렉션 별로 분류한다.
		FixedHitReader hitReader = internalSearchResult.getFixedHitReader();
		int[][] docIdList = new int[collectionIdList.length][];
		int[] length = new int[collectionIdList.length];
		
		resultFutureList = new ResultFuture[collectionIdList.length];
		
		for (int i = 0; i < collectionIdList.length; i++) {
			String cId = collectionIdList[i];
			DataStrategy dataStrategy = DataService.getInstance().getCollectionDataStrategy(cId);
			List<Node> nodeList = dataStrategy.dataNodes();
			//TODO shard 갯수를 확인하고 각 shard에 해당하는 노드들을 가져온다.
			//TODO 여러개의 replaica로 분산되어있을 경우, 적합한 노드를 찾아서 리턴한다.
			
			Node dataNode = nodeList.get(0);
			logger.debug("collection [{}] search at {}", cId, dataNode);
			String queryStr = queryString.replace("cn="+collectionId, "cn="+cId);
			logger.debug("query-{} >> {}", i, queryStr);
			InternalDocumentRequestJob job = new InternalDocumentRequestJob(cId, docIdList[i], length[i]);
			resultFutureList[i] = NodeService.getInstance().sendRequest(dataNode, job);
		}
		
		
		if(searchResult != null){
			IRService.getInstance().searchCache().put(queryString, searchResult);
			logger.debug("CACHE_PUT result>>{}, qr >>{}", searchResult, queryString);
		}
		
		logger.debug("ClusterGroupSearchJob 수행시간 : {}", Strings.getHumanReadableTimeInterval(System.currentTimeMillis() - st));
		return new JobResult(searchResult);
	}

}
