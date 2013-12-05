package org.fastcatsearch.ir.search;

import java.io.IOException;
import java.util.HashMap;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.document.DocumentReader;
import org.fastcatsearch.ir.filter.FilterException;
import org.fastcatsearch.ir.group.GroupsData;
import org.fastcatsearch.ir.group.GroupDataGenerator;
import org.fastcatsearch.ir.group.GroupHit;
import org.fastcatsearch.ir.io.BitSet;
import org.fastcatsearch.ir.io.FixedHitStack;
import org.fastcatsearch.ir.io.FixedMaxPriorityQueue;
import org.fastcatsearch.ir.query.AllDocumentOperatedClause;
import org.fastcatsearch.ir.query.Clause;
import org.fastcatsearch.ir.query.ClauseException;
import org.fastcatsearch.ir.query.Filters;
import org.fastcatsearch.ir.query.Groups;
import org.fastcatsearch.ir.query.HighlightInfo;
import org.fastcatsearch.ir.query.HitFilter;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.OperatedClause;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.query.Sorts;
import org.fastcatsearch.ir.settings.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Single Thread로 동작한다.
 * 동시에 여러 thread에서 함께 사용할 수 없으며, 한번에 하나의 검색에만 사용.
 * */
public class SegmentSearcher {
	private static Logger logger = LoggerFactory.getLogger(SegmentSearcher.class);

	private final static int BULK_SIZE = 100;
	private FixedMaxPriorityQueue<HitElement> ranker;
	private GroupDataGenerator groupGenerator;
	private HitFilter hitFilter;
	private HitFilter groupHitFilter;
	private DocumentReader documentReader;
	private SegmentReader segmentReader;
	private Schema schema;

	private int totalCount;

	private HighlightInfo highlightInfo;

	public SegmentSearcher(SegmentReader segmentReader) {
		this.segmentReader = segmentReader;
		this.schema = segmentReader.schema();
		highlightInfo = new HighlightInfo();
	}

	public Document getDocument(int docNo) throws IOException {
		if (documentReader == null) {
			documentReader = segmentReader.newDocumentReader();
		}
		return documentReader.readDocument(docNo);
	}
	
	//true인 index의 필드값만 채워진다.
	public Document getDocument(int docNo, boolean[] fieldSelectOption) throws IOException {
		if (documentReader == null) {
			documentReader = segmentReader.newDocumentReader();
		}
		Document document = documentReader.readDocument(docNo, fieldSelectOption);
		document.setDocId(docNo + segmentReader.segmentInfo().getBaseNumber());
		return document;
	}

	public Hit searchHit(Query query) throws ClauseException, IOException, IRException {
		search(query.getMeta(), query.getClause(), query.getFilters(), query.getGroups(), query.getGroupFilters(), query.getSorts());
		return new Hit(rankHitList(), makeGroupData(), totalCount, highlightInfo);
	}

	public GroupHit searchGroupHit(Query query) throws ClauseException, IOException, IRException {
		search(query.getMeta(), query.getClause(), query.getFilters(), query.getGroups(), null, null);
		return new GroupHit(makeGroupData(), totalCount);
	}

	public void search(Metadata meta, Clause clause, Filters filters, Groups groups, Filters groupFilters, Sorts sorts) throws ClauseException,
			IOException, IRException {
		FieldIndexesReader fieldIndexesReader = null;
		int sortMaxSize = meta.start() + meta.rows() - 1;

		int docCount = segmentReader.docCount();
		// Search
		OperatedClause operatedClause = null;

		if (clause == null) {
			operatedClause = new AllDocumentOperatedClause(docCount);
		} else {
			operatedClause = clause.getOperatedClause(docCount, segmentReader.newSearchIndexesReader(), highlightInfo);
		}

		// filter
		if (filters != null) {
			//schema를 통해 field index setting을 알아야 필터링시 ignorecase등의 정보를 활용가능하다.
			hitFilter = filters.getHitFilter(schema, segmentReader.newFieldIndexesReader(), BULK_SIZE);
		}

		//group
		if (groups != null) {
			if(fieldIndexesReader == null){
				fieldIndexesReader = segmentReader.newFieldIndexesReader();
			}
			groupGenerator = groups.getGroupDataGenerator(schema, segmentReader.newGroupIndexesReader(), fieldIndexesReader);
			if (groupFilters != null) {
				groupHitFilter = groupFilters.getHitFilter(schema, fieldIndexesReader, BULK_SIZE);
			}
		}

		// sort
		// Sorts sorts = q.getSorts();
		SortGenerator sortGenerator = null;
		if (sorts == null || sorts == Sorts.DEFAULT_SORTS) {
			ranker = new DefaultRanker(sortMaxSize);
		} else {
			if(fieldIndexesReader == null){
				fieldIndexesReader = segmentReader.newFieldIndexesReader();
			}
			sortGenerator = sorts.getSortGenerator(schema, fieldIndexesReader);
			// ranker에 정렬 로직이 담겨있다.
			// ranker 안에는 필드타입과 정렬옵션을 확인하여 적합한 byte[] 비교를 수행한다.
			ranker = sorts.createRanker(schema, sortMaxSize);
		}

		RankInfo[] rankInfoList = new RankInfo[BULK_SIZE];
		boolean exausted = false;
		BitSet localDeleteSet = segmentReader.deleteSet();

//		int searchTime = 0, sortTime = 0, groupTime = 0, filterTime = 0;
		while (!exausted) {
			int nread = 0;
//			long st = System.nanoTime();
			// search
			for (nread = 0; nread < BULK_SIZE; nread++) {
				RankInfo rankInfo = new RankInfo();
				if (operatedClause.next(rankInfo)) {
					rankInfoList[nread] = rankInfo;
//					logger.debug("search rankInfo {} ", rankInfo);
				} else {
					exausted = true;
					break;
				}
			}
//			searchTime += (System.nanoTime() - st);st = System.nanoTime();
			if (filters != null && hitFilter != null) {
				nread = hitFilter.filtering(rankInfoList, nread);
			}
//			filterTime += (System.nanoTime() - st);st = System.nanoTime();
			if (!exausted && nread == 0) {
				continue;
			}

			// check delete documents
			int count = 0;
			for (int i = 0; i < nread; i++) {
				RankInfo rankInfo = rankInfoList[i];
				// Check deleted list
				if (!localDeleteSet.isSet(rankInfo.docNo())) {
					rankInfoList[count] = rankInfo;
					count++;
//					logger.debug("ok docNo = {}", rankInfo.docNo());
				} else {
					logger.debug("deleted docNo = {}", rankInfo.docNo());
				}

			}
//			logger.debug("check delete docs {} => {}", nread, count);
			nread = count;
//			st = System.nanoTime();
			// group
			if (groups != null) {
				groupGenerator.insert(rankInfoList, nread);
				// if(groupClause != null)
				// nread = applyGroupClause(groupOperatedClause, rankInfoList, nread);

				// group filter
				if (groupFilters != null) {
					nread = groupHitFilter.filtering(rankInfoList, nread);
				}
			}
//			groupTime += (System.nanoTime() - st);st = System.nanoTime();
			
			if (sorts == null || sorts == Sorts.DEFAULT_SORTS) {
				// if sort is not set, rankdata is null
				for (int i = 0; i < nread; i++) {
					ranker.push(new HitElement(rankInfoList[i].docNo(), rankInfoList[i].score()));
				}
			} else {
				// if sort set
				// sortGenerator 는 단순히 데이터를 읽어서 HitElement에 넣어주고 실제 정렬로직은 ranker에 push하면서 수행된다.
				HitElement[] e = sortGenerator.getHitElement(rankInfoList, nread);
				for (int i = 0; i < nread; i++) {
					ranker.push(e[i]);
				}
			}
//			sortTime += (System.nanoTime() - st);
			totalCount += nread;
		}

//		 logger.debug("#### time = se:{}ms, ft:{}ms, gr:{}ms, so:{}ms", searchTime / 1000000, filterTime / 1000000, groupTime / 1000000, sortTime / 1000000);
	}

	/**
	 * Top K개의 HIT들을 리턴한다.
	 * 
	 * @return
	 */
	private FixedHitStack rankHitList() {
		int segmentSequence = segmentReader.sequence();
		int size = ranker.size();
		FixedHitStack hitStack = new FixedHitStack(size);
		for (int i = 0; i < size; i++) {
			HitElement el = ranker.pop();
			
			//여기에서 segment번호를 셋팅해준다. 
			el.setDocNo(segmentSequence, el.docNo());
			
			logger.debug("rank hit seg#{} {} ", segmentSequence, el.docNo());
			hitStack.push(el);

		}
		return hitStack;
	}

	// 그룹결과는 문서를 next로 다읽은 경우에 완료된다.
	private GroupsData makeGroupData() throws IOException {
		if (groupGenerator == null)
			return new GroupsData(null, totalCount);

		return groupGenerator.generate();
	}

}
