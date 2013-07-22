package org.fastcatsearch.ir.search;

import java.io.IOException;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.document.DocumentReader;
import org.fastcatsearch.ir.filter.FilterException;
import org.fastcatsearch.ir.group.GroupData;
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
import org.fastcatsearch.ir.query.HitFilter;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.OperatedClause;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.query.Sorts;
import org.fastcatsearch.ir.settings.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	public SegmentSearcher(SegmentReader segmentReader) {
		this.segmentReader = segmentReader;
		this.schema = segmentReader.schema();
	}

	public Document getDocument(int docNo) throws IOException {
		if (documentReader == null) {
			documentReader = segmentReader.newDocumentReader();
		}
		return documentReader.readDocument(docNo);
	}

	public Hit search(Query query) throws ClauseException, IOException, IRException {
		search(query.getMeta(), query.getClause(), query.getFilters(), query.getGroups(), query.getGroupFilters(), query.getSorts());
		return new Hit(rankHitList(), makeGroupData(), totalCount);
	}

	public GroupHit doGrouping(Query query) throws ClauseException, IOException, IRException {
		search(query.getMeta(), query.getClause(), query.getFilters(), query.getGroups(), null, null);
		return new GroupHit(makeGroupData(), totalCount);
	}

	public void search(Metadata meta, Clause clause, Filters filters, Groups groups, Filters groupFilters, Sorts sorts) throws ClauseException,
			IOException, IRException {
		FieldIndexesReader fieldIndexesReader = segmentReader.newFieldIndexesReader();
		int sortMaxSize = meta.start() + meta.rows() - 1;

		int docCount = segmentReader.docCount();
		// Search
		// summary = new ArrayList<HighlightInfo>();
		// Clause clause = q.getClause();
		OperatedClause operatedClause = null;

		if (clause == null) {
			operatedClause = new AllDocumentOperatedClause(docCount);
		} else {
			operatedClause = clause.getOperatedClause(docCount, segmentReader.newSearchIndexesReader());
		}

		// filter
		// Filters filters = q.getFilters();
		if (filters != null) {
			try {
				hitFilter = filters.getHitFilter(schema.fieldSettingMap(), segmentReader.newFieldIndexesReader(), BULK_SIZE);
			} catch (FilterException e) {
				logger.error("패턴의 길이가 필드길이보다 커서 필터링을 수행할수 없습니다.", e);
			}
		}

		// group
		// Groups groups = q.getGroups();
		// Clause groupClause = null;
		// Filters groupFilters = null;
		// OperatedClause groupOperatedClause = null;

		if (groups != null) {
			groupGenerator = groups.getGroupDataGenerator(schema, segmentReader.newGroupIndexesReader(), fieldIndexesReader);
			// group clause
			// groupClause = q.getGroupClause();
			// if(groupClause != null){
			// groupOperatedClause = groupClause.getOperatedClause(docCount, searchFieldReader);
			// groupClauseRemain = groupOperatedClause.next(groupClauseDocInfo);
			// }
			// group filter
			// groupFilters = q.getGroupFilters();
			if (groupFilters != null) {
				groupHitFilter = groupFilters.getHitFilter(schema.fieldSettingMap(), fieldIndexesReader, BULK_SIZE);
			}
		}

		// sort
		// Sorts sorts = q.getSorts();
		SortGenerator sortGenerator = null;
		if (sorts == null || sorts == Sorts.DEFAULT_SORTS) {
			ranker = new DefaultRanker(sortMaxSize);
		} else {
			sortGenerator = sorts.getSortGenerator(schema, fieldIndexesReader);
			// ranker에 정렬 로직이 담겨있다.
			// ranker 안에는 필드타입과 정렬옵션을 확인하여 적합한 byte[] 비교를 수행한다.
			ranker = sorts.createRanker(schema, sortMaxSize);
		}

		RankInfo[] rankInfoList = new RankInfo[BULK_SIZE];
		boolean exausted = false;
		BitSet localDeleteSet = segmentReader.deleteSet();

		// int searchTime = 0, sortTime = 0, filterTime = 0;
		while (!exausted) {
			int nread = 0;

			// search
			for (nread = 0; nread < BULK_SIZE; nread++) {
				RankInfo rankInfo = new RankInfo();
				if (operatedClause.next(rankInfo)) {
					rankInfoList[nread] = rankInfo;
				} else {
					exausted = true;
					break;
				}
			}

			if (filters != null && hitFilter != null) {
				nread = hitFilter.filtering(rankInfoList, nread);
			}

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
					// logger.debug("ok docNo = "+rankInfo.docNo()+", global="+ (baseDocNo+rankInfo.docNo()));
				} else {
					// logger.debug("deleted docNo = "+rankInfo.docNo()+", global="+ (baseDocNo+rankInfo.docNo()));
				}

			}
			nread = count;

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
			totalCount += nread;
		}

		// logger.debug("#### time = "+searchTime+", "+filterTime+", "+sortTime + "("+totalCount+")");
	}

	/**
	 * Top K개의 HIT들을 리턴한다.
	 * 
	 * @return
	 */
	private FixedHitStack rankHitList() {
		int baseDocNo = segmentReader.baseDocNumber();
		int size = ranker.size();
		// logger.debug("size="+size);
		FixedHitStack hitStack = new FixedHitStack(size);
		for (int i = 0; i < size; i++) {
			HitElement el = ranker.pop();
			// local문서번호를 global문서번호로 바꿔준다.
			el.docNo(baseDocNo + el.docNo());
			hitStack.push(el);

		}
		return hitStack;
	}

	// 그룹결과는 문서를 next로 다읽은 경우에 완료된다.
	private GroupData makeGroupData() throws IOException {
		if (groupGenerator == null)
			return new GroupData(null, totalCount);

		return groupGenerator.generate();
	}

}
