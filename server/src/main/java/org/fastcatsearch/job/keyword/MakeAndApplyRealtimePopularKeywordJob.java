package org.fastcatsearch.job.keyword;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.job.MasterNodeJob;
import org.fastcatsearch.job.statistics.MakeRealtimePopularKeywordJob;

public class MakeAndApplyRealtimePopularKeywordJob extends MasterNodeJob {

	private static final long serialVersionUID = -3081201242740567689L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		boolean result = false;
		MakeRealtimePopularKeywordJob makeJob = new MakeRealtimePopularKeywordJob();
		ResultFuture resultFuture = JobService.getInstance().offer(makeJob);
		resultFuture.take();
		if(resultFuture.isSuccess()){
			ApplyRealtimePopularKeywordJob applyJob = new ApplyRealtimePopularKeywordJob();
			ResultFuture resultFuture2 = JobService.getInstance().offer(applyJob);
			resultFuture2.take();
			result = resultFuture2.isSuccess();
		}
		return new JobResult(result);
	}

}
