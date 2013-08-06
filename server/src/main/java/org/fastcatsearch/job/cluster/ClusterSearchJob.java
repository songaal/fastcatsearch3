package org.fastcatsearch.job.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.Strings;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.data.DataService;
import org.fastcatsearch.data.DataStrategy;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.field.DocNoField;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.field.ScoreField;
import org.fastcatsearch.ir.field.UnknownField;
import org.fastcatsearch.ir.group.GroupsData;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.io.FixedHitReader;
import org.fastcatsearch.ir.query.Groups;
import org.fastcatsearch.ir.query.HighlightInfo;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.query.Row;
import org.fastcatsearch.ir.query.InternalSearchResult;
import org.fastcatsearch.ir.query.View;
import org.fastcatsearch.ir.search.DocIdList;
import org.fastcatsearch.ir.search.DocumentResult;
import org.fastcatsearch.ir.search.HitElement;
import org.fastcatsearch.ir.search.SearchResultAggregator;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.internal.InternalDocumentRequestJob;
import org.fastcatsearch.job.internal.InternalDocumentSearchJob;
import org.fastcatsearch.job.internal.InternalSearchJob;
import org.fastcatsearch.query.QueryParseException;
import org.fastcatsearch.query.QueryParser;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableDocumentList;
import org.fastcatsearch.transport.vo.StreamableDocumentResult;
import org.fastcatsearch.transport.vo.StreamableShardSearchResult;

public class ClusterSearchJob extends Job {

	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		long st = System.currentTimeMillis();
		String[] args = getStringArrayArgs();
		String queryString = args[0];
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
			Result result = irService.searchCache().get(queryString);
			logger.debug("CACHE_GET result>>{}, qr >>{}", result, queryString);
			if(result != null){
				logger.debug("Cached Result!");
				return new JobResult(result);
			}
		}
		
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		DataService dataService = ServiceManager.getInstance().getService(DataService.class);
		
		Metadata meta = q.getMeta();
		String collectionId = q.getMeta().collectionId();
		Groups groups = q.getGroups();
		
		String[] collectionIdList = collectionId.split(",");
		ResultFuture[] resultFutureList = new ResultFuture[collectionIdList.length];
		Map<String, Integer> collectionNumberMap = new HashMap<String, Integer>();
		
		for (int i = 0; i < collectionIdList.length; i++) {
			String cId = collectionIdList[i];
			collectionNumberMap.put(cId, i);
			
			DataStrategy dataStrategy = dataService.getCollectionDataStrategy(cId);
			List<Node> nodeList = dataStrategy.dataNodes();
			//TODO shard 갯수를 확인하고 각 shard에 해당하는 노드들을 가져온다.
			//TODO 여러개의 replaica로 분산되어있을 경우, 적합한 노드를 찾아서 리턴한다.
			
			Node dataNode = nodeList.get(0);
			logger.debug("collection [{}] search at {}", cId, dataNode);
			String queryStr = queryString.replace("cn="+collectionId, "cn="+cId);
			logger.debug("query-{} >> {}", i, queryStr);
			InternalSearchJob job = new InternalSearchJob(queryStr);
			resultFutureList[i] = nodeService.sendRequest(dataNode, job);
		}
		
		List<InternalSearchResult> resultList = new ArrayList<InternalSearchResult>(collectionIdList.length);
		HighlightInfo highlightInfo = null;
		
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
			
			StreamableShardSearchResult obj2 = (StreamableShardSearchResult) obj;
			InternalSearchResult internalSearchResult = obj2.getInternalSearchResult();
			resultList.add(internalSearchResult);
			
			//TODO highlightInfo 들을 머지해야하나?
			highlightInfo = internalSearchResult.getHighlightInfo();
			
		}
		
		//
		//collectionIdList 내의 스키마는 동일하다는 가정하에 진행한다. collectionIdList[0] 의 스키마를 가져온다.
		//
		
		Schema schema = irService.collectionContext(collectionIdList[0]).schema();
		SearchResultAggregator aggregator = new SearchResultAggregator(q, schema);
		InternalSearchResult aggregatedSearchResult = aggregator.aggregate(resultList);
		int totalSize = aggregatedSearchResult.getTotalCount();
		
		
		///
		/// 컬렉션별 도큐먼트를 가져와서 완전한 결과객체를 만든다.
		//
		
		//internalSearchResult의 결과를 보면서 컬렉션 별로 분류한다.
		int realSize = aggregatedSearchResult.getCount();
		DocIdList[] docIdList = new DocIdList[collectionIdList.length];
//		int[] length = new int[collectionIdList.length];
		int[] collectionTags = new int[realSize]; //해당 문서가 어느컬렉션에 속하는지 알려주는 항목.
		int[] eachDocIds = new int[realSize];
		float[] eachScores = new float[realSize];
		
		
		for (int i = 0; i < collectionIdList.length; i++) {
			docIdList[i] = new DocIdList(realSize);
		}
		
		int idx = 0;
		FixedHitReader hitReader = aggregatedSearchResult.getFixedHitReader();
		while(hitReader.next()){
			HitElement el = hitReader.read();
			int collectionNo = collectionNumberMap.get(el.collection());
			docIdList[collectionNo].add(el.segmentSequence(), el.docNo());
			collectionTags[idx] = collectionNo;
			eachDocIds[idx] = el.docNo();
			eachScores[idx] = el.score();
			idx++;
		}
		
		//document 요청을 보낸다.
		resultFutureList = new ResultFuture[collectionIdList.length];
		List<View> views = q.getViews(); 
		String[] tags = q.getMeta().tags();
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
//			InternalDocumentSearchJob job = new InternalDocumentSearchJob(cId, docIdList[i], length[i]);
			InternalDocumentSearchJob job = new InternalDocumentSearchJob(collectionId, docIdList[i], views, tags, highlightInfo);
			resultFutureList[i] = nodeService.sendRequest(dataNode, job);
		}
		
		//document 결과를 받는다.
		DocumentResult[] docResultList = new DocumentResult[collectionIdList.length];
		
		for (int i = 0; i < collectionIdList.length; i++) {
			String cId = collectionIdList[i];
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
			
			StreamableDocumentResult obj2 = (StreamableDocumentResult) obj;
			DocumentResult documentResult = obj2.documentResult();
			if(documentResult != null){
				docResultList[i] = documentResult;
			}else{
				logger.warn("{}의 documentList가 null입니다.", cId);
			}
		}
		String[] fieldIdList = docResultList[0].fieldIdList();
		Row[] rows = new Row[realSize];
		for (int i = 0; i < realSize; i++) {
			int collectionNumber = collectionTags[i];
			DocumentResult documentResult = docResultList[collectionNumber];
			rows[i] = documentResult.next();
		}
		
		/*
		 * Group Result
		 * */
		GroupsData groupsData = aggregatedSearchResult.getGroupsData();
		GroupResults groupResults = null;
		if(aggregatedSearchResult.getGroupsData() != null){
			groupResults = groups.getGroupsResultGenerator().generate(groupsData);
		}
		
		Result searchResult = new Result(rows, groupResults, fieldIdList, realSize, totalSize, meta.start());
		
		irService.searchCache().put(queryString, searchResult);
		logger.debug("CACHE_PUT result>>{}, qr >>{}", searchResult, queryString);
		
		logger.debug("ClusterSearchJob 수행시간 : {}", Strings.getHumanReadableTimeInterval(System.currentTimeMillis() - st));
		return new JobResult(searchResult);
	}

}
