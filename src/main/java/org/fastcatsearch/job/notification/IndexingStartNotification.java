package org.fastcatsearch.job.notification;

import java.io.IOException;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.control.JobException;
import org.fastcatsearch.job.StreamableJob;
import org.fastcatsearch.service.ServiceException;

public class IndexingStartNotification extends StreamableJob {

	public IndexingStartNotification(String collection, String indexingType, long startTime) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public JobResult doRun() throws JobException, ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void readFrom(StreamInput input) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeTo(StreamOutput output) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
