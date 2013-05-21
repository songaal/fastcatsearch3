package org.fastcatsearch.job.action;

import java.util.List;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.data.DataService;
import org.fastcatsearch.data.DataStrategy;
import org.fastcatsearch.db.dao.IndexingResult;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.job.IndexingJob;
import org.fastcatsearch.job.NodeFullIndexJob;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.notification.NotificationService;
import org.fastcatsearch.notification.message.IndexingFinishNotification;
import org.fastcatsearch.notification.message.IndexingStartNotification;
import org.fastcatsearch.processlogger.IndexingProcessLogger;
import org.fastcatsearch.processlogger.ProcessLoggerService;
import org.fastcatsearch.processlogger.log.IndexingFinishProcessLog;
import org.fastcatsearch.processlogger.log.IndexingStartProcessLog;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableThrowable;

public class FullIndexRequest extends IndexingJob {

	private static final long serialVersionUID = 5314187715835186514L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		ServiceManager serviceManager = null;
		ProcessLoggerService processLoggerService = null;
		NotificationService notificationService = null;
		boolean isSuccess = false;
		Object result = null;

		String collectionId = null;
		Throwable throwable = null;

		try {
			String[] args = getStringArrayArgs();
			collectionId = (String) args[0];

			serviceManager = ServiceManager.getInstance();
			processLoggerService = serviceManager.getService(ProcessLoggerService.class);
			notificationService = serviceManager.getService(NotificationService.class);

			processLoggerService.log(IndexingProcessLogger.class, new IndexingStartProcessLog(collectionId,
					IndexingResult.TYPE_FULL_INDEXING, startTime(), isScheduled()));
			notificationService.notify(new IndexingStartNotification(collectionId, IndexingResult.TYPE_FULL_INDEXING,
					startTime(), isScheduled()));

			DataService dataService = serviceManager.getService(DataService.class);
			DataStrategy dataStrategy = dataService.getCollectionDataStrategy(collectionId);
			List<Node> nodeList = dataStrategy.indexNodes();
			if (nodeList == null || nodeList.size() == 0) {
				throw new FastcatSearchException("색인할 노드가 정의되어있지 않습니다.");
			}

			//
			// TODO 어느 노드로 색인할지 고른다.
			// 현 버전에서는 일단 첫번째 노드로 색인.
			NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);

			// 선택된 노드로 색인 메시지를 전송한다.
			Node node = nodeList.get(0);
			NodeFullIndexJob job = new NodeFullIndexJob(collectionId);
			ResultFuture resultFuture = nodeService.sendRequest(node, job);
			result = resultFuture.take();
			isSuccess = resultFuture.isSuccess();

			if (!isSuccess) {
				if (result instanceof Throwable) {
					throw (Throwable) result;
				}
			}

			IndexingJobResult indexingJobResult = (IndexingJobResult) result;

			return new JobResult(indexingJobResult);

		} catch (Throwable e) {
			throwable = e;
			throw new FastcatSearchException(throwable); // 전체색인실패.

		} finally {
			long endTime = System.currentTimeMillis();
			Streamable streamableResult = null;
			if (throwable != null) {
				streamableResult = new StreamableThrowable(throwable);
			} else if (result instanceof IndexingJobResult) {
				streamableResult = (IndexingJobResult) result;
			}

			processLoggerService.log(IndexingProcessLogger.class, new IndexingFinishProcessLog(collectionId,
					IndexingResult.TYPE_FULL_INDEXING, isSuccess, startTime(), endTime, isScheduled(), streamableResult));

			notificationService.notify(new IndexingFinishNotification(collectionId, IndexingResult.TYPE_FULL_INDEXING, isSuccess,
					startTime(), endTime, streamableResult));
		}
	}
}
