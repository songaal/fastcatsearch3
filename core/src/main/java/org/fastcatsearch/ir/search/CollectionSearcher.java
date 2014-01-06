package org.fastcatsearch.ir.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.fastcatsearch.ir.analysis.AnalyzerPool;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.document.DocumentReader;
import org.fastcatsearch.ir.document.PrimaryKeyIndexReader;
import org.fastcatsearch.ir.field.DocNoField;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.field.ScoreField;
import org.fastcatsearch.ir.field.UnknownField;
import org.fastcatsearch.ir.group.GroupDataMerger;
import org.fastcatsearch.ir.group.GroupHit;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.group.GroupsData;
import org.fastcatsearch.ir.io.BitSet;
import org.fastcatsearch.ir.io.BytesDataOutput;
import org.fastcatsearch.ir.io.FixedHitQueue;
import org.fastcatsearch.ir.io.FixedHitReader;
import org.fastcatsearch.ir.io.FixedMinHeap;
import org.fastcatsearch.ir.query.Groups;
import org.fastcatsearch.ir.query.HighlightInfo;
import org.fastcatsearch.ir.query.InternalSearchResult;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.query.Row;
import org.fastcatsearch.ir.query.Sorts;
import org.fastcatsearch.ir.query.View;
import org.fastcatsearch.ir.query.ViewContainer;
import org.fastcatsearch.ir.search.clause.ClauseException;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.RefSetting;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.summary.BasicHighlightAndSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionSearcher {
	private static Logger logger = LoggerFactory.getLogger(CollectionSearcher.class);

	private CollectionHandler collectionHandler;

	private HighlightAndSummary has;

	public CollectionSearcher(CollectionHandler collectionHandler) {
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

	public Result search(Query q) throws IRException, IOException, SettingException {
		collectionHandler.queryCounter().incrementCount();
		
		InternalSearchResult internalSearchResult = searchInternal(q);
		DocIdList hitList = internalSearchResult.getDocIdList();
		
		
		int realSize = internalSearchResult.getCount();
		HighlightInfo highlightInfo = internalSearchResult.getHighlightInfo();
		DocumentResult documentResult = searchDocument(hitList, q.getViews(), q.getMeta().tags(), highlightInfo);
		int fieldSize = q.getViews().size();
		int totalSize = internalSearchResult.getTotalCount();
		int start = q.getMeta().start();
		//groups
		Groups groups =  q.getGroups();
		GroupResults groupResults = null; 
		if(groups != null){
			GroupsData groupsData = internalSearchResult.getGroupsData();
			groupResults = groups.getGroupResultsGenerator().generate(groupsData);
		}
		return new Result(documentResult.rows(), groupResults, documentResult.fieldIdList(), realSize, totalSize, start);
	}

	public InternalSearchResult searchInternal(Query q) throws IRException, IOException, SettingException {
		int segmentSize = collectionHandler.segmentSize();
		if (segmentSize == 0) {
			logger.warn("Collection {} is not indexed!", collectionHandler.collectionId());
		}

		logger.debug("searchInternal incrementCount > {} ", q);
		collectionHandler.queryCounter().incrementCount();
		
		Schema schema = collectionHandler.schema();

		Metadata meta = q.getMeta();
		int start = meta.start();
		int rows = meta.rows();
		String collectionId = meta.collectionId();
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
		try {
			for (int i = 0; i < segmentSize; i++) {
				Hit hit = collectionHandler.segmentSearcher(i).searchHit(q);
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
			if (c >= start) {
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
		return new InternalSearchResult(collectionId, hitElementList, totalHit.size(), totalSize, groupData, highlightInfo);
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

		int idx = 0;
		for (int i = 0; i < list.size(); i++) {

			int segmentSequence = list.segmentSequence(i);
			int docNo = list.docNo(i);
			// 문서번호는 segmentSequence+docNo 에 유일하며, docNo만으로는 세그먼트끼리는 중복된다.
//			logger.debug("FOUND [segment seq#{}] docNo={}", segmentSequence, docNo);

			Document doc = collectionHandler.segmentReader(segmentSequence).segmentSearcher().getDocument(docNo, fieldSelectOption);
			eachDocList[idx++] = doc;
		}
//long a, b = 0, c = System.nanoTime();

		for (int i = 0; i < realSize; i++) {
			Document document = eachDocList[i];
			row[i] = new Row(viewSize);
//			logger.debug("document#{}---------------", i);
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

					if (has != null && text != null && highlightInfo != null) {
						String fiedlName = view.fieldId();
						String analyzerId = highlightInfo.getAnalyzer(fiedlName);
						String queryString = highlightInfo.getQueryString(fiedlName);
						if (analyzerId != null && queryString != null) {
//							a = System.nanoTime();
							text = getHighlightedSnippet(text, analyzerId, queryString, tags, view);
//							b += (System.nanoTime() - a);
						}
					}

					if (text != null) {
						row[i].put(j, text.toCharArray());
					} else {
						row[i].put(j, null);
					}

				}
			}
		}
//		logger.debug("time > {}, {}", (System.nanoTime() - c) / 1000000, b / 1000000);
		return new DocumentResult(row, fieldIdList);
	}

	private String getHighlightedSnippet(String text, String analyzerId, String queryString, String[] tags, View view) throws IOException {
		AnalyzerPool analyzerPool = collectionHandler.analyzerPoolManager().getPool(analyzerId);
		if (analyzerPool != null) {
			Analyzer analyzer = analyzerPool.getFromPool();
			if (analyzer != null) {
				try {
					text = has.highlight(analyzer, text, queryString, view.isHighlighted() ? tags : null, view.snippetSize(), view.fragmentSize());
				} finally {
					analyzerPool.releaseToPool(analyzer);
				}
			}
		}
		return text;
	}

	// 원문조회기능.
	public Result listDocument(String collectionId, int start, int rows) throws IRException, IOException, SettingException {
		if (collectionHandler.segmentSize() == 0) {
			logger.warn("Collection {} is not indexed!", collectionId);
		}

		Result result = new Result();

		Metadata meta = new Metadata();
		meta.setStart(start + 1);

		int fieldSize = collectionHandler.schema().getFieldSize();
		String[] fieldNameList = new String[fieldSize];
		List<FieldSetting> fieldSettingList = collectionHandler.schema().schemaSetting().getFieldSettingList();
		int[] fieldNumList = new int[fieldSize];
		for (int i = 0; i < fieldNumList.length; i++) {
			fieldNumList[i] = i;
			fieldNameList[i] = fieldSettingList.get(i).getId();
		}

		// /////////////////////////////////////
		int totalSize = 0;
		int segmentSize = collectionHandler.segmentSize();
		int pageIndex = 1;
		int deletedDocCount = 0;
		// 이 배열의 index번호는 세그먼트번호.
		int[] segEndNums = new int[segmentSize];
		ArrayList<Row> rowList = new ArrayList<Row>();

		for (int i = 0; i < segmentSize; i++) {
			int docCount = collectionHandler.segmentReader(i).segmentInfo().getRevisionInfo().getDocumentCount();
			totalSize += docCount;
			segEndNums[i] = totalSize - 1;
		}

		SegmentInfo lastSegInfo = collectionHandler.segmentReader(segmentSize - 1).segmentInfo();
		if (lastSegInfo == null) {
			logger.error("There is no indexed data.");
			throw new IRException("색인된 데이터가 없습니다.");
		}
		// 총문서갯수와 시작번호에 근거해서 이 시작번호가 어느 세그먼트에 속하고 이 세그먼트의 어느 위치에서 시작되는지를 구한다.
		// [세스머튼번호,시작번호,끝번호][2] = matchSegment(int[][], 조회시작변수);
		int[][] matchSeg = matchSegment(segEndNums, start, rows);
		// 해당 세그먼트의 deleteSet객체를 얻는다.
		for (int i = 0; i < matchSeg.length; i++) {
			int segNo = matchSeg[i][0];
			int startNo = matchSeg[i][1];
			int endNo = matchSeg[i][2];
			// logger.debug("segNo: "+segNo+"startNo "+startNo+"endNo "+endNo);
			SegmentReader segmentReader = collectionHandler.segmentReader(segNo);
			// File targetDir = segmentReader.getSegmentDir();
			// int lastRevision = segmentReader.getLastRevision();
			String segmentId = segmentReader.segmentInfo().getId();
			int revision = segmentReader.segmentInfo().getRevision();
			DocumentReader reader = new DocumentReader(collectionHandler.schema().schemaSetting(), segmentReader.segmentDir());
			BitSet deleteSet = null;
			deleteSet = new BitSet(segmentReader.revisionDir(), IndexFileNames.getSuffixFileName(IndexFileNames.docDeleteSet, segmentId));

			for (int j = startNo; j <= endNo; j++) {
				Document document = reader.readDocument(j);
				Row row = new Row(fieldSize);
				row.setRowTag(segNo + "-" + revision + "-" + j);
				for (int m = 0; m < fieldSize; m++) {
					int fieldNum = fieldNumList[m];
					Field field = document.get(fieldNum);
					row.put(m, field.toString().toCharArray());
				}
				if (deleteSet.isSet(j)) {
					row.setDeleted(true);
					deletedDocCount++;
				}
				rowList.add(row);
			}
		}

		Row[] row = new Row[rowList.size()];
		for (int i = 0; i < rowList.size(); i++) {
			row[i] = rowList.get(i);
		}

		// //////////////////////////////////////

		result = new Result(row, null, fieldNameList, row.length, row.length, start);
		result.setSegmentCount(segmentSize);
		result.setDeletedDocCount(deletedDocCount);
		result.setDocCount(totalSize);

		return result;
	}

	// 원문조회기능.
	public Result findDocument(String collectionId, String primaryKey) throws IRException, IOException, SettingException {
		if (collectionHandler.segmentSize() == 0) {
			logger.warn("Collection {} is not indexed!", collectionId);
		}

		Result result = new Result();

		Metadata meta = new Metadata();
		meta.setStart(1);

		int fieldSize = collectionHandler.schema().getFieldSize();
		String[] fieldNameList = new String[fieldSize];
		List<FieldSetting> fieldSettingList = collectionHandler.schema().schemaSetting().getFieldSettingList();
		int[] fieldNumList = new int[fieldSize];
		for (int i = 0; i < fieldNumList.length; i++) {
			fieldNumList[i] = i;
			fieldNameList[i] = fieldSettingList.get(i).getId();
		}

		List<FieldSetting> pkFieldSettingList = new ArrayList<FieldSetting>(3);

		for (RefSetting pkRefSetting : collectionHandler.schema().schemaSetting().getPrimaryKeySetting().getFieldList()) {
			pkFieldSettingList.add(collectionHandler.schema().getFieldSetting(pkRefSetting.getRef()));
		}
		// /////////////////////////////////////
		int totalSize = 0;
		int segmentSize = collectionHandler.segmentSize();
		int deletedDocCount = 0;
		// 이 배열의 index번호는 세그먼트번호.
		ArrayList<Row> rowList = new ArrayList<Row>();

		// SegmentInfo lastSegInfo = shardHandler.getSegmentInfo(segmentSize - 1);
		// File lastSegDir = lastSegInfo.getSegmentDir();
		// int lastSegRevision = lastSegInfo.getLastRevision();

		for (int i = 0; i < segmentSize; i++) {
			SegmentReader segmentReader = collectionHandler.segmentReader(i);
			// File targetDir = segmentReader.getSegmentDir();
			// int lastRevision = segmentReader.getLastRevision();

			String segmentId = segmentReader.segmentInfo().getId();
			int revision = segmentReader.segmentInfo().getRevision();
			// DocumentReader reader = new DocumentReader(shardHandler.schema(), segmentReader.segmentDir());
			BitSet deleteSet = null;
			deleteSet = new BitSet(segmentReader.revisionDir(), IndexFileNames.getSuffixFileName(IndexFileNames.docDeleteSet, segmentId));
			// if (i < segmentSize - 1) {
			// deleteSet = new BitSet(segmentReader.revisionDir(),
			// IndexFileNames.getSuffixFileName(IndexFileNames.docDeleteSet, Integer.toString(i)));
			// } else {
			// deleteSet = new BitSet(segmentReader.revisionDir(), IndexFileNames.docDeleteSet);
			// }
			logger.debug("DELETE-{} {} >> {}", new Object[] { i, deleteSet, deleteSet.getEntry() });

			BytesDataOutput pkOutput = new BytesDataOutput();

			String[] pkValues = null;

			if (pkFieldSettingList.size() > 1) {
				// 결합 pk일경우 값들은 ';'로 구분되어있다.
				pkValues = primaryKey.split(";");
			} else {
				pkValues = new String[] { primaryKey };
			}

			int docNo = -1;

			for (int j = 0; j < pkFieldSettingList.size(); j++) {
				FieldSetting fieldSetting = pkFieldSettingList.get(j);
				Field field = fieldSetting.createIndexableField(pkValues[j]);
				field.writeTo(pkOutput);
			}

			// FIXME segmentReader내부의 PrimaryKeyIndexReader를 사용해야한다.
			PrimaryKeyIndexReader pkReader = new PrimaryKeyIndexReader(segmentReader.revisionDir(), IndexFileNames.primaryKeyMap);
			docNo = pkReader.get(pkOutput.array(), 0, (int) pkOutput.position());
			pkReader.close();

			if (docNo != -1) {

				Document document = segmentReader.newDocumentReader().readDocument(docNo);
				Row row = new Row(fieldSize);
				row.setRowTag(i + "-" + revision + "-" + docNo);
				for (int m = 0; m < fieldSize; m++) {
					int fieldNum = fieldNumList[m];
					Field field = document.get(fieldNum);
					row.put(m, field.toString().toCharArray());
				}
				// 현재 삭제리스트는 이전 리비전 문서들의 번호도 함께 저장되어 있으므로, 이전 삭제리스트까지 확인해볼 필요는 없이 deleteSet 만 사용하도록 한다.
				if (deleteSet.isSet(docNo)) {
					row.setDeleted(true);
					deletedDocCount++;
				}
				rowList.add(row);
			}

		}

		Row[] row = new Row[rowList.size()];
		for (int i = 0; i < rowList.size(); i++) {
			row[i] = rowList.get(i);
		}

		// //////////////////////////////////////

		result = new Result(row, null, fieldNameList, row.length, row.length, 1);
		result.setSegmentCount(segmentSize);
		result.setDeletedDocCount(deletedDocCount);
		result.setDocCount(totalSize);

		return result;
	}

	// segEndNums는 세그먼트별로 총 문서갯수-1 이 들어있다.
	// 즉, 세그먼트-0에 5개 세그먼트-1에 5개가 들어있다면, {4,9}와 같이 들어온다.
	private int[][] matchSegment(int[] segEndNums, int start, int rows) {
		if (rows <= 0) {
			return new int[0][0];
		}

		// [][세그먼트번호,시작번호,끝번호]
		ArrayList<int[]> list = new ArrayList<int[]>();

		for (int i = 0; i < segEndNums.length; i++) {
			if (start <= segEndNums[i]) {
				int prevEnd = i == 0 ? -1 : segEndNums[i - 1];
				if (start + rows - 1 <= segEndNums[i]) {
					// start, end가 모두 한 세그먼트내에 들어오면 추가하고 바로 종료.
					int newStart = start - prevEnd - 1;
					list.add(new int[] { i, newStart, newStart + rows - 1 });
					// 끝.
					break;
				} else {
					// 나누어 질경우.
					list.add(new int[] { i, start - prevEnd - 1, segEndNums[i] - prevEnd - 1 });
					// 읽은 수만큼 빼준다.
					rows -= (segEndNums[i] - start + 1);
					// 다음세그먼트의 시작으로 변경한다.
					start = segEndNums[i] + 1;
					// System.out.println("new " + start + ", " + rows);
				}
			}
		}
		int[][] result = new int[list.size()][3];
		for (int i = 0; i < list.size(); i++) {
			int[] tmp = list.get(i);
			result[i][0] = tmp[0];
			result[i][1] = tmp[1];
			result[i][2] = tmp[2];
		}

		return result;
	}

}
