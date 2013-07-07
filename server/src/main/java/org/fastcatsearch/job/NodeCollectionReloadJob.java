package org.fastcatsearch.job;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.SegmentInfo;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.service.ServiceManager;

public class NodeCollectionReloadJob extends StreamableJob {
	long startTime;
	String collectionId;
	int dataSequence;
	int segmentNumber;
	
	public NodeCollectionReloadJob(){ }
	
	public NodeCollectionReloadJob(long startTime, String collectionId, int dataSequence, int segmentNumber){ 
		this.startTime = startTime;
		this.collectionId = collectionId;
		this.dataSequence = dataSequence;
		this.segmentNumber = segmentNumber;
	}
	
	@Override
	public JobResult doRun() throws FastcatSearchException {
		
//		File collectionHome = new File(IRSettings.getCollectionHome(collectionId));
		try{
			long st = System.currentTimeMillis();
//			Schema schema = IRSettings.getSchema(collectionId, true);
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			Schema schema = irService.collectionContext(collectionId).schema();
//			CollectionHandler newHandler = new CollectionHandler(collectionId, collectionHome, schema, IRSettings.getIndexConfig());
			CollectionHandler newHandler = irService.loadCollectionHandler(collectionId, -1);
			//이미 수정된 모든 파일이 복사되었기 때문에 collection.info, delete.set을 수정하는 addSegment를 수행핦 필요없음.
			//수행하면 세그먼트가 더 늘어나서, 오히려 에러발생.
//			int[] updateAndDeleteSize = newHandler.addSegment(segmentNumber, null);
	//		updateAndDeleteSize[1] += writer.getDuplicateDocCount();//중복문서 삭제카운트
	//		logger.info("== SegmentStatus ==");
//			newHandler.printSegmentStatus();
			
			newHandler.saveDataSequenceFile();
			
			CollectionHandler oldCollectionHandler = irService.putCollectionHandler(collectionId, newHandler);
			if(oldCollectionHandler != null){
				logger.info("## Close Previous Collection Handler");
				oldCollectionHandler.close();
			}
			
			SegmentInfo si = newHandler.getLastSegmentInfo();
			logger.info(si.toString());
			int docSize = si.getDocCount();
			int newDataSequence = newHandler.getDataSequence();
			/*
			 * indextime 파일 업데이트.
			 */
			CollectionContext collectionContext = irService.collectionContext(collectionId);
			collectionContext.updateCollectionStatus(IndexingType.FULL_INDEXING, newDataSequence, count.intValue(), st , System.currentTimeMillis());
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String startDt = sdf.format(startTime);
			String endDt = sdf.format(new Date());
			int duration = (int) (System.currentTimeMillis() - startTime);
			String durationStr = Formatter.getFormatTime(duration);
//			IRSettings.storeIndextime(collectionId, "FULL", startDt, endDt, durationStr, docSize);
			
			/*
			 * 5초후에 캐시 클리어.
			 */
			getJobExecutor().offer(new CacheServiceRestartJob(5000));
			return new JobResult(true);
			
		}catch(Exception e){
			logger.error("", e);
			throw new FastcatSearchException("ERR-00525", e);
		}
		
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		startTime = input.readLong();
		collectionId = input.readString();
		dataSequence = input.readInt();
		segmentNumber = input.readInt();
		logger.debug("## readFrom {}, {}, {}, {}", new Object[]{startTime, collectionId, dataSequence, segmentNumber});
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		logger.debug("## writeTo {}, {}, {}, {}", new Object[]{startTime, collectionId, dataSequence, segmentNumber});
		output.writeLong(startTime);
		output.writeString(collectionId);
		output.writeInt(dataSequence);
		output.writeInt(segmentNumber);
	}

}
