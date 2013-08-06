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

package org.fastcatsearch.job;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.CollectionIndexer;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableThrowable;
import org.fastcatsearch.util.CollectionContextUtil;

public class FullIndexJob extends IndexingJob {

	private static final long serialVersionUID = 7898036370433248984L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		prepare(IndexingType.FULL_INDEXING);

		indexingLogger.info("[{}] Full Indexing Start!", collectionId);

		updateStatusStart();

		boolean isSuccess = false;
		Object result = null;

		Throwable throwable = null;

		try {
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			
			/*
			 * Do indexing!!
			 */
			//////////////////////////////////////////////////////////////////////////////////////////
			CollectionContext collectionContext = irService.collectionContext(collectionId).copy();
			CollectionIndexer collectionIndexer = new CollectionIndexer(collectionContext);
			SegmentInfo segmentInfo = collectionIndexer.fullIndexing();
			RevisionInfo revisionInfo = segmentInfo.getRevisionInfo();
			//////////////////////////////////////////////////////////////////////////////////////////
			
			
			//status를 바꾸고 context를 저장한다.
			collectionContext.updateCollectionStatus(IndexingType.FULL_INDEXING, revisionInfo, indexingStartTime(), System.currentTimeMillis());
			CollectionContextUtil.saveAfterIndexing(collectionContext);
			
			// apply schema setting
			CollectionHandler collectionHandler = irService.loadCollectionHandler(collectionContext);
			
			/*
			 * 컬렉션 리로드
			 */
			CollectionHandler oldCollectionHandler = irService.putCollectionHandler(collectionId, collectionHandler);
			if (oldCollectionHandler != null) {
				logger.info("## Close Previous Collection Handler");
				oldCollectionHandler.close();
			}
			DataInfo dataInfo = collectionHandler.collectionContext().dataInfo();
			indexingLogger.info(dataInfo.toString());

			int duration = (int) (System.currentTimeMillis() - indexingStartTime());

			/*
			 * 캐시 클리어.
			 */
			getJobExecutor().offer(new CacheServiceRestartJob());

			indexingLogger.info("[{}] Full Indexing Finished! {} time = {}", revisionInfo, duration);
			logger.info("== SegmentStatus ==");
			collectionHandler.printSegmentStatus();
			logger.info("===================");
			
			result = new IndexingJobResult(collectionId, revisionInfo, duration);
			isSuccess = true;

			return new JobResult(result);

		} catch (Throwable e) {
			indexingLogger.error("[" + collectionId + "] Indexing", e);
			throwable = e;
			throw new FastcatSearchException("ERR-00500", throwable, collectionId); // 전체색인실패.
		} finally {

			Streamable streamableResult = null;
			if (throwable != null) {
				streamableResult = new StreamableThrowable(throwable);
			} else if (result instanceof IndexingJobResult) {
				streamableResult = (IndexingJobResult) result;
			}

			updateStatusFinish(isSuccess, streamableResult);
		}

	}

}
