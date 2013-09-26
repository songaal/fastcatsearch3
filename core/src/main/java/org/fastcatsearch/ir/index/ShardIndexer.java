package org.fastcatsearch.ir.index;

import java.io.File;
import java.io.IOException;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.config.ShardContext;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.util.IndexFilePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ShardIndexer {
	protected static final Logger logger = LoggerFactory.getLogger(ShardIndexer.class);

	protected ShardContext shardContext;

	private DeleteIdSet deleteIdSet;
	private IndexWriteInfoList indexWriteInfoList;
	
	private SegmentWriter segmentWriter;
	protected SegmentInfo segmentInfo;
	int count;
	long lapTime;
	long startTime;
	
	public ShardIndexer(ShardContext shardContext) throws IRException {
		this.shardContext = shardContext;
		
		prepare();
		
		segmentInfo = new SegmentInfo();
		indexWriteInfoList = new IndexWriteInfoList();

		IndexFilePaths indexFilePaths = shardContext.indexFilePaths();
		int dataSequence = shardContext.getIndexSequence();

		IndexConfig indexConfig = shardContext.indexConfig();
		
		logger.debug("WorkingSegmentInfo = {}", segmentInfo);
		String segmentId = segmentInfo.getId();
		RevisionInfo revisionInfo = segmentInfo.getRevisionInfo();

		File segmentDir = indexFilePaths.segmentFile(dataSequence, segmentId);
		logger.info("Segment Dir = {}", segmentDir.getAbsolutePath());
		Schema schema = shardContext.schema();
		
		try {
			segmentWriter = new SegmentWriter(schema, segmentDir, revisionInfo, indexConfig);
		} catch (IRException e) {
			logger.error("", e);
		}
		
		startTime = System.currentTimeMillis();
	}
	
	protected abstract void prepare() throws IRException;
	protected abstract void done() throws IRException;
	
	public void addDocument(Document document) throws IRException, IOException{
		segmentWriter.addDocument(document);
		count++;
		if (count % 10000 == 0) {
			logger.info(
					"{} documents indexed, lap = {} ms, elapsed = {}, mem = {}",
					new Object[] { count, System.currentTimeMillis() - lapTime,
							Formatter.getFormatTime(System.currentTimeMillis() - startTime),
							Formatter.getFormatSize(Runtime.getRuntime().totalMemory()) });
			lapTime = System.currentTimeMillis();
		}
	}
	
	public SegmentInfo close() throws IRException{
		
		done();
		
		RevisionInfo revisionInfo = segmentInfo.getRevisionInfo();
		if (segmentWriter != null) {
			try {
				segmentWriter.close();

				segmentWriter.getIndexWriteInfo(indexWriteInfoList);
				logger.debug("segmentWriter close {}", revisionInfo);
			} catch (IOException e) {
				throw new IRException(e);
			}
		}
		
		int insertCount = revisionInfo.getInsertCount();

		if (insertCount > 0) {
			shardContext.addSegmentInfo(segmentInfo);
		} else {
			logger.info("[{}] Indexing Canceled due to no documents.", shardContext.shardId());
		}

		return segmentInfo;
	}
	
	public DeleteIdSet deleteIdSet() {
		return deleteIdSet;
	}

	public IndexWriteInfoList indexWriteInfoList() {
		return indexWriteInfoList;
	}

}
