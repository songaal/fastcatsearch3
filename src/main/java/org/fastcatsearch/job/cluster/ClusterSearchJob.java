package org.fastcatsearch.job.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.Strings;
import org.fastcatsearch.control.JobException;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.data.DataService;
import org.fastcatsearch.data.DataStrategy;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.Field;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.ir.config.Schema;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.field.DocNoField;
import org.fastcatsearch.ir.field.MultiValueField;
import org.fastcatsearch.ir.field.ScoreField;
import org.fastcatsearch.ir.field.SingleValueField;
import org.fastcatsearch.ir.field.UnknownField;
import org.fastcatsearch.ir.io.AsciiCharTrie;
import org.fastcatsearch.ir.io.FixedHitReader;
import org.fastcatsearch.ir.query.Groups;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.QueryParseException;
import org.fastcatsearch.ir.query.QueryParser;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.query.Row;
import org.fastcatsearch.ir.query.ShardSearchResult;
import org.fastcatsearch.ir.query.View;
import org.fastcatsearch.ir.search.HitElement;
import org.fastcatsearch.ir.search.SearchResultAggregator;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.internal.InternalDocumentRequestJob;
import org.fastcatsearch.job.internal.InternalSearchJob;
import org.fastcatsearch.service.IRService;
import org.fastcatsearch.service.ServiceException;
import org.fastcatsearch.transport.vo.StreamableDocumentList;
import org.fastcatsearch.transport.vo.StreamableShardSearchResult;

public class ClusterSearchJob extends Job {

	@Override
	public JobResult doRun() throws JobException, ServiceException {
		
		long st = System.currentTimeMillis();
		String[] args = getStringArrayArgs();
		String queryString = args[0];
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
		
		Metadata meta = q.getMeta();
		String collectionId = q.getMeta().collectionName();
//		Groups groups = q.getGroups();
		
		String[] collectionIdList = collectionId.split(",");
		ResultFuture[] resultFutureList = new ResultFuture[collectionIdList.length];
		Map<String, Integer> collectionNumberMap = new HashMap<String, Integer>();
		
		for (int i = 0; i < collectionIdList.length; i++) {
			String cId = collectionIdList[i];
			collectionNumberMap.put(cId, i);
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
		ShardSearchResult internalSearchResult = aggregator.aggregate(resultList);
		int totalSize = internalSearchResult.getTotalCount();
		
		
		///
		/// 컬렉션별 도큐먼트를 가져와서 완전한 결과객체를 만든다.
		//
		
		//internalSearchResult의 결과를 보면서 컬렉션 별로 분류한다.
		int realSize = internalSearchResult.getCount();
		int[][] docIdList = new int[collectionIdList.length][];
		int[] length = new int[collectionIdList.length];
		int[] collectionTags = new int[realSize]; //해당 문서가 어느컬렉션에 속하는지 알려주는 항목.
		int[] eachDocIds = new int[realSize];
		
		
		for (int i = 0; i < realSize; i++) {
			docIdList[i] = new int[realSize];
		}
		
		int idx = 0;
		FixedHitReader hitReader = internalSearchResult.getFixedHitReader();
		while(hitReader.next()){
			HitElement el = hitReader.read();
			int collectionNo = collectionNumberMap.get(el.collection());
			docIdList[collectionNo][ length[collectionNo]++ ] = el.docNo();
			collectionTags[idx] = collectionNo;
			eachDocIds[idx] = el.docNo();
			idx++;
		}
		
		//document 요청을 보낸다.
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
		
		//document 결과를 받는다.
		Iterator<Document>[] docResultList = new Iterator[collectionIdList.length];
		
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
			
			StreamableDocumentList obj2 = (StreamableDocumentList) obj;
			List<Document> documentList = obj2.documentList();
			if(documentList != null){
				docResultList[i] = documentList.iterator();
			}
		}
		
		Row[] row = new Row[realSize];
		AsciiCharTrie fieldnames = schema.fieldnames;
		List<View> views = q.getViews();
		Iterator<View> iter = views.iterator();
		int fieldSize = views.size();
		List<View> fieldNames = q.getViews();
		String[] fieldNameList = new String[fieldNames.size()];
		int[] fieldNumList = new int[fieldSize];
		for (int i = 0; i < fieldNames.size(); i++) {
			View view = fieldNames.get(i);
			fieldNameList[i] = view.fieldname();
			int num = fieldnames.get(fieldNameList[i]);
//			if(num >= 0){
//				fieldSummarySize[num] = view.summarySize();
//				logger.trace("Summary size = {} : {}", num, fieldSummarySize[num]);
//			}
		}
		
		
		//search 조건에 입력한 요약옵션(8)과 별도로 view에 셋팅한 요약길이를 확인하여 검색필드가 아니더라도 요약해주도록함.
		int[] extraSnipetSize = new int[fieldSize];
		
//		logger.debug("fieldSize = "+fieldSize);
		int jj = 0;
		while(iter.hasNext()){
			View v = iter.next();
			String fn = v.fieldname();
			int i = -1;
//			logger.debug("fn = "+fn);
			
			if(fn.equalsIgnoreCase(ScoreField.fieldName)){
				i = ScoreField.fieldNumber;
			}else if(fn.equalsIgnoreCase(DocNoField.fieldName)){
				i = DocNoField.fieldNumber;
			}else{
				i = fieldnames.get(fn);
			}
			
			fieldNumList[jj] = i;
			
			if(v.summarySize() > 0){
				extraSnipetSize[jj] = v.summarySize();
			}else{
				extraSnipetSize[jj] = -1;
			}
			
			jj++;
		}
		
		//
		//TODO 일단 highlight는 없다. 구현필요.
		//
		for (int i = 0; i < realSize; i++) {
			int collectionNumber = collectionTags[0];
			Iterator<Document> docIterator = docResultList[collectionNumber];
//			int segmentNumber = tags[i];
//			int p = pos[segmentNumber]++;
//			Document document = eachDocList[segmentNumber][p];
			Document document = docIterator.next();//eachDocList[i];
			row[i] = new Row(fieldSize);
			for (int j = 0; j < fieldSize; j++) {
				int fieldNum = fieldNumList[j];
				if(fieldNum == ScoreField.fieldNumber){
					int score = document.getScore();
					row[i].put(j, ScoreField.getChars(score));
				}else if(fieldNum == DocNoField.fieldNumber){
//					row[i].put(j, (eachDocIds[segmentNumber][p]+"").toCharArray());
					row[i].put(j, Integer.toString(eachDocIds[i]).toCharArray());
				}else if(fieldNum == UnknownField.fieldNumber){
					row[i].put(j, UnknownField.getChars());
				}else{
					Field field = document.get(fieldNum);
//					logger.debug("field-"+i+" = "+field);
					if(field.isMultiValue()){
						MultiValueField mvField = (MultiValueField)field;
						char[][] dispChars = mvField.getDisplayChars();
//						logger.debug("dispChars.len = "+dispChars.length);
//						if(hilightInfoList[fieldNum] != null && has != null){
////							logger.debug("modify field - "+j+" = "+hilightInfoList[fieldNum]);
//							StringBuilder str = new StringBuilder();
//							for (int k = 0; k < dispChars.length; k++) {
//								char[] data = has.modify(hilightInfoList[fieldNum], dispChars[k], meta.tags());
//								str.append(data);
//								if(k < dispChars.length - 1){
//									str.append(mvField.delimiter());
//								}
//							}
//							row[i].put(j, str.toString().toCharArray());
//						}else{
							StringBuilder str = new StringBuilder();
							for (int k = 0; k < dispChars.length; k++) {
								str.append(dispChars[k]);
								if(k < dispChars.length - 1)
									str.append(mvField.delimiter());
							}
							row[i].put(j, str.toString().toCharArray());
//						}
					}else{
						SingleValueField svField = (SingleValueField)field;
						char[] data = null;
						//do summarize, highlight
//						if(hilightInfoList[fieldNum] != null && has != null){
////						logger.debug("modify field - "+j+" = "+hilightInfoList[fieldNum]);
//							data = has.modify(hilightInfoList[fieldNum], svField.getDisplayChars(), meta.tags());
//						}else{
							data = svField.getDisplayChars();
//						}
						row[i].put(j, data);
					}
				}
			}
		}
		
		Result searchResult = new Result(row, fieldSize, fieldNameList, realSize, totalSize, meta);
		
		if(searchResult != null){
			IRService.getInstance().searchCache().put(queryString, searchResult);
			logger.debug("CACHE_PUT result>>{}, qr >>{}", searchResult, queryString);
		}
		
		logger.debug("ClusterGroupSearchJob 수행시간 : {}", Strings.getHumanReadableTimeInterval(System.currentTimeMillis() - st));
		return new JobResult(searchResult);
	}

}
