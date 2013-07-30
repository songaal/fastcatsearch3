package org.fastcatsearch.ir.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.fastcatsearch.ir.group.GroupsData;
import org.fastcatsearch.ir.group.GroupDataMerger;
import org.fastcatsearch.ir.group.GroupHit;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.io.BitSet;
import org.fastcatsearch.ir.io.BytesDataOutput;
import org.fastcatsearch.ir.io.FixedHitQueue;
import org.fastcatsearch.ir.io.FixedHitReader;
import org.fastcatsearch.ir.io.FixedMinHeap;
import org.fastcatsearch.ir.query.ClauseException;
import org.fastcatsearch.ir.query.Groups;
import org.fastcatsearch.ir.query.HighlightInfo;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.query.Row;
import org.fastcatsearch.ir.query.ShardSearchResult;
import org.fastcatsearch.ir.query.Sorts;
import org.fastcatsearch.ir.query.View;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.PkRefSetting;
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

		Metadata meta = q.getMeta();
		String collectionId = meta.collectionId();

		Groups groups = q.getGroups();

		if (groups == null) {
			new GroupResults(0);
		}

		if (segmentSize == 1) {
			// 머징필요없음.
			try {
				GroupHit groupHit = collectionHandler.segmentSearcher(0).doGrouping(q);
				// GroupHit groupHit = segmentSearcherList[0].doGrouping(q);
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
			// int searchTotalSize = 0;
			try {
				for (int i = 0; i < segmentSize; i++) {
					// GroupHit groupHit = segmentSearcherList[i].doGrouping(q);
					GroupHit groupHit = collectionHandler.segmentSearcher(i).doGrouping(q);
					// searchTotalSize += groupHit.searchTotalCount();

					if (dataMerger != null) {
						dataMerger.put(groupHit.groupData());
					}
				}
			} catch (IOException e) {
				throw new IRException(e);
			} catch (ClauseException e) {
				throw new IRException(e);
			}

			/*
			 * Group Result
			 */

			GroupsData groupData = null;
			if (dataMerger != null) {
				groupData = dataMerger.merge();
			}
			return groupData;
		}

	}

	public ShardSearchResult searchShard(Query q) throws IRException, IOException, SettingException {
		int segmentSize = collectionHandler.segmentSize();
		if (segmentSize == 0) {
			logger.warn("Collection {} is not indexed!", collectionHandler.collectionId());
		}

		Schema schema = collectionHandler.schema();
		// TODO shardId를 처리하도록 한다.
		int shardId = 0;

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
		int totalSize = 0;
		try {
			for (int i = 0; i < segmentSize; i++) {
				Hit hit = collectionHandler.segmentSearcher(i).search(q);
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
		// while(heap.size() > 0){
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
		return new ShardSearchResult(collectionId, shardId, hitElementList, totalHit.size(), totalSize, groupData);
	}

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
		if (collectionHandler.segmentSize() == 0) {
			logger.warn("Collection {} is not indexed!", collectionHandler.collectionId());
		}

		Result result = null;
		Metadata meta = q.getMeta();
		int start = meta.start();
		int rows = meta.rows();
		String collection = meta.collectionId();
		// Schema schema = IRSettings.getSchema(collection, false);

		Groups groups = q.getGroups();
		// int groupSize = (groups != null) ? q.getGroups().size() : 0;

		Sorts sorts = q.getSorts();
		FixedMinHeap<FixedHitReader> hitMerger = null;
		if (sorts != null) {
			hitMerger = sorts.createMerger(collectionHandler.schema(), collectionHandler.segmentSize());
		} else {
			hitMerger = new FixedMinHeap<FixedHitReader>(collectionHandler.segmentSize());
		}

		// HitMerger hitMerger = sorts.createMerger(schemaSetting, segmentSize);
		// FixedMinHeap<FixedHitReader> heap = new FixedMinHeap<FixedHitReader>(segmentSize);
		GroupDataMerger dataMerger = null;
		if (groups != null) {
			dataMerger = new GroupDataMerger(groups, collectionHandler.segmentSize());
		}
		// Set<HighlightInfo> totalSummarySet = new HashSet<HighlightInfo>();

		// 하이라이팅에 사용될 필드별 analyzer 들.
		HighlightInfo highlightInfo = null;

		int totalSize = 0;
		try {
			for (int i = 0; i < collectionHandler.segmentSize(); i++) {
				Hit hit = collectionHandler.segmentSearcher(i).search(q);
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

		GroupsData groupData = null;
		if (dataMerger != null) {
			groupData = dataMerger.merge();
		}

		// 각 세그먼트의 결과들을 rankdata를 기준으로 재정렬한다.
		FixedHitQueue totalHit = new FixedHitQueue(rows);
		int c = 1, n = 0;
		// while(heap.size() > 0){
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

		result = makeSearchResult(q, collectionHandler.schema(), totalHit, totalSize, highlightInfo);

		// TODO
		// group function에서 생성된 경과를 groupResultGenerator에서 char key 로 변경하기 때문에 type이 필요하게 되었다.
		// group function에서 즉시 char key로 생성하면 안될까? 정렬도 하고. 전부다. 그게 맞는거 같은데...
		// function에 sort, limit등의 옵션도 다 들어있으니 거기서 최종결과가 나와야한다. 아니면 최종 key값이라도 통일되어야..
		// 분산에서는 raw 리스트를 넘기므로 일단 스킵.
		// 제일 큰 문제는 정렬과 키값생성이 groupResultGenerator에서 수행되면 function plugin이 제 기능을 다하지 못한다....특히 datetime에 대해서...
		// 그리고 plugin에서 int 키 값을 String으로 바꾸었는데, groupResultGenerator에서는 int형으로 인식하므로 null 에러가 발생가능성도 있다.
		// 즉, group function에서 최종 키값을 생성필요.
		// 정렬은 entry가 numeric인지 string인지 확인하여 알아서 수행.
		// groupResultGenerator는 필요없게된다..

		/*
		 * Group Result
		 */
		if (groups != null) {
			GroupResults groupResults = groups.getGroupsResultGenerator().generate(groupData);
			result.setGroupResult(groupResults);
		}
		return result;
	}

	public Result makeSearchResult(Query q, Schema schema, FixedHitQueue totalHit, int totalSize, HighlightInfo highlightInfo) throws IOException {
		Metadata meta = q.getMeta();
		List<View> viewList = q.getViews();
		String[] fieldNameList = new String[viewList.size()];
		int[] fieldSummarySize = new int[schema.getFieldSize()];
		for (int i = 0; i < viewList.size(); i++) {
			View view = viewList.get(i);
			fieldNameList[i] = view.fieldId();
			int num = collectionHandler.schema().getFieldSequence(fieldNameList[i]);
			if (num >= 0) {
				fieldSummarySize[num] = view.summarySize();
				logger.trace("Summary size = {} : {}", num, fieldSummarySize[num]);
			}
		}

		FixedHitReader hitReader = totalHit.getReader();

		// logger.debug("==== ranking ====");

		int realSize = totalHit.size();
		Document[] eachDocList = new Document[realSize];
		int[] eachDocIds = new int[realSize];

		int idx = 0;
		while (hitReader.next()) {
			HitElement e = hitReader.read();

			logger.debug("FOUND {}:{}", e.docNo(), e.score());
			int docNo = e.docNo();
			float score = e.score();
			int segmentNumber = -1;

			// make doc number lists to send each columns
			for (int m = collectionHandler.segmentSize() - 1; m >= 0; m--) {
				// logger.debug("docNo="+docNo+" , segmentInfoList["+m+"].getBaseDocNo()="+segmentInfoList[m].getBaseDocNo());
				if (docNo >= collectionHandler.segmentReader(m).segmentInfo().getBaseNumber()) {
					segmentNumber = m;
					break;
				}
			}

			eachDocIds[idx] = docNo;
			Document doc = collectionHandler.segmentReader(segmentNumber).segmentSearcher().getDocument(docNo);
			doc.setScore(score);
			eachDocList[idx] = doc;

			idx++;
			// tags[idx++] = segmentNumber;
			// // logger.debug(segmentNumber+" / docNo:" + docNo);
			// eachDocIds[segmentNumber][cnt[segmentNumber]] = docNo;
			// eachDocScores[segmentNumber][cnt[segmentNumber]] = score;
			// cnt[segmentNumber]++;

		}

		// each segment's read position
		// int[] pos = new int[segmentSize];
		Row[] row = new Row[realSize];

		// logger.debug("=================");
		// logger.debug("realSize = "+realSize);

		List<View> views = q.getViews();
		Iterator<View> iter = views.iterator();
		int fieldSize = views.size();
		int[] fieldSequenceList = new int[fieldSize];

		// search 조건에 입력한 요약옵션(8)과 별도로 view에 셋팅한 요약길이를 확인하여 검색필드가 아니더라도 요약해주도록함.
		int[] extraSnipetSize = new int[fieldSize];
		int[] fragmentSize = new int[fieldSize];
		boolean[] extraUseHighlight = new boolean[fieldSize];

		int jj = 0;
		while (iter.hasNext()) {
			View v = iter.next();
			String fieldId = v.fieldId();
			int i = -1;

			if (fieldId.equalsIgnoreCase(ScoreField.fieldName)) {
				i = ScoreField.fieldNumber;
			} else if (fieldId.equalsIgnoreCase(DocNoField.fieldName)) {
				i = DocNoField.fieldNumber;
			} else {
				i = collectionHandler.schema().getFieldSequence(fieldId);
			}

			fieldSequenceList[jj] = i;

			if (v.summarySize() > 0) {
				extraSnipetSize[jj] = v.summarySize();
				fragmentSize[jj] = v.fragments();
			} else {
				extraSnipetSize[jj] = -1;
				fragmentSize[jj] = -1;
			}
			extraUseHighlight[jj] = v.highlight();

			jj++;
		}

		int size = schema.getFieldSize();

		for (int i = 0; i < realSize; i++) {
			Document document = eachDocList[i];
			row[i] = new Row(fieldSize);
			logger.debug("document#{}---------------", i);
			for (int j = 0; j < fieldSize; j++) {

				int fieldSequence = fieldSequenceList[j];
				if (fieldSequence == ScoreField.fieldNumber) {
					float score = document.getScore();
					row[i].put(j, Float.toString(score).toCharArray());
				} else if (fieldSequence == DocNoField.fieldNumber) {
					row[i].put(j, Integer.toString(eachDocIds[i]).toCharArray());
				} else if (fieldSequence == UnknownField.fieldNumber) {
					row[i].put(j, UnknownField.value().toCharArray());
				} else {
					Field field = document.get(fieldSequence);
					logger.debug("field#{} >> {}", j, field);
					String text = field.toString();

					if (has != null && text != null) {
						String analyzerId = highlightInfo.getAnalyzer(fieldNameList[j]);
						String queryString = highlightInfo.getQueryString(fieldNameList[j]);
						if (analyzerId != null && queryString != null) {
							AnalyzerPool analyzerPool = schema.getAnalyzerPool(analyzerId);
							if (analyzerPool != null) {
								Analyzer analyzer = analyzerPool.getFromPool();
								if (analyzer != null) {
									try {
										String[] tags = null;
										// TODO meta.option을 확인하여 Force Highlight면 모든 필드에 대해 Highlighting을 수행하도록 한다.
										if (extraUseHighlight[j]) {
											tags = meta.tags();
										}
										
										text = has.highlight(analyzer, text, queryString, tags, extraSnipetSize[j], fragmentSize[j]);
									} finally {
										analyzerPool.releaseToPool(analyzer);
									}
								}
							}
						}

					}

					if(text != null){
						row[i].put(j, text.toCharArray());
					}else{
						row[i].put(j, null);
					}

				}
			}
		}

		return new Result(row, fieldSize, fieldNameList, realSize, totalSize, meta);
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
		// File lastSegDir = lastSegInfo.getSegmentDir();
		// int lastSegRevision = lastSegInfo.getLastRevision();

		// 총문서갯수와 시작번호에 근거해서 이 시작번호가 어느 세그먼트에 속하고 이 세그먼트의 어느 위치에서 시작되는지를 구한다.
		// [세스머튼번호,시작번호,끝번호][2] = matchSegment(int[][], 조회시작변수);
		int[][] matchSeg = matchSegment(segEndNums, start, rows);
		// logger.debug("start: "+start+", rows "+rows+", result "+matchSeg.length);
		// targetsegInfo = this.getSegmentInfo(우에서 얻은 세그먼트번호);
		// 해당 세그먼트의 deleteSet객체를 얻는다.
		for (int i = 0; i < matchSeg.length; i++) {
			int segNo = matchSeg[i][0];
			int startNo = matchSeg[i][1];
			int endNo = matchSeg[i][2];
			// logger.debug("segNo: "+segNo+"startNo "+startNo+"endNo "+endNo);
			SegmentReader segmentReader = collectionHandler.segmentReader(segNo);
			// File targetDir = segmentReader.getSegmentDir();
			// int lastRevision = segmentReader.getLastRevision();
			int revision = segmentReader.segmentInfo().getRevision();
			DocumentReader reader = new DocumentReader(collectionHandler.schema(), segmentReader.segmentDir());
			BitSet deleteSet = null;
			// 마지막 세그먼트(현재세그먼트)이면 숫자 suffix없이 삭제문서파일을 읽는다.
			if (segNo == segmentSize - 1) {
				deleteSet = new BitSet(segmentReader.revisionDir(), IndexFileNames.docDeleteSet);
			} else {
				deleteSet = new BitSet(segmentReader.revisionDir(), IndexFileNames.getSuffixFileName(IndexFileNames.docDeleteSet,
						Integer.toString(segNo)));
			}

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

		result = new Result(row, fieldSize, fieldNameList, row.length, row.length, meta);
		result.setSegmentCount(segmentSize);
		result.setDeletedDocCount(deletedDocCount);
		result.setDocCount(totalSize);

		return result;
	}

	// 원문조회기능.
	public Result searchDocument(String collectionId, String primaryKey) throws IRException, IOException, SettingException {
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

		for (PkRefSetting pkRefSetting : collectionHandler.schema().schemaSetting().getPrimaryKeySetting().getFieldList()) {
			pkFieldSettingList.add(collectionHandler.schema().getFieldSetting(pkRefSetting.getRef()));
		}
		// /////////////////////////////////////
		int totalSize = 0;
		int segmentSize = collectionHandler.segmentSize();
		int deletedDocCount = 0;
		// 이 배열의 index번호는 세그먼트번호.
		ArrayList<Row> rowList = new ArrayList<Row>();

		// SegmentInfo lastSegInfo = collectionHandler.getSegmentInfo(segmentSize - 1);
		// File lastSegDir = lastSegInfo.getSegmentDir();
		// int lastSegRevision = lastSegInfo.getLastRevision();

		for (int i = 0; i < segmentSize; i++) {
			SegmentReader segmentReader = collectionHandler.segmentReader(i);
			// File targetDir = segmentReader.getSegmentDir();
			// int lastRevision = segmentReader.getLastRevision();
			int revision = segmentReader.segmentInfo().getRevision();
			DocumentReader reader = new DocumentReader(collectionHandler.schema(), segmentReader.segmentDir());
			BitSet deleteSet = null;

			if (i < segmentSize - 1) {
				deleteSet = new BitSet(segmentReader.revisionDir(),
						IndexFileNames.getSuffixFileName(IndexFileNames.docDeleteSet, Integer.toString(i)));
			} else {
				deleteSet = new BitSet(segmentReader.revisionDir(), IndexFileNames.docDeleteSet);
			}
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

			PrimaryKeyIndexReader pkReader = new PrimaryKeyIndexReader(segmentReader.revisionDir(), IndexFileNames.primaryKeyMap);
			docNo = pkReader.get(pkOutput.array(), 0, (int) pkOutput.position());
			pkReader.close();

			if (docNo != -1) {
				Document document = reader.readDocument(docNo);
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

		result = new Result(row, fieldSize, fieldNameList, row.length, row.length, meta);
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
