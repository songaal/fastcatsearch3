package org.fastcatsearch.ir;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.fastcatsearch.datasource.reader.DataSourceReader;
import org.fastcatsearch.ir.analysis.AnalyzerPoolManager;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionIndexStatus.IndexStatus;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.fastcatsearch.ir.index.IndexWritable;
import org.fastcatsearch.ir.index.IndexWriteInfoList;
import org.fastcatsearch.ir.index.SegmentWriter;
import org.fastcatsearch.ir.index.SelectedIndexList;
import org.fastcatsearch.ir.index.WriteInfoLoggable;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.job.indexing.IndexingStopException;
import org.fastcatsearch.job.state.IndexingTaskState;
import org.fastcatsearch.util.CollectionContextUtil;
import org.fastcatsearch.util.FilePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCollectionIndexer2 implements CollectionIndexerable {
	protected static final Logger logger = LoggerFactory.getLogger(CollectionFullIndexer.class);
	protected CollectionContext collectionContext;
	protected AnalyzerPoolManager analyzerPoolManager;
	
	protected DataSourceReader dataSourceReader;
	protected long startTime;
	protected IndexingTaskState indexingTaskState;
	
	protected DeleteIdSet deleteIdSet; //삭제문서리스트. 외부에서 source reader를 통해 셋팅된다.
	
	protected IndexWriteInfoList indexWriteInfoList;
	
	protected List<IndexWritable> indexWriterList;
	protected SegmentInfo workingSegmentInfo;
	protected AtomicInteger count;
	protected long lapTime;
	
	protected boolean stopRequested;
	protected SelectedIndexList selectedIndexList;// 색인필드 선택사항.
	
	public AbstractCollectionIndexer2(CollectionContext collectionContext, AnalyzerPoolManager analyzerPoolManager) {
		this(collectionContext, analyzerPoolManager, null);
	}
	public AbstractCollectionIndexer2(CollectionContext collectionContext, AnalyzerPoolManager analyzerPoolManager, SelectedIndexList selectedIndexList) {
		this.collectionContext = collectionContext;
		this.analyzerPoolManager = analyzerPoolManager;
		this.selectedIndexList = selectedIndexList;
	}
	
	protected abstract DataSourceReader createDataSourceReader(File filePath, SchemaSetting schemaSetting) throws IRException;
	protected abstract void prepare() throws IRException;
	protected abstract boolean done(RevisionInfo revisionInfo, IndexStatus indexStatus) throws IRException, IndexingStopException;
	protected IndexWritable createIndexWriter(Schema schema, File segmentDir, RevisionInfo revisionInfo, IndexConfig indexConfig) throws IRException {
		return new SegmentWriter(schema, segmentDir, revisionInfo, indexConfig, analyzerPoolManager, selectedIndexList);
	}
	
	public void init(Schema schema) throws IRException {

		prepare();
		
		FilePaths dataFilePaths = collectionContext.collectionFilePaths().dataPaths();
		int dataSequence = collectionContext.getIndexSequence();

		IndexConfig indexConfig = collectionContext.indexConfig();
		
		logger.debug("WorkingSegmentInfo = {}", workingSegmentInfo);
		String segmentId = workingSegmentInfo.getId();
		RevisionInfo revisionInfo = workingSegmentInfo.getRevisionInfo();

		
		File filePath = collectionContext.collectionFilePaths().file();
		dataSourceReader = createDataSourceReader(filePath, schema.schemaSetting());
		
		//가용한 스레드 갯수만큼 indexwriter 를 생성한다.
		indexWriterList = new ArrayList<IndexWritable>();
		
		for(int inx=0;inx<3;inx++) {
			File segmentDir = dataFilePaths.segmentFile(dataSequence, segmentId);
			segmentDir = new File(segmentDir, "_test"+inx);
			logger.info("Segment Dir = {}", segmentDir.getAbsolutePath());
			IndexWritable indexWriter = createIndexWriter(schema, segmentDir, revisionInfo, indexConfig);
			indexWriterList.add(indexWriter);
		}
		
		indexWriteInfoList = new IndexWriteInfoList();
		
		startTime = System.currentTimeMillis();
		
		count = new AtomicInteger(0);
	}

	public void addDocument(int threadInx, Document document) throws IRException, IOException{
		indexWriterList.get(threadInx).addDocument(document);
		if (count.incrementAndGet() % 10000 == 0) {
			logger.info(
					"{} documents indexed, lap = {} ms, elapsed = {}, mem = {}",
					count, System.currentTimeMillis() - lapTime,
							Formatter.getFormatTime(System.currentTimeMillis() - startTime),
							Formatter.getFormatSize(Runtime.getRuntime().totalMemory()));
			lapTime = System.currentTimeMillis();
		}
		if(indexingTaskState != null){
			indexingTaskState.incrementDocumentCount();
		}
	}
	@Override
	public void requestStop(){
		logger.info("Collection [{}] Indexer Stop Requested! ", collectionContext.collectionId());
		
		stopRequested = true;
	}
	
	//색인취소(0건)이면 false;
	@Override
	public boolean close() throws IRException, SettingException, IndexingStopException {
		
		RevisionInfo revisionInfo = workingSegmentInfo.getRevisionInfo();
		if (indexWriterList != null) {
			for(int inx=0;inx<indexWriterList.size();inx++) {
				try {
					IndexWritable indexWriter = indexWriterList.get(inx);
					indexWriter.close();
					if(indexWriter instanceof WriteInfoLoggable)
					((WriteInfoLoggable) indexWriter).getIndexWriteInfo(indexWriteInfoList);
				} catch (IOException e) {
					throw new IRException(e);
				}
			}
		}

		dataSourceReader.close();
		
		logger.debug("##Indexer close {}", revisionInfo);
		deleteIdSet = dataSourceReader.getDeleteList();
		int deleteCount = 0;
		if(deleteIdSet != null) {
			deleteCount = deleteIdSet.size();
		}
		
		revisionInfo.setDeleteCount(deleteCount);
		
		long endTime = System.currentTimeMillis();
		
		IndexStatus indexStatus = new IndexStatus(revisionInfo.getDocumentCount(), revisionInfo.getInsertCount(), revisionInfo.getUpdateCount(), deleteCount,
				Formatter.formatDate(new Date(startTime)), Formatter.formatDate(new Date(endTime)), Formatter.getFormatTime(endTime - startTime));
		
		if(done(revisionInfo, indexStatus)){
			CollectionContextUtil.saveCollectionAfterIndexing(collectionContext);
		}else{
			//저장하지 않음.
		}
		return true;
	}
	
	public IndexWriteInfoList indexWriteInfoList() {
		return indexWriteInfoList;
	}
	@Override
	public void doIndexing() throws IRException, IOException {
		
		indexingTaskState.setStep(IndexingTaskState.STEP_INDEXING);
		
		final LinkedBlockingQueue<Document> documentQueue = new LinkedBlockingQueue<Document>();
		final boolean[] finished = {false};
		
		lapTime = System.currentTimeMillis();
		
		Thread producer = new Thread() {
			public void run() {
				try {
					while(dataSourceReader.hasNext()) {
						if(stopRequested) {
							break;
						}
						
						if(documentQueue.size() >= 5000) {
							Thread.sleep(100);
							continue;
						}
						
						documentQueue.add(dataSourceReader.nextDocument());
					}
				} catch (InterruptedException e) {
					logger.error("", e);
					throw new RuntimeException(e);
				} catch (IRException e) {
					logger.error("", e);
					throw new RuntimeException(e);
				}
				finished[0] = true;
			}
		};
		producer.start();
		
		final List<Thread> consumerList = new ArrayList<Thread>();
		
		for (int inx = 0; inx < indexWriterList.size(); inx++) {
			final int consumerInx = inx;
			Thread consumer = new Thread() {
				public void run() {
					try {
						Document document = null;
						
						while(true) {
							if(finished[0] && documentQueue.size() == 0) {
								break;
							}
							document = documentQueue.poll(100, TimeUnit.MILLISECONDS);
							
							if(document == null) {
								continue;
							} else {
								addDocument(consumerInx, document);
							}
						}
					} catch (Exception e) {
						logger.error("", e);
						throw new RuntimeException(e);
					}
				}
			};
			consumerList.add(consumer);
			consumer.start();
		}
		
		while(consumerList.size() > 0) {
			for(int inx=0;inx<consumerList.size();inx++) {
				if(!consumerList.get(inx).isAlive()) {
					consumerList.remove(inx);
					inx--;
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException ignore) {
			}
		}
	}
	
	public DeleteIdSet deleteIdSet() {
		return deleteIdSet;
	}
	
	public void setTaskState(IndexingTaskState indexingTaskState) {
		this.indexingTaskState = indexingTaskState;
	}
}