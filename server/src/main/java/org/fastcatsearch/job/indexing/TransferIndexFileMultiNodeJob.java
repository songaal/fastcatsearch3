package org.fastcatsearch.job.indexing;

import java.io.File;
import java.util.List;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeJobResult;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.task.IndexFileTransfer;
import org.fastcatsearch.transport.TransportException;

public class TransferIndexFileMultiNodeJob extends Job {

	private static final long serialVersionUID = 6571189743024849707L;

	private File file;
	private List<Node> nodeList;

	public TransferIndexFileMultiNodeJob(File file, List<Node> nodeList) {
		this.file = file;
		this.nodeList = nodeList;
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {
		int nodeSize = nodeList.size();

		NodeJobResult[] nodeJobResultList = new NodeJobResult[nodeSize];
		ResultFuture[] resultList = new ResultFuture[nodeSize];

		for (int i = 0; i < nodeList.size(); i++) {
			Node node = nodeList.get(i);
			TransferIndexFileJob transferJob = new TransferIndexFileJob(file, node);
			resultList[i] = JobService.getInstance().offer(transferJob);
		}

		for (int i = 0; i < nodeList.size(); i++) {
			boolean isSuccess = false;
			if(resultList[i] != null){
				Object obj = resultList[i].take();
				isSuccess = resultList[i].isSuccess() && obj instanceof Boolean && (Boolean) obj;
			}
			nodeJobResultList[i] = new NodeJobResult(nodeList.get(i), null, isSuccess);
		}

		return new JobResult(nodeJobResultList);
	}

	public static class TransferIndexFileJob extends Job {

		private static final long serialVersionUID = 7392952546461673256L;

		private File file;
		private Node node;

		public TransferIndexFileJob(File file, Node node) {
			this.file = file;
			this.node = node;
		}

		@Override
		public JobResult doRun() throws FastcatSearchException {

			NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);

			try {
				new IndexFileTransfer(environment).transferFile(file, nodeService, node);
			} catch (TransportException e) {
				logger.error("", e);
				return new JobResult(false);
			}

			return new JobResult(true);
		}
	}
}
