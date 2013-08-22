package org.fastcatsearch.job.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.query.HighlightInfo;
import org.fastcatsearch.ir.query.View;
import org.fastcatsearch.ir.search.ShardHandler;
import org.fastcatsearch.ir.search.DocIdList;
import org.fastcatsearch.ir.search.DocumentResult;
import org.fastcatsearch.job.StreamableJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableDocumentResult;

/*
 * 문서의 모든 필드를 통째로 가져오는 job.
 *
 * */
public class InternalDocumentSearchJob extends StreamableJob {

	private static final long serialVersionUID = -5716557532305983540L;

	private String collectionId;
	private DocIdList docIdList;
	private List<View> views;
	private String[] tags;
	private HighlightInfo highlightInfo;

	public InternalDocumentSearchJob() {
	}

	public InternalDocumentSearchJob(String collectionId, DocIdList docIdList, List<View> views, String[] tags, HighlightInfo highlightInfo) {
		this.collectionId = collectionId;
		this.docIdList = docIdList;
		this.views = views;
		this.tags = tags;
		this.highlightInfo = highlightInfo;
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {

		try {
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			ShardHandler collectionHandler = irService.collectionHandler(collectionId);

			if (collectionHandler == null) {
				throw new FastcatSearchException("ERR-00520", collectionId);
			}

			DocumentResult documentResult = collectionHandler.searcher().searchDocument(docIdList, views, tags, highlightInfo);

			return new JobResult(new StreamableDocumentResult(documentResult));
		} catch (FastcatSearchException e) {
			throw e;
		} catch (Exception e) {
			// EventDBLogger.error(EventDBLogger.CATE_SEARCH, "검색에러..", EventDBLogger.getStackTrace(e));
			throw new FastcatSearchException("ERR-00550", e, collectionId);
		}

	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		collectionId = input.readString();

		// DocIdList
		int size = input.readVInt();
		docIdList = new DocIdList(size);
		for (int i = 0; i < size; i++) {
			docIdList.add(input.readVInt(), input.readVInt());
		}

		// List<View>
		int viewSize = input.readVInt();
		views = new ArrayList<View>(viewSize);
		for (int i = 0; i < viewSize; i++) {
			views.add(new View(input.readString(), input.readVInt(), input.readVInt(), input.readBoolean()));
		}
		
		// tags[]
		if (input.readBoolean()) {
			tags = new String[]{input.readString(), input.readString()}; 
		}
		
		// HighlightInfo
		if (input.readBoolean()) {
			highlightInfo = new HighlightInfo((Map<String, String>) input.readGenericValue(), (Map<String, String>) input.readGenericValue());
		}
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(collectionId);

		// DocIdList
		output.writeVInt(docIdList.size());
		for (int i = 0; i < docIdList.size(); i++) {
			output.writeVInt(docIdList.segmentSequence(i));
			output.writeVInt(docIdList.docNo(i));
		}

		// List<View>
		output.writeVInt(views.size());
		for (int i = 0; i < views.size(); i++) {
			View view = views.get(i);
			output.writeString(view.fieldId());
			output.writeVInt(view.snippetSize());
			output.writeVInt(view.fragmentSize());
			output.writeBoolean(view.isHighlighted());
		}

		// tags[]
		if (tags == null) {
			output.writeBoolean(false);
		} else {
			output.writeBoolean(true);
			output.writeString(tags[0]);
			output.writeString(tags[1]);
		}

		// HighlightInfo
		if (highlightInfo == null) {
			output.writeBoolean(false);
		} else {
			output.writeBoolean(true);
			output.writeGenericValue(highlightInfo.fieldAnalyzerMap());
			output.writeGenericValue(highlightInfo.fieldQueryMap());
		}
	}
}
