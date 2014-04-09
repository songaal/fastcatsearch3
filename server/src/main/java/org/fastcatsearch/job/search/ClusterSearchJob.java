package org.fastcatsearch.job.search;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.group.GroupResult;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.group.GroupsData;
import org.fastcatsearch.ir.io.FixedHitReader;
import org.fastcatsearch.ir.query.Groups;
import org.fastcatsearch.ir.query.HighlightInfo;
import org.fastcatsearch.ir.query.InternalSearchResult;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.QueryModifier;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.query.ResultModifier;
import org.fastcatsearch.ir.query.Row;
import org.fastcatsearch.ir.query.RowExplanation;
import org.fastcatsearch.ir.query.ViewContainer;
import org.fastcatsearch.ir.search.DocIdList;
import org.fastcatsearch.ir.search.DocumentResult;
import org.fastcatsearch.ir.search.Explanation;
import org.fastcatsearch.ir.search.HitElement;
import org.fastcatsearch.ir.search.SearchResultAggregator;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.internal.InternalDocumentSearchJob;
import org.fastcatsearch.job.internal.InternalSearchJob;
import org.fastcatsearch.query.QueryMap;
import org.fastcatsearch.query.QueryParseException;
import org.fastcatsearch.query.QueryParser;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableDocumentResult;
import org.fastcatsearch.transport.vo.StreamableInternalSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 검색을 수행하여 병합까지 마치는 broker 검색작업.
 * */
public class ClusterSearchJob extends Job {

	private static final long serialVersionUID = 2375551165135599911L;
	protected static Logger searchLogger = LoggerFactory.getLogger("SEARCH_LOG");

	@Override
	public JobResult doRun() throws FastcatSearchException {

		long st = System.nanoTime();
		QueryMap queryMap = (QueryMap) getArgs();
		boolean noCache = false;
		String collectionId = null;
		String searchKeyword = null;
		Query q = null;
		boolean isCache = false;
		Result searchResult = null;
		try {
			
			try {
				q = QueryParser.getInstance().parseQuery(queryMap);
			} catch (QueryParseException e) {
				throw new FastcatSearchException("[Query Parsing Error] " + e.getMessage());
			}
	
			Metadata meta = q.getMeta();
			QueryModifier queryModifier = meta.queryModifier();
			//쿼리모디파이.
			if (queryModifier != null) {
				q = queryModifier.modify(q);
				meta = q.getMeta();
			}
			
			collectionId = meta.collectionId();
			searchKeyword = meta.getUserData("KEYWORD");
			// no cache 옵션이 없으면 캐시를 확인한다.
			if (meta.isSearchOption(Query.SEARCH_OPT_NOCACHE)) {
				noCache = true;
			}
	
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			if (!noCache) {
				Result result = irService.searchCache().get(queryMap.queryString());
				// logger.debug("CACHE_GET result>>{}, qr >>{}", result, queryMap.queryString());
				if (result != null) {
					isCache = true;
					return new JobResult(result);
				}
			}
			
			NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);

			Groups groups = q.getGroups();

			// TODO collectionId가 collectionGroup명인지 확인하여 그룹이면 여러컬렉션 검색.

			String[] collectionIdList = collectionId.split(",");// new String[]{ collectionId };

			// CollectionContext collectionContext = irService.collectionContext(collectionId);
			// 무조건 첫번째 context사용. 모든 컬렉션이 동일하다고 가정.
			CollectionContext collectionContext = irService.collectionContext(collectionIdList[0]);

			ResultFuture[] resultFutureList = new ResultFuture[collectionIdList.length];
			Map<String, Integer> collectionNumberMap = new HashMap<String, Integer>();
			Node[] selectedNodeList = new Node[collectionIdList.length];

			boolean forMerging = collectionIdList.length > 1;
			for (int i = 0; i < collectionIdList.length; i++) {
				String id = collectionIdList[i];
				collectionNumberMap.put(id, i);

				Node dataNode = nodeService.getBalancedNode(id);
				if (dataNode == null) {
					// 적합한 살아있는 노드를 찾지못함.
					logger.error("Not Found Node for {}", id);
					continue;
				}
				selectedNodeList[i] = dataNode;

				QueryMap newQueryMap = queryMap.clone();
				newQueryMap.setId(id);
				logger.debug("query-{} {} >> {}", i, id, newQueryMap);
				// collectionId가 하나이상이면 머징을 해야한다.
				InternalSearchJob job = new InternalSearchJob(newQueryMap, forMerging);
				resultFutureList[i] = nodeService.sendRequest(dataNode, job);
			}

			List<InternalSearchResult> resultList = new ArrayList<InternalSearchResult>(collectionIdList.length);
			HighlightInfo highlightInfo = null;

			for (int i = 0; i < collectionIdList.length; i++) {
				// TODO 노드 접속불가일경우 resultFutureList[i]가 null로 리턴됨.
				if (resultFutureList[i] == null) {
					throw new FastcatSearchException("요청메시지 전송불가에러.");
				}
				Object obj = resultFutureList[i].take();
				if (!resultFutureList[i].isSuccess()) {
					if (obj instanceof Throwable) {
						throw new FastcatSearchException("검색수행중 에러발생.", (Throwable) obj);
					} else {
						throw new FastcatSearchException("검색수행중 에러발생.");
					}
				}

				StreamableInternalSearchResult obj2 = (StreamableInternalSearchResult) obj;
				InternalSearchResult internalSearchResult = obj2.getInternalSearchResult();
				internalSearchResult.setNodeId(selectedNodeList[i].id());
				resultList.add(internalSearchResult);

				// TODO highlightInfo 들을 머지해야하나?
				highlightInfo = internalSearchResult.getHighlightInfo();

			}

			//
			// collectionIdList 내의 스키마는 동일하다는 가정하에 진행한다. collectionIdList[0] 의 스키마를 가져온다.
			//

			Schema schema = collectionContext.schema();
			SearchResultAggregator aggregator = new SearchResultAggregator(q, schema);
			InternalSearchResult aggregatedSearchResult = aggregator.aggregate(resultList);
			int totalSize = aggregatedSearchResult.getTotalCount();
			List<Explanation> explanations = aggregatedSearchResult.getExplanations();

			// /
			// / 컬렉션별 도큐먼트를 가져와서 완전한 결과객체를 만든다.
			//

			// internalSearchResult의 결과를 보면서 컬렉션 별로 분류한다.
			int realSize = aggregatedSearchResult.getCount();
			DocIdList[] docIdList = new DocIdList[collectionIdList.length];
			int[] collectionTags = new int[realSize]; // 해당 문서가 어느 collection에 속하는지 알려주는 항목.
			ArrayDeque<Integer>[] eachScores = new ArrayDeque[collectionIdList.length];
			List<RowExplanation>[] rowExplanationsList = null;
			if(explanations != null){
				rowExplanationsList = new List[realSize];
			}
			
			for (int i = 0; i < collectionIdList.length; i++) {
				docIdList[i] = new DocIdList(realSize);
				eachScores[i] = new ArrayDeque<Integer>(realSize);
			}

			int idx = 0;
			FixedHitReader hitReader = aggregatedSearchResult.getFixedHitReader();
			while (hitReader.next()) {
				HitElement el = hitReader.read();
				int collectionNo = collectionNumberMap.get(el.collectionId());
				docIdList[collectionNo].add(el.segmentSequence(), el.docNo());
				eachScores[collectionNo].add(el.score());
				collectionTags[idx] = collectionNo;
				if(rowExplanationsList != null){
					rowExplanationsList[idx] = el.rowExplanations();
				}
				idx++;
			}

			// document 요청을 보낸다.
			resultFutureList = new ResultFuture[collectionIdList.length];
			ViewContainer views = q.getViews();
			String[] tags = q.getMeta().tags();
			for (int i = 0; i < collectionIdList.length; i++) {
				String cid = collectionIdList[i];
				Node dataNode = selectedNodeList[i];

				logger.debug("collection [{}] search at {}", cid, dataNode);

				InternalDocumentSearchJob job = new InternalDocumentSearchJob(cid, docIdList[i], views, tags, highlightInfo);
				resultFutureList[i] = nodeService.sendRequest(dataNode, job);
			}

			// document 결과를 받는다.
			DocumentResult[] docResultList = new DocumentResult[collectionIdList.length];

			for (int i = 0; i < collectionIdList.length; i++) {
				String shardId = collectionIdList[i];
				// 노드 접속불가일경우 resultFutureList[i]가 null로 리턴됨.
				if (resultFutureList[i] == null) {
					throw new FastcatSearchException("요청메시지 전송불가에러.");
				}

				Object obj = resultFutureList[i].take();
				if (!resultFutureList[i].isSuccess()) {
					if (obj instanceof Throwable) {
						throw new FastcatSearchException("검색수행중 에러발생.", (Throwable) obj);
					} else {
						throw new FastcatSearchException("검색수행중 에러발생.");
					}
				}

				StreamableDocumentResult obj2 = (StreamableDocumentResult) obj;
				DocumentResult documentResult = obj2.documentResult();
				if (documentResult != null) {
					docResultList[i] = documentResult;
				} else {
					logger.warn("{}의 documentList가 null입니다.", shardId);
				}
			}
			String[] fieldIdList = docResultList[0].fieldIdList();
			Row[] rows = new Row[realSize];
			for (int i = 0; i < realSize; i++) {
				int collectionNo = collectionTags[i];
				DocumentResult documentResult = docResultList[collectionNo];
				rows[i] = documentResult.next();
				int score = eachScores[collectionNo].pop();
				rows[i].setScore(score);
			}

			//TODO row별과 통합 explain결과 포함시킨다.
			
			
			
			
			
			
			/*
			 * Group Result
			 */
			GroupsData groupsData = aggregatedSearchResult.getGroupsData();
			GroupResults groupResults = null;
			if (aggregatedSearchResult.getGroupsData() != null) {
				groupResults = groups.getGroupResultsGenerator().generate(groupsData);
			}

			searchResult = new Result(rows, groupResults, fieldIdList, realSize, totalSize, meta.start(), explanations, rowExplanationsList);

			ResultModifier resultModifier = meta.resultModifier();
			if(resultModifier != null){
				searchResult = resultModifier.modify(searchResult);
			}
			
			if(!noCache && realSize > 0){
				irService.searchCache().put(queryMap.queryString(), searchResult);
			}
//			logger.debug("CACHE_PUT result>>{}, qr >>{}", searchResult, queryMap.queryString());

//			logger.debug("ClusterSearchJob 수행시간 : {}", Strings.getHumanReadableTimeInterval((System.nanoTime() - st) / 1000000));
			return new JobResult(searchResult);
		}catch(Throwable t){
			throw new FastcatSearchException(t);
		} finally {
			//로깅은 반드시 수행한다.
			writeSearchLog(collectionId, searchKeyword, searchResult, (System.nanoTime() - st) / 1000000, isCache);
		}
	}

	private static String LOG_DELIMITER = "\t";
	private static String CACHE = "[cache]";
	private static String NOCACHE = "[nocache]";
	
	protected void writeSearchLog(String collectionId, String searchKeyword, Object obj, long searchTime, boolean isCache) {
		int count = -1;
		int totalCount = -1;
		GroupResults groupResults = null;

		if (obj instanceof Result) {
			Result result = (Result) obj;
			count = result.getCount();
			totalCount = result.getTotalCount();
			groupResults = result.getGroupResult();
		}

		StringBuffer logBuffer = new StringBuffer();

		logBuffer.append(isCache ? CACHE : NOCACHE);
		logBuffer.append(LOG_DELIMITER);
		
		logBuffer.append(collectionId);
		logBuffer.append(LOG_DELIMITER);

		logBuffer.append(searchKeyword);
		logBuffer.append(LOG_DELIMITER);

		logBuffer.append(searchTime);
		logBuffer.append(LOG_DELIMITER);

		logBuffer.append(count);
		logBuffer.append(LOG_DELIMITER);

		logBuffer.append(totalCount);

		if (groupResults != null) {
			logBuffer.append(LOG_DELIMITER);
			int groupSize = groupResults.groupSize();
			for (int i = 0; i < groupSize; i++) {
				GroupResult groupResult = groupResults.getGroupResult(i);
				if (i > 0) {
					logBuffer.append(";");
				}
				logBuffer.append(groupResult.size());
			}
		}
		searchLogger.info(logBuffer.toString());
	}
}
