package org.fastcatsearch.job.state;

import java.io.IOException;

import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class IndexingTaskState extends TaskState {

	public static final String STEP_INITIALIZE = "TASK INITIALIZE";
	public static final String STEP_END = "TASK END";
	
	public static final String STEP_INDEXING = "INDEXING";
	public static final String STEP_FILECOPY = "COPY FILE TO REMOTE NODE";
	public static final String STEP_RELOAD = "RELOAD INDEX";
	public static final String STEP_FINALIZE = "FINALIZE";
	
	public static final String STEP_DOCUMENT_STORE = "DOCUMENT STORE"; // 문서저장 색인작업.

	private IndexingType indexingType;
	private int documentCount;
	
	public IndexingTaskState() {
	}

	public IndexingTaskState(IndexingType indexingType, boolean isScheduled) {
		super(isScheduled);
		this.indexingType = indexingType;
	}
	
	public IndexingType getIndexingType() {
		return indexingType;
	}
	public int getDocumentCount() {
		return documentCount;
	}

	public void incrementDocumentCount() {
		documentCount++;
	}

	@Override
	public String getSummary() {
		return (isScheduled ? "scheduled" : "manually") + " indexing " + documentCount + (step != null ? " : " + step : "");
	}

	@Override
	public int getProgressRate() {
		return -1; // -1은 진행율 표시하지 않음.
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		super.readFrom(input);
		indexingType = IndexingType.valueOf(input.readString());
		documentCount = input.readVInt();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		super.writeTo(output);
		output.writeString(indexingType.name());
		output.writeVInt(documentCount);
	}
}
