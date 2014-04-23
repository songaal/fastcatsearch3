package org.fastcatsearch.job.state;

import java.io.IOException;

import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class IndexingTaskState extends TaskState {

	public static final String STEP_INITIALIZE = "INITIALIZE";
	public static final String STEP_INDEXING = "INDEXING";
	public static final String STEP_FILECOPY = "COPY FILE TO REMOTE NODE";
	public static final String STEP_FINALIZE = "FINALIZE";
	public static final String STEP_END = "INDEXING END";
	
	public static final String STEP_DOCUMENT_STORE = "DOCUMENT STORE"; // 문서저장 색인작업.

	public IndexingTaskState() {
	}

	public IndexingTaskState(boolean isScheduled) {
		super(isScheduled);
	}
	
	private int documentCount;

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
		documentCount = input.readVInt();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		super.writeTo(output);
		output.writeVInt(documentCount);
	}
}
