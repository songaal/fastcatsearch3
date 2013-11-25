//package org.fastcatsearch.ir.index;
//
//import java.io.File;
//import java.io.IOException;
//
//import org.fastcatsearch.ir.common.IRException;
//import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
//import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
//import org.fastcatsearch.ir.config.IndexConfig;
//import org.fastcatsearch.ir.config.ShardContext;
//import org.fastcatsearch.ir.document.Document;
//import org.fastcatsearch.ir.settings.Schema;
//import org.fastcatsearch.ir.util.Formatter;
//import org.fastcatsearch.util.FilePaths;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public abstract class ShardIndexer {
//	protected static final Logger logger = LoggerFactory.getLogger(ShardIndexer.class);
//	
//	protected ShardContext shardContext;
//	protected DeleteIdSet deleteIdSet; //삭제문서리스트. 외부에서 source reader를 통해 셋팅된다.
//	
//	private IndexWriteInfoList indexWriteInfoList;
//	
//	private SegmentWriter segmentWriter;
//	protected SegmentInfo workingSegmentInfo;
//	protected int count;
//	protected long lapTime;
//	protected long startTime;
//	
//	public ShardIndexer(ShardContext shardContext) throws IRException {
//		this.shardContext = shardContext;
//	}
//	
//	public void init(Schema schema) throws IRException {
//		
//		prepare();
//		
//		FilePaths indexFilePaths = shardContext.filePaths();
//		int dataSequence = shardContext.getIndexSequence();
//
//		IndexConfig indexConfig = shardContext.indexConfig();
//		
//		logger.debug("WorkingSegmentInfo = {}", workingSegmentInfo);
//		String segmentId = workingSegmentInfo.getId();
//		RevisionInfo revisionInfo = workingSegmentInfo.getRevisionInfo();
//
//		File segmentDir = indexFilePaths.segmentFile(dataSequence, segmentId);
//		logger.info("Segment Dir = {}", segmentDir.getAbsolutePath());
//		
//		segmentWriter = new SegmentWriter(schema, segmentDir, revisionInfo, indexConfig);
//		
//		indexWriteInfoList = new IndexWriteInfoList();
//		
//		startTime = System.currentTimeMillis();
//	}
//	
//	protected abstract void prepare() throws IRException;
//	protected abstract void done() throws IRException;
//	
//	public void setDeleteIdSet(DeleteIdSet deleteIdSet){
//		this.deleteIdSet = deleteIdSet;
//	}
//	
//	public ShardContext shardContext(){
//		return shardContext;
//	}
//	
//	public void addDocument(Document document) throws IRException, IOException{
//		segmentWriter.addDocument(document);
//		count++;
//		if (count % 10000 == 0) {
//			logger.info(
//					"{} documents indexed, lap = {} ms, elapsed = {}, mem = {}",
//					new Object[] { count, System.currentTimeMillis() - lapTime,
//							Formatter.getFormatTime(System.currentTimeMillis() - startTime),
//							Formatter.getFormatSize(Runtime.getRuntime().totalMemory()) });
//			lapTime = System.currentTimeMillis();
//		}
//	}
//	
//	public RevisionInfo close() throws IRException{
//		
//		RevisionInfo revisionInfo = workingSegmentInfo.getRevisionInfo();
//		if (segmentWriter != null) {
//			try {
//				segmentWriter.close();
//
//				segmentWriter.getIndexWriteInfo(indexWriteInfoList);
//				logger.debug("segmentWriter close {}", revisionInfo);
//			} catch (IOException e) {
//				throw new IRException(e);
//			}
//		}
//
//		done();
//		
//		int insertCount = revisionInfo.getInsertCount();
//
//		if (insertCount == 0) {
//			logger.info("[{}] Indexing Canceled due to no documents.", shardContext.shardId());
//		}
//
//		return revisionInfo;
//	}
//	
//	public IndexWriteInfoList indexWriteInfoList() {
//		return indexWriteInfoList;
//	}
//
//}
