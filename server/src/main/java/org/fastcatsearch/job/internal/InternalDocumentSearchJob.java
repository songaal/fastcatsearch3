package org.fastcatsearch.job.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.error.SearchAbortError;
import org.fastcatsearch.error.SearchError;
import org.fastcatsearch.error.ServerErrorCode;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.query.HighlightInfo;
import org.fastcatsearch.ir.query.View;
import org.fastcatsearch.ir.query.ViewContainer;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.CollectionSearcher;
import org.fastcatsearch.ir.search.DocIdList;
import org.fastcatsearch.ir.search.DocumentResult;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableDocumentResult;

/*
 * 요청된 문서의 특정필드만 포함된 문서리스트를 가져오는 job.
 *
 * */
public class InternalDocumentSearchJob extends Job implements Streamable {

	private static final long serialVersionUID = -5716557532305983540L;

	private String collectionId;
	private DocIdList docIdList;
	private ViewContainer views;
	private String[] tags;
	private HighlightInfo highlightInfo;

    private CollectionSearcher documentCollectionSearcher;

	public InternalDocumentSearchJob() {
	}

	public InternalDocumentSearchJob(String collectionId, DocIdList docIdList, ViewContainer views, String[] tags, HighlightInfo highlightInfo) {
		this.collectionId = collectionId;
		this.docIdList = docIdList;
		this.views = views;
		this.tags = tags;
		this.highlightInfo = highlightInfo;
	}

    protected void whenAborted() {
        if (documentCollectionSearcher != null) {
            documentCollectionSearcher.abort();
        }
    }

    @Override
	public JobResult doRun() throws FastcatSearchException {

        IRService irService = ServiceManager.getInstance().getService(IRService.class);
        CollectionHandler collectionHandler = irService.collectionHandler(collectionId);

        if (collectionHandler == null) {
            throw new SearchError(ServerErrorCode.COLLECTION_NOT_FOUND, collectionId);
        }

        DocumentResult documentResult = null;
        try {
            documentCollectionSearcher = collectionHandler.searcher();
            documentResult = documentCollectionSearcher.searchDocument(docIdList, views, tags, highlightInfo);
        } catch (SearchError e){
            throw e;
        } catch (SearchAbortError e){
            throw e;
        } catch (IOException e) {
            throw new FastcatSearchException(e);
        }

        return new JobResult(new StreamableDocumentResult(documentResult));

	}

	@Override
	public void readFrom(DataInput input) throws IOException {
        setTimeout(input.readLong(), input.readBoolean());
		collectionId = input.readString();

		// DocIdList
		int size = input.readVInt();
		docIdList = new DocIdList(size);
		for (int i = 0; i < size; i++) {
			String segmentId = input.readString();
			int docNo = input.readVInt();
			//하위 묶음문서
			int bundleSize = input.readVInt();
			if(bundleSize > 0) {
				DocIdList bundleDocIdList = new DocIdList(bundleSize);
				for (int j = 0; j < bundleSize; j++) {
					bundleDocIdList.add(input.readString(), input.readVInt());
				}
				docIdList.add(segmentId, docNo, bundleDocIdList);
			} else {
				docIdList.add(segmentId, docNo);
			}
		}

		// List<View>
		int viewSize = input.readVInt();
		//views = new ArrayList<View>(viewSize);
		views = new ViewContainer();
		for (int i = 0; i < viewSize; i++) {
			views.add(new View(input.readString(), input.readVInt(), input.readVInt()));
		}
		
		// tags[]
		if (input.readBoolean()) {
			tags = new String[]{input.readString(), input.readString()}; 
		}
		
		// HighlightInfo
		if (input.readBoolean()) {
			highlightInfo = new HighlightInfo((Map<String, String>) input.readGenericValue(), (Map<String, String>) input.readGenericValue(), (Map<String, String>) input.readGenericValue(), (Map<String, Integer>) input.readGenericValue());
		}
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
        output.writeLong(getTimeout());
        output.writeBoolean(isForceAbortWhenTimeout());
		output.writeString(collectionId);

		// DocIdList
		output.writeVInt(docIdList.size());
		for (int i = 0; i < docIdList.size(); i++) {
			output.writeString(docIdList.segmentId(i));
			output.writeVInt(docIdList.docNo(i));
			
			//하위 묶음문서
			DocIdList bundleDocIdList = docIdList.bundleDocIdList(i);
			if(bundleDocIdList == null) {
				output.writeVInt(0);
			} else {
				output.writeVInt(bundleDocIdList.size());
				for (int j = 0; j < bundleDocIdList.size(); j++) {
					output.writeString(bundleDocIdList.segmentId(j));
					output.writeVInt(bundleDocIdList.docNo(j));
				}
				
			}
		}

		// List<View>
		output.writeVInt(views.size());
		for (int i = 0; i < views.size(); i++) {
			View view = views.get(i);
			output.writeString(view.fieldId());
			output.writeVInt(view.snippetSize());
			output.writeVInt(view.fragmentSize());
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
			output.writeGenericValue(highlightInfo.fieldIndexAnalyzerMap());
			output.writeGenericValue(highlightInfo.fieldQueryAnalyzerMap());
			output.writeGenericValue(highlightInfo.fieldQueryTermMap());
			output.writeGenericValue(highlightInfo.fieldSearchOptionMap());
		}
	}
}
