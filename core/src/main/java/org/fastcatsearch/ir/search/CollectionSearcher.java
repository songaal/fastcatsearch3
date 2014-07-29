package org.fastcatsearch.ir.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.analysis.AnalyzerPool;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.field.DocNoField;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.field.ScoreField;
import org.fastcatsearch.ir.field.UnknownField;
import org.fastcatsearch.ir.group.GroupDataMerger;
import org.fastcatsearch.ir.group.GroupHit;
import org.fastcatsearch.ir.group.GroupsData;
import org.fastcatsearch.ir.io.FixedHitQueue;
import org.fastcatsearch.ir.io.FixedHitReader;
import org.fastcatsearch.ir.io.FixedMinHeap;
import org.fastcatsearch.ir.query.Bundle;
import org.fastcatsearch.ir.query.Groups;
import org.fastcatsearch.ir.query.HighlightInfo;
import org.fastcatsearch.ir.query.InternalSearchResult;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.Row;
import org.fastcatsearch.ir.query.Sorts;
import org.fastcatsearch.ir.query.Term;
import org.fastcatsearch.ir.query.Term.Option;
import org.fastcatsearch.ir.query.View;
import org.fastcatsearch.ir.query.ViewContainer;
import org.fastcatsearch.ir.search.clause.Clause;
import org.fastcatsearch.ir.search.clause.ClauseException;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.summary.BasicHighlightAndSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionSearcher {
	private static Logger logger = LoggerFactory.getLogger(CollectionSearcher.class);

	private String collectionId;
	private CollectionHandler collectionHandler;

	private HighlightAndSummary has;

	public CollectionSearcher(CollectionHandler collectionHandler) {
		this.collectionId = collectionHandler.collectionId();
		this.collectionHandler = collectionHandler;
		has = new BasicHighlightAndSummary();
	}

	public GroupsData doGrouping(Query q) throws IRException, IOException, SettingException {
		
		int segmentSize = collectionHandler.segmentSize();
		if (segmentSize == 0) {
			logger.warn("Collection {} is not indexed!", collectionHandler.collectionId());
		}

		Groups groups = q.getGroups();

		if (groups == null) {
			return null;
		}

		if (segmentSize == 1) {
			// 머징필요없음.
			try {
				GroupHit groupHit = collectionHandler.segmentSearcher(0).searchGroupHit(q);
				return groupHit.groupData();
			} catch (IOException e) {
				throw new IRException(e);
			} catch (ClauseException e) {
				throw new IRException(e);
			}
		} else {

			GroupDataMerger dataMerger = null;
			if (groups != null) {
				dataMerger = new GroupDataMerger(groups, segmentSize);
			}

			try {
				for (int i = 0; i < segmentSize; i++) {
					GroupHit groupHit = collectionHandler.segmentSearcher(i).searchGroupHit(q);

					if (dataMerger != null) {
						dataMerger.put(groupHit.groupData());
					}
				}
			} catch (IOException e) {
				throw new IRException(e);
			} catch (ClauseException e) {
				throw new IRException(e);
			}

			GroupsData groupData = null;
			if (dataMerger != null) {
				groupData = dataMerger.merge();
			}
			return groupData;
		}

	}

	// id리스트에 해당하는 document자체를 읽어서 리스트로 리턴한다.
	@Deprecated
	public List<Document> requestDocument(int[] docIdList) throws IOException {
		// eachDocList에 해당하는 문서리스트를 리턴한다.
		List<Document> documentList = new ArrayList<Document>(docIdList.length);

		int segmentSize = collectionHandler.segmentSize();
		for (int i = 0; i < docIdList.length; i++) {
			int docNo = docIdList[i];

			// make doc number lists to send each columns
			for (int m = segmentSize - 1; m >= 0; m--) {
				if (docNo >= collectionHandler.segmentReader(m).segmentInfo().getBaseNumber()) {
					documentList.add(collectionHandler.segmentReader(m).segmentSearcher().getDocument(docNo));
					break;
				}
			}
		}

		return documentList;
	}
	
	public Document requestDocument(int docNo) throws IOException {

		int segmentSize = collectionHandler.segmentSize();
		// make doc number lists to send each columns
		for (int m = segmentSize - 1; m >= 0; m--) {
			if (docNo >= collectionHandler.segmentReader(m).segmentInfo().getBaseNumber()) {
				return collectionHandler.segmentReader(m).segmentSearcher().getDocument(docNo);
			}
		}
		return null;
	}

	public InternalSearchResult searchInternal(Query q) throws IRException, IOException, SettingException {
		return searchInternal(q, false, null);
	}
	
	/**
	 * @param forMerging : 머징용도이면 start + length 만큼을 앞에서부터 모두 가져온다. 
	 * */
	public InternalSearchResult searchInternal(Query q, boolean forMerging) throws IRException, IOException, SettingException {
		return searchInternal(q, forMerging, null);
	}
	
	public InternalSearchResult searchInternal(Query q, boolean forMerging, PkScoreList boostList) throws IRException, IOException, SettingException {
		int segmentSize = collectionHandler.segmentSize();
		if (segmentSize == 0) {
			logger.warn("Collection {} is not indexed!", collectionId);
		}

//		logger.debug("searchInternal incrementCount > {} ", q);
		collectionHandler.queryCounter().incrementCount();
		
		Schema schema = collectionHandler.schema();
		
		Metadata meta = q.getMeta();
		int start = meta.start();
		int rows = meta.rows();
		
		if (forMerging) {// 앞에서 부터 모두.
			rows += start;
		}
		
//		if(collectionId == null){
//			collectionId = meta.collectionId();
//		}
		Groups groups = q.getGroups();

		Sorts sorts = q.getSorts();
		FixedMinHeap<FixedHitReader> hitMerger = null;
		if (sorts != null) {
			hitMerger = sorts.createMerger(schema, segmentSize);
		} else {
			hitMerger = new FixedMinHeap<FixedHitReader>(segmentSize);
		}

		GroupDataMerger dataMerger = null;
		if (groups != null) {
			dataMerger = new GroupDataMerger(groups, segmentSize);
		}

		HighlightInfo highlightInfo = null;

		int totalSize = 0;
		List<Explanation> explanationList = null;
		try {
			for (int i = 0; i < segmentSize; i++) {
				Hit hit = collectionHandler.segmentSearcher(i).searchHit(q, boostList);
				if (highlightInfo == null) {
					highlightInfo = hit.highlightInfo();
				}

				totalSize += hit.totalCount();
				FixedHitReader hitReader = hit.hitStack().getReader();

				GroupsData groupData = hit.groupData();

				// posting data
				if (hitReader.next()) {
					hitMerger.push(hitReader);
				}
				// Put GroupResult

				if (dataMerger != null) {
					dataMerger.put(groupData);
				}
				
				if(hit.explanation() != null){
					if(explanationList == null){
						explanationList = new ArrayList<Explanation>();
					}
					hit.explanation().setSegmentId(i);
					hit.explanation().setCollectionId(collectionId);
					explanationList.add(hit.explanation());
				}
			}
		} catch (IOException e) {
			throw new IRException(e);
		} catch (ClauseException e) {
			throw new IRException(e);
		}

		// 각 세그먼트의 결과들을 rankdata를 기준으로 재정렬한다.
		FixedHitQueue totalHit = new FixedHitQueue(rows);
		int c = 1, n = 0;

		// 이미 각 세그먼트의 결과들은 정렬이되어서 전달이 된다.
		// 그러므로, 여기서는 원하는 갯수가 다 차면 더이상 정렬을 수행할 필요없이 early termination이 가능하다.
		while (hitMerger.size() > 0) {
			FixedHitReader r = hitMerger.peek();
			HitElement el = r.read();
			
			if (forMerging) {
				//머징용도는 처음부터 모두 넣는다.
				totalHit.push(el);
				n++;
			}else if (c >= start) {
				//차후 머징용도가 아니라면 start이후 부터만 가져온다. 
				totalHit.push(el);
				n++;
			}
			c++;

			// 결과가 만들어졌으면 일찍 끝낸다.
			if (n == rows)
				break;

			if (!r.next()) {
				// 다 읽은 것은 버린다.
				hitMerger.pop();
			}
			hitMerger.heapify();
		}

		GroupsData groupData = null;
		if (dataMerger != null) {
			groupData = dataMerger.merge();
		}
		
		HitElement[] hitElementList = totalHit.getHitElementList();
		int size = totalHit.size();
		/*
		 * 번들 요청이 있으면 하위 묶음문서를 찾아온다.
		 * */
		Bundle bundle = q.getBundle();
		if(bundle != null) {
			fillBundleResult(schema, segmentSize, hitElementList, size, bundle);
		}
		return new InternalSearchResult(collectionId, hitElementList, size, totalSize, groupData, highlightInfo, explanationList);
	}
	
	/*
	 * 번들 문서를 찾아온다.
	 * */
	private void fillBundleResult(Schema schema, int segmentSize, HitElement[] hitElementList, int size, Bundle bundle) throws IRException{
		/*
		 * el의 bundlekey를 보고 하위 묶음문서가 몇개가 있는지 확인한다.
		 * 2개 이상일 경우만 저장하고 나머지는 버린다.
		 */
		String fieldIndexId = bundle.getFieldIndexId();
		Sorts bundleSorts = bundle.getSorts();
		int bundleRows = bundle.getRows();
		int bundleStart = 1;
		try {
			for (int k = 0; k < size; k++) {
				int totalSize = 0;
				//bundleKey로 clause생성한다.
				BytesRef bundleKey = hitElementList[k].getBundleKey();
				String bundleStringKey = bundleKey.toAlphaString();
				Clause bundleClause = new Clause(new Term(fieldIndexId, bundleStringKey));
				Hit[] segmentHitList = new Hit[segmentSize];
				
				for (int i = 0; i < segmentSize; i++) {
					//bundle key 별로 결과를 모은다.
					segmentHitList[i] = collectionHandler.segmentSearcher(i).searchIndex(bundleClause, bundleSorts, bundleStart, bundleRows);
					totalSize += segmentHitList[i].totalCount();
				}
				
				//2이상이어야만 번들이 유효하다.
				if(totalSize > 1) {
					
					FixedMinHeap<FixedHitReader> hitMerger = null;
					if (bundleSorts != null) {
						hitMerger = bundleSorts.createMerger(schema, segmentSize);
					} else {
						hitMerger = new FixedMinHeap<FixedHitReader>(segmentSize);
					}
					
					for (int i = 0; i < segmentSize; i++) {
						FixedHitReader hitReader = segmentHitList[i].hitStack().getReader();
	//					// posting data
						if (hitReader.next()) {
							hitMerger.push(hitReader);
						}
					}
					
					int realSize = Math.min(bundleRows, totalSize);
					DocIdList bundleDocIdList = new DocIdList(realSize);
					int c = 1, n = 0;
					while (hitMerger.size() > 0) {
						FixedHitReader r = hitMerger.peek();
						HitElement el = r.read();
						
						if (c >= bundleStart) {
							bundleDocIdList.add(el.segmentSequence(), el.docNo());
							logger.debug("[{}] {}", el.segmentSequence() ,el.docNo());
						}
						c++;

						// 결과가 만들어졌으면 일찍 끝낸다.
						if (n == bundleRows)
							break;

						if (!r.next()) {
							// 다 읽은 것은 버린다.
							hitMerger.pop();
						}
						hitMerger.heapify();
					}
					
					hitElementList[k].setBundleDocIdList(bundleDocIdList);
				}
				
				
			}
		} catch (ClauseException e) {
			throw new IRException(e);
		} catch (IOException e) {
			throw new IRException(e);
		}
		
		
	}

	public DocumentResult searchDocument(DocIdList list, ViewContainer views, String[] tags, HighlightInfo highlightInfo) throws IOException {
		int realSize = list.size();
		Row[] row = new Row[realSize];

		int fieldSize = collectionHandler.schema().getFieldSize();
		int viewSize = views.size();
		int[] fieldSequenceList = new int[viewSize];
		String[] fieldIdList = new String[viewSize];
		boolean[] fieldSelectOption = new boolean[fieldSize]; // true인 index의 필드값만 채워진다.
		for (int i = 0; i < views.size(); i++) {
			View v = views.get(i);
			String fieldId = v.fieldId();
			fieldIdList[i] = fieldId;
			int sequence = -1;
			if (fieldId.equalsIgnoreCase(ScoreField.fieldName)) {
				sequence = ScoreField.fieldNumber;
			} else if (fieldId.equalsIgnoreCase(DocNoField.fieldName)) {
				sequence = DocNoField.fieldNumber;
			} else {
				sequence = collectionHandler.schema().getFieldSequence(fieldId);
				if(sequence != -1){
					fieldSelectOption[sequence] = true;
				}
			}

			fieldSequenceList[i] = sequence;
		}

		Document[] eachDocList = new Document[realSize];
		Document[][] eachBundleDocList = new Document[realSize][];
		
		//SegmentSearcher를 재사용하기 위한 array. Lazy-loading되며, segmentSequence가 array 첨자가 된다.
		//처음에는 길이 5로 만들어놓고 나중에 더 필요하면, grow시킨다.
		SegmentSearcher[] segmentSearcherList = new SegmentSearcher[5];
		
		int idx = 0;
		for (int i = 0; i < list.size(); i++) {
			int segmentSequence = list.segmentSequence(i);
			int docNo = list.docNo(i);
			DocIdList bundleDocIdList = list.bundleDocIdList(i);
			int size = segmentSearcherList.length;
			
			//기존 범위를 벗어나는 세그먼트 요청이 있을 때 grow한다. 
			if(segmentSequence >= size){
				while(segmentSequence >= size){
					size += 5;
				}
				SegmentSearcher[] newSegmentSearcherList = new SegmentSearcher[size];
				System.arraycopy(segmentSearcherList, 0, newSegmentSearcherList, 0, segmentSearcherList.length);
				segmentSearcherList = newSegmentSearcherList;
			}
			
			if(segmentSearcherList[segmentSequence] == null) {
				segmentSearcherList[segmentSequence] = collectionHandler.segmentReader(segmentSequence).segmentSearcher();
			}
			Document doc = segmentSearcherList[segmentSequence].getDocument(docNo, fieldSelectOption);
			eachDocList[idx] = doc;
			
			if(bundleDocIdList != null) {
				Document[] bundleDoclist = new Document[bundleDocIdList.size()];
				for (int j = 0; j < bundleDocIdList.size(); j++) {
					int bundleSegmentSequence = bundleDocIdList.segmentSequence(i);
					int bundleDocNo = bundleDocIdList.docNo(i);
					Document bundleDoc = collectionHandler.segmentReader(bundleSegmentSequence).segmentSearcher().getDocument(bundleDocNo, fieldSelectOption);
					bundleDoclist[j] = bundleDoc;
				}
				eachBundleDocList[idx] = bundleDoclist;
			}
			
			idx++;
		}
		
		
		for (int i = 0; i < realSize; i++) {
			Document document = eachDocList[i];
			row[i] = new Row(viewSize);
			for (int j = 0; j < viewSize; j++) {
				View view = views.get(j);

				int fieldSequence = fieldSequenceList[j];
				if (fieldSequence == ScoreField.fieldNumber) {
					//여기서는 score를 알수가 없으므로 공백처리.
					//float score = document.getScore();
					row[i].put(j, null);
				} else if (fieldSequence == DocNoField.fieldNumber) {
					row[i].put(j, Integer.toString(document.getDocId()).toCharArray());
				} else if (fieldSequence == UnknownField.fieldNumber) {
					row[i].put(j, UnknownField.value().toCharArray());
				} else {
					Field field = document.get(fieldSequence);
//					logger.debug("field#{} >> {}", j, field);
					String text = null;
					if (field != null) {
						text = field.toString();
					}

					boolean isHighlightSummary = false;
					if (has != null && text != null && highlightInfo != null) {
						//하이라이팅만 수행하거나, 또는 view.snippetSize 가 존재하면 summary까지 수행될수 있다.
						String fieldId = view.fieldId();
						Option searchOption = highlightInfo.getOption(fieldId);
						if(searchOption.useHighlight()) {
							String indexAnalyzerId = highlightInfo.getIndexAnalyzerId(fieldId);
							String queryAnalyzerId = highlightInfo.getQueryAnalyzerId(fieldId);
							String queryTerm = highlightInfo.getQueryTerm(fieldId);
							if (indexAnalyzerId != null && queryAnalyzerId != null && queryTerm != null) {
	//							a = System.nanoTime();
								text = getHighlightedSnippet(fieldId, text, indexAnalyzerId, queryAnalyzerId, queryTerm, tags, view, searchOption);
	//							b += (System.nanoTime() - a);
								isHighlightSummary = true;
							}
						}
					}
					
					if(!isHighlightSummary && view.isSummarize()){
						//검색필드가 아니라서 하이라이팅이 불가능한경우는 앞에서부터 잘라 summary 해준다.
						if(text != null){
							if(text.length() > view.snippetSize()){
								text = text.substring(0, view.snippetSize());
							}
						}
					}

					if (text != null) {
						row[i].put(j, text.toCharArray());
					} else {
						row[i].put(j, null);
					}

				}
			}
			
			
			//bundle document
			
			Document[] bundleDocList = eachBundleDocList[i];
			for()
			
			
		}
//		logger.debug("time > {}, {}", (System.nanoTime() - c) / 1000000, b / 1000000);
		return new DocumentResult(row, fieldIdList);
	}

	private Row fromDocumentToRow(Document document) {
		Row rows = new Row(viewSize);
		for (int j = 0; j < viewSize; j++) {
			View view = views.get(j);

			int fieldSequence = fieldSequenceList[j];
			if (fieldSequence == ScoreField.fieldNumber) {
				//여기서는 score를 알수가 없으므로 공백처리.
				//float score = document.getScore();
				row[i].put(j, null);
			} else if (fieldSequence == DocNoField.fieldNumber) {
				row[i].put(j, Integer.toString(document.getDocId()).toCharArray());
			} else if (fieldSequence == UnknownField.fieldNumber) {
				row[i].put(j, UnknownField.value().toCharArray());
			} else {
				Field field = document.get(fieldSequence);
//				logger.debug("field#{} >> {}", j, field);
				String text = null;
				if (field != null) {
					text = field.toString();
				}

				boolean isHighlightSummary = false;
				if (has != null && text != null && highlightInfo != null) {
					//하이라이팅만 수행하거나, 또는 view.snippetSize 가 존재하면 summary까지 수행될수 있다.
					String fieldId = view.fieldId();
					Option searchOption = highlightInfo.getOption(fieldId);
					if(searchOption.useHighlight()) {
						String indexAnalyzerId = highlightInfo.getIndexAnalyzerId(fieldId);
						String queryAnalyzerId = highlightInfo.getQueryAnalyzerId(fieldId);
						String queryTerm = highlightInfo.getQueryTerm(fieldId);
						if (indexAnalyzerId != null && queryAnalyzerId != null && queryTerm != null) {
//							a = System.nanoTime();
							text = getHighlightedSnippet(fieldId, text, indexAnalyzerId, queryAnalyzerId, queryTerm, tags, view, searchOption);
//							b += (System.nanoTime() - a);
							isHighlightSummary = true;
						}
					}
				}
				
				if(!isHighlightSummary && view.isSummarize()){
					//검색필드가 아니라서 하이라이팅이 불가능한경우는 앞에서부터 잘라 summary 해준다.
					if(text != null){
						if(text.length() > view.snippetSize()){
							text = text.substring(0, view.snippetSize());
						}
					}
				}

				if (text != null) {
					row[i].put(j, text.toCharArray());
				} else {
					row[i].put(j, null);
				}

			}
		}
		
		return rows;
	}
	private String getHighlightedSnippet(String fieldId, String text, String indexAnalyzerId, String queryAnalyzerId, String queryString, String[] tags, View view, Option searchOption) throws IOException {
		AnalyzerPool queryAnalyzerPool = collectionHandler.analyzerPoolManager().getPool(queryAnalyzerId);
		//analyzer id 가 같으면 하나만 공통으로 사용한다.
		boolean isSamePool = queryAnalyzerId.equals(indexAnalyzerId);
		AnalyzerPool indexAnalyzerPool = null;
		
		if(isSamePool){
			indexAnalyzerPool = queryAnalyzerPool;
		}else{
			indexAnalyzerPool = collectionHandler.analyzerPoolManager().getPool(indexAnalyzerId);
		}
		
		if (queryAnalyzerPool != null) {
			Analyzer queryAnalyzer = queryAnalyzerPool.getFromPool();
			Analyzer indexAnalyzer = null;
			if(isSamePool){
				indexAnalyzer = queryAnalyzer;
			}else{
				indexAnalyzer = indexAnalyzerPool.getFromPool();
			}
			
			if (indexAnalyzer != null && queryAnalyzer != null) {
				try {
					text = has.highlight(fieldId, indexAnalyzer, queryAnalyzer, text, queryString, tags, view.snippetSize(), view.fragmentSize(), searchOption);
				} finally {
					if(!isSamePool){
						indexAnalyzerPool.releaseToPool(indexAnalyzer);
					}
					queryAnalyzerPool.releaseToPool(queryAnalyzer);
				}
			}
		}
		return text;
	}


}
