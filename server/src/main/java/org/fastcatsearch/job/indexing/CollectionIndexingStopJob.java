package org.fastcatsearch.job.indexing;

import java.io.IOException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;

/**
 * 색인노드에서 실행되는 색인정지 작업.
 * */
public class CollectionIndexingStopJob extends Job implements Streamable {

	private static final long serialVersionUID = -9020411832250747477L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		String collectionId = getStringArgs();
		boolean isRequested = false;
		//전체색인.
		IndexingJob indexingJob = (IndexingJob) JobService.getInstance().findRunningJob(IndexingJob.class, collectionId);
		if(indexingJob != null){
			indexingJob.requestStop();
			isRequested = true;
			logger.debug("IndexingJob Stop Requested! {} {}", indexingJob.getClass().getName(), indexingJob.getArgs());
		}else{
			logger.debug("Stopping IndexingJob Not Found! {}", collectionId);
		}
		
		return new JobResult(isRequested);
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		args = input.readString();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(getStringArgs());
	}

}
