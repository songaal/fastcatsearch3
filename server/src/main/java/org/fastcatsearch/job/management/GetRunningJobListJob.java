package org.fastcatsearch.job.management;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;

public class GetRunningJobListJob extends Job {

	private static final long serialVersionUID = -9023882122708815679L;

	@Override
	public JobResult doRun() throws FastcatSearchException {

		JobService jobService = JobService.getInstance();
		List<JobInfo> jobInfoList = new ArrayList<GetRunningJobListJob.JobInfo>();
		for (Job job : jobService.getRunningJobs()) {
			jobInfoList.add(new JobInfo(job));
		}
		RunningJobListInfo result = new RunningJobListInfo(jobInfoList);

		return new JobResult(result);
	}

	public static class JobInfo {
		private long jobId;
		private String className;
		private String args;
		private boolean isScheduled;
		private boolean noResult;
		private long startTime;
		private long endTime;

		public JobInfo(long jobId, String className, String args,
				boolean isScheduled, boolean noResult, long startTime,
				long endTime) {
			this.jobId = jobId;
			this.className = className;
			this.args = args;
			this.isScheduled = isScheduled;
			this.noResult = noResult;
			this.startTime = startTime;
			this.endTime = endTime;
		}

		public JobInfo(Job job) {
			jobId = job.getId();
			className = job.getClass().getName();
			args = job.getArgs() != null ? job.getArgs().toString() : null;
			isScheduled = job.isScheduled();
			noResult = job.isNoResult();
			startTime = job.jobStartTime();
			endTime = job.jobEndTime();
		}

		public long getJobId() {
			return jobId;
		}

		public void setJobId(long jobId) {
			this.jobId = jobId;
		}

		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}

		public String getArgs() {
			return args;
		}

		public void setArgs(String args) {
			this.args = args;
		}

		public boolean isScheduled() {
			return isScheduled;
		}

		public void setScheduled(boolean isScheduled) {
			this.isScheduled = isScheduled;
		}

		public boolean isNoResult() {
			return noResult;
		}

		public void setNoResult(boolean noResult) {
			this.noResult = noResult;
		}

		public long getStartTime() {
			return startTime;
		}

		public void setStartTime(long startTime) {
			this.startTime = startTime;
		}

		public long getEndTime() {
			return endTime;
		}

		public void setEndTime(long endTime) {
			this.endTime = endTime;
		}

	}

	public static class RunningJobListInfo implements Streamable {

		private List<JobInfo> jobInfoList;

		public RunningJobListInfo() {
		}

		public RunningJobListInfo(List<JobInfo> jobInfoList) {
			this.jobInfoList = jobInfoList;
		}

		public List<JobInfo> getJobInfoList(){
			return jobInfoList;
		}
		
		@Override
		public void readFrom(DataInput input) throws IOException {
			int size = input.readVInt();
			jobInfoList = new ArrayList<GetRunningJobListJob.JobInfo>(size);
			for (int i = 0; i < size; i++) {
				jobInfoList.add(new JobInfo(input.readVLong(), input.readString(), input.readString(), input.readBoolean(), input.readBoolean(), input.readVLong(), input.readVLong()));
			}
		}

		@Override
		public void writeTo(DataOutput output) throws IOException {
			output.writeVInt(jobInfoList.size());
			for (JobInfo jobInfo : jobInfoList) {
				output.writeVLong(jobInfo.getJobId());
				output.writeString(jobInfo.getClassName());
				output.writeString(jobInfo.getArgs());
				output.writeBoolean(jobInfo.isScheduled);
				output.writeBoolean(jobInfo.isNoResult());
				output.writeVLong(jobInfo.getStartTime());
				output.writeVLong(jobInfo.getEndTime());
			}

		}

	}

}
