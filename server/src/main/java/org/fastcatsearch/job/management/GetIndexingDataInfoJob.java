package org.fastcatsearch.job.management;

import org.fastcatsearch.util.FileUtils;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.env.Path;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class GetIndexingDataInfoJob extends Job implements Streamable {

	private static final long serialVersionUID = -9023882122708815679L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		String collectionId = getStringArgs();
		
		IndexingDataInfo result = new IndexingDataInfo();
		
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		CollectionContext collectionContext = irService.collectionContext(collectionId);
		if(collectionContext == null){
			//컬렉션이 올바로 로딩되지 않았다면 빈 객체리턴.
			return new JobResult(result);
		}
		
		DataInfo dataInfo = collectionContext.dataInfo();

		result.segmentSize = dataInfo.getSegmentSize();

		int sequence = collectionContext.indexStatus().getSequence();
		result.sequence = sequence;
		File indexFileDir = collectionContext.dataFilePaths().indexDirFile(sequence);
		result.dataPath = new Path(collectionContext.collectionFilePaths().file()).relativise(indexFileDir).getPath();
		
        long byteCount = 0L;
        try {
            for(SegmentInfo info : dataInfo.getSegmentInfoList()) {
                File segmentDir = new File(indexFileDir, info.getId());
                if(segmentDir.exists()) {
                    byteCount += FileUtils.sizeOfDirectorySafe(segmentDir);
                }
            }
        }catch(Exception e) {
            //머징으로 인해 갑자기 사라질수 있으니 에러무시.
        }
		result.diskSize = FileUtils.byteCountToDisplaySize(byteCount);
		
		result.documentSize = collectionContext.dataInfo().getDocuments();
        result.deleteSize = collectionContext.dataInfo().getDeletes();

		SegmentInfo lastSegmentInfo = dataInfo.getLatestSegmentInfo();
		if(lastSegmentInfo != null) {
			result.createTime = Formatter.formatDate(new Date(lastSegmentInfo.getCreateTime()));
		} else {
			result.createTime = "";
		}
		
		
		return new JobResult(result);
	}
	
	

	@Override
	public void readFrom(DataInput input) throws IOException {
		args = input.readString();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(getStringArgs());
	}

	public static class IndexingDataInfo implements Streamable {
		public int segmentSize;
		public int sequence;
		public String dataPath;
		public String diskSize;
		public int documentSize;
        public int deleteSize;
		public String createTime;
		
		@Override
		public void readFrom(DataInput input) throws IOException {
			segmentSize = input.readInt();
			sequence = input.readInt();
			dataPath = input.readString();
			diskSize = input.readString();
			documentSize = input.readInt();
            deleteSize = input.readInt();
			createTime = input.readString();
		}

		@Override
		public void writeTo(DataOutput output) throws IOException {
			output.writeInt(segmentSize);
			output.writeInt(sequence);
			output.writeString(dataPath);
			output.writeString(diskSize);
			output.writeInt(documentSize);
            output.writeInt(deleteSize);
			output.writeString(createTime);
		}

	}

}
