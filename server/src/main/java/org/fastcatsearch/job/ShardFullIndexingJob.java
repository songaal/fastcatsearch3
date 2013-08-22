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
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.ShardIndexer;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.config.ShardContext;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableThrowable;
import org.fastcatsearch.util.ShardContextUtil;
/**
 * 특정 shard만 전체색인을 수행한다.
 * schema가 변경될수도 있고, 그대로 일수 있으나
 * 이 job에서 schema를 적용하지는 않는다.
 * 상위의 CollectionFullIndexingJob에서 스키마 적용을 해야 모든 shard가 동일한 schema를 가질수 있다.
 * TODO 컬렉션 전체색인 job이 이 job을 호출할때 shardcontext에 미리 색인에 사용될 schema를 셋팅해놓으면 
 * shard는 그 schema만 보고 색인하도록한다. 즉, work schema를 모르고 그냥 색인만 하도록. 
 * */
public class ShardFullIndexingJob extends IndexingJob {

	private static final long serialVersionUID = 7898036370433248984L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		prepare(IndexingType.FULL);

		updateIndexingStatusStart();

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
			ShardContext shardContext = collectionContext.getShardContext(shardId);
			
			ShardIndexer shardIndexer = new ShardIndexer(shardContext);
			SegmentInfo segmentInfo = shardIndexer.fullIndexing();
			RevisionInfo revisionInfo = segmentInfo.getRevisionInfo();
			//////////////////////////////////////////////////////////////////////////////////////////
			if(revisionInfo.getInsertCount() == 0){
				int duration = (int) (System.currentTimeMillis() - indexingStartTime());
				result = new IndexingJobResult(collectionId, shardId, revisionInfo, duration, false);
				isSuccess = false;

				return new JobResult(result);
			}
			//status를 바꾸고 context를 저장한다.

			shardContext.updateIndexingStatus(IndexingType.FULL, revisionInfo, indexingStartTime(), System.currentTimeMillis());
			ShardContextUtil.saveAfterIndexing(shardContext);
			
			int duration = (int) (System.currentTimeMillis() - indexingStartTime());

			indexingLogger.info("[{} / {}] Shard Full Indexing Finished! {} time = {}", collectionId, shardId, revisionInfo, duration);
			
			result = new IndexingJobResult(collectionId, shardId, revisionInfo, duration);
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

			updateIndexingStatusFinish(isSuccess, streamableResult);
		}

	}

}
