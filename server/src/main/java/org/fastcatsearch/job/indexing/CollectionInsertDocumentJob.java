/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.job.indexing;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.db.mapper.IndexingResultMapper.ResultStatus;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.http.action.service.indexing.JSONRequestReader;
import org.fastcatsearch.ir.DynamicIndexModule;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.state.TaskStateService;
import org.fastcatsearch.notification.NotificationService;
import org.fastcatsearch.processlogger.ProcessLoggerService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableThrowable;

import java.io.IOException;
import java.util.List;

/**
 *
 * JSON 문서를 각 노드에서 색인하고 적용한다.
 * */
public class CollectionInsertDocumentJob extends IndexingJob {

	private static final long serialVersionUID = 7898036370433248984L;

    private String documents;

    @Override
    public void prepare(IndexingType indexingType, String indexingStep) {
        collectionId = getStringArgs(0);
        documents = getStringArgs(1);
        this.indexingType = indexingType;
        this.indexingStep = indexingStep;
        ServiceManager serviceManager = ServiceManager.getInstance();
        processLoggerService = serviceManager.getService(ProcessLoggerService.class);
        notificationService = serviceManager.getService(NotificationService.class);
        taskStateService = serviceManager.getService(TaskStateService.class);
    }
    @Override
    public void readFrom(DataInput input) throws IOException {
        isScheduled = input.readBoolean();
        args = new String[] {input.readString(), input.readString()};
    }

    @Override
    public void writeTo(DataOutput output) throws IOException {
        output.writeBoolean(isScheduled);
        output.writeString(((String[]) args)[0]);
        output.writeString(((String[]) args)[1]);
    }
	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		prepare(IndexingType.ADD, "ALL");
		
		Throwable throwable = null;
		ResultStatus resultStatus = ResultStatus.RUNNING;
		Object result = null;
		long startTime = System.currentTimeMillis();
		try {
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
            List<String> jsonList = new JSONRequestReader().readJsonList(documents);

            DynamicIndexModule indexModule = irService.getDynamicIndexModule(collectionId);
            if(indexModule == null) {
                throw new FastcatSearchException("Collection [" + collectionId + "] is not exist.");
            }
            indexModule.insertDocument(jsonList);

			int duration = (int) (System.currentTimeMillis() - startTime);
			
			/*
			 * 캐시 클리어.
			 */
//			getJobExecutor().offer(new CacheServiceRestartJob());

			return new JobResult();
			
		} catch (Throwable e) {
			indexingLogger.error("[" + collectionId + "] Indexing", e);
			throwable = e;
			resultStatus = ResultStatus.FAIL;
			throw new FastcatSearchException("ERR-00501", throwable, collectionId); // 색인실패.
		} finally {
			Streamable streamableResult = null;
			if (throwable != null) {
				streamableResult = new StreamableThrowable(throwable);
			} else if (result instanceof Streamable) {
				streamableResult = (Streamable) result;
			}

			updateIndexingStatusFinish(resultStatus, streamableResult);
		}

	}

}
