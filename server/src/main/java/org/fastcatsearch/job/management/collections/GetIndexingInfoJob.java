package org.fastcatsearch.job.management.collections;

import org.fastcatsearch.util.FileUtils;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.env.Path;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionConfig;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.management.model.CollectionIndexingInfo;
import org.fastcatsearch.service.ServiceManager;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class GetIndexingInfoJob extends Job implements Streamable {
    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GetIndexingInfoJob.class);
    private String collectionId;

    public GetIndexingInfoJob() {}

    public GetIndexingInfoJob(String collectionId) {
        this.collectionId = collectionId;
    }

    @Override
    public void readFrom(DataInput input) throws IOException {
        collectionId = input.readString();
    }

    @Override
    public void writeTo(DataOutput output) throws IOException {
        output.writeString(collectionId);
    }

    /**
     * @return
     * @throws FastcatSearchException
     */
    @Override
    public JobResult doRun() throws FastcatSearchException {
        logger.info("collectionId: {}", collectionId);
        IRService irService = ServiceManager.getInstance().getService(IRService.class);

        CollectionContext collectionContext = irService.collectionContext(collectionId);
        if(collectionContext == null){
            return null;
        }
        CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
        boolean isActive = collectionHandler != null && collectionHandler.isLoaded();
        CollectionConfig collectionConfig = collectionContext.collectionConfig();
        DataInfo dataInfo = collectionContext.dataInfo();
        String revisionUUID = "";
        int sequence = collectionContext.indexStatus().getSequence();

        File indexFileDir = collectionContext.dataFilePaths().indexDirFile(sequence);
        int documentSize = collectionContext.dataInfo().getDocuments();
        int segmentSize = dataInfo.getSegmentSize();
        String diskSize = "";
        if (indexFileDir.exists()) {
            long byteCount = FileUtils.sizeOfDirectorySafe(indexFileDir);
            diskSize = FileUtils.byteCountToDisplaySize(byteCount);
        }
        String dataPath = new Path(collectionContext.collectionFilePaths().file()).relativise(indexFileDir).getPath();
        DataInfo.SegmentInfo lastSegmentInfo = collectionContext.dataInfo().getLatestSegmentInfo();
        String createTime = null;
        if (lastSegmentInfo != null) {
            createTime = Formatter.formatDate(new Date(lastSegmentInfo.getCreateTime()));
        } else {
            createTime = "";
        }

        CollectionIndexingInfo result = new CollectionIndexingInfo();
        result.setCollectionId(collectionId);
        result.setIsActive(isActive);
        result.setName(collectionConfig.getName());
        result.setRevisionUUID(revisionUUID);
        result.setSequence(sequence);

        result.setIndexNode(collectionConfig.getIndexNode());
        result.setDataNodeList(join(collectionConfig.getDataNodeList()));
        result.setSearchNodeList(join(collectionConfig.getSearchNodeList()));

        result.setDocumentSize(documentSize);
        result.setSegmentSize(segmentSize);
        result.setDiskSize(diskSize);
        result.setDataPath(dataPath);
        result.setCreateTime(createTime);

        return new JobResult(result);
    }

    public String join(List<String> list) {
        String joinString = "";
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                if(i > 0){
                    joinString += ", ";
                }
                joinString += list.get(i);
            }
        }

        return joinString;
    }

}
