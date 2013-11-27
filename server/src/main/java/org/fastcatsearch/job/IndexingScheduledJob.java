//package org.fastcatsearch.job;
//
//import java.util.Date;
//
//import org.fastcatsearch.cluster.Node;
//import org.fastcatsearch.cluster.NodeService;
//import org.fastcatsearch.control.ResultFuture;
//import org.fastcatsearch.exception.FastcatSearchException;
//import org.fastcatsearch.ir.IRService;
//import org.fastcatsearch.ir.config.CollectionContext;
//import org.fastcatsearch.job.indexing.IndexingJob;
//import org.fastcatsearch.service.ServiceManager;
//
//public class IndexingScheduledJob extends ScheduledJob {
//
//	private static final long serialVersionUID = 1313927045484624930L;
//
//	public IndexingScheduledJob(IndexingJob job, Date startTime, int periodInSecond) {
//		super(job, startTime, periodInSecond);
//	}
//	
//	@Override
//	public JobResult doRun() throws FastcatSearchException {
//		if (isCanceled) {
//			return new JobResult();
//		}
//		try {
//			Thread.sleep(getTimeToWaitInMillisecond());
//		} catch (InterruptedException e) {
//			// if cancel method is called.
//			logger.info("[{}] {}({}) is canceled.", getClass().getSimpleName(), actualJob.getClass().getSimpleName(), actualJob.getArgs());
//			return new JobResult();
//		}
//
//		IndexingJob indexingJob = (IndexingJob) actualJob;
//		String collectionId = indexingJob.getStringArgs();
//		try {
//			
//			IRService irService = ServiceManager.getInstance().getService(IRService.class);
//			CollectionContext collectionContext = irService.collectionContext(collectionId);
//			String indexNodeId = collectionContext.collectionConfig().getIndexNode();
//			
//			NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
//			Node indexNode = nodeService.getNodeById(indexNodeId);
//			
//			//자신이 isIndexNode 이면 알아서 로컬에서 수행된다.
//			ResultFuture resultFuture = nodeService.sendRequest(indexNode, actualJob);
//			
//			Object result = null;
//			if (resultFuture == null) {
//				// ignore
//				logger.debug("Scheduled job {} is ignored.", actualJob);
//				
//				//알림메시지.
//				
//			} else {
//				result = resultFuture.take();
//				logger.debug("Schedule Job Result = {}", result);
//			}
//			return new JobResult();
//		} finally {
//			executeCount++;
//			lastExecuteTime = new Date();
//
//			if (!isCanceled) {
//				jobExecutor.offer(this);
//			}
//		}
//	}
//
//}
