package org.fastcatsearch.job.cluster;

import java.io.IOException;

import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.job.CacheServiceRestartJob;
import org.fastcatsearch.job.StreamableJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableCollectionContext;
import org.fastcatsearch.util.CollectionContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeCollectionReloadJob extends StreamableJob {
	private static final long serialVersionUID = 7222232821891387399L;
	private static Logger indexingLogger = LoggerFactory.getLogger("INDEXING_LOG");

	private CollectionContext collectionContext;

	public NodeCollectionReloadJob() {
	}

	public NodeCollectionReloadJob(CollectionContext collectionContext) {
		this.collectionContext = collectionContext;
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {

		try {
			CollectionContextUtil.saveCollectionAfterIndexing(collectionContext);
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			String collectionId = collectionContext.collectionId();
			CollectionHandler collectionHandler = irService.loadCollectionHandler(collectionContext);
			CollectionHandler oldCollectionHandler = irService.putCollectionHandler(collectionId, collectionHandler);
			if (oldCollectionHandler != null) {
				logger.info("## [{}] Close Previous Collection Handler", collectionId);
				oldCollectionHandler.close();
			}

			/*
			 * 캐시 클리어.
			 */
			getJobExecutor().offer(new CacheServiceRestartJob());
			return new JobResult(true);

		} catch (Exception e) {
			logger.error("", e);
			throw new FastcatSearchException("ERR-00525", e);
		}

	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		StreamableCollectionContext streamableCollectionContext = new StreamableCollectionContext(environment);
		streamableCollectionContext.readFrom(input);
		this.collectionContext = streamableCollectionContext.collectionContext();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		StreamableCollectionContext streamableCollectionContext = new StreamableCollectionContext(collectionContext);
		streamableCollectionContext.writeTo(output);
	}

}
