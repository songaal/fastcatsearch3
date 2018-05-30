package org.fastcatsearch.job.management.collections;

import org.apache.commons.io.FileUtils;
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

public class GetIndexingInfoJob extends Job implements Streamable {

    private String collectionId;

    public GetIndexingInfoJob() {
    }

    public GetIndexingInfoJob(String collectionId) {
        this.collectionId = collectionId;
    }


    @Override
    public void readFrom(DataInput input) throws IOException {

    }

    @Override
    public void writeTo(DataOutput output) throws IOException {

    }

    /**
     * @return
     * @throws FastcatSearchException
     */
    @Override
    public JobResult doRun() throws FastcatSearchException {

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

        CollectionIndexingInfo result = new CollectionIndexingInfo();
        result.setCollectionId(collectionId);
        result.setActive(isActive);
        result.setName(collectionConfig.getName());

        //TODO


//            .key("id").value(collectionId);

//                    .key("isActive").value(isActive)
//                    .key("name").value(collectionConfig.getName())
//                    .key("sequence").value(sequence)
//                    .key("revisionUUID").value(revisionUUID)
//                    .key("indexNode").value(collectionConfig.getIndexNode())
//                    .key("dataNodeList").value(join(collectionConfig.getDataNodeList()))
//                    .key("searchNodeList").value(join(collectionConfig.getSearchNodeList()));



        {//detail-info
            File indexFileDir = collectionContext.dataFilePaths().indexDirFile(sequence);
            int documentSize = collectionContext.dataInfo().getDocuments();
            int segmentSize = dataInfo.getSegmentSize();
            String diskSize = "";
            if(indexFileDir.exists()){
                long byteCount = FileUtils.sizeOfDirectory(indexFileDir);
                diskSize = FileUtils.byteCountToDisplaySize(byteCount);
            }
            String dataPath = new Path(collectionContext.collectionFilePaths().file()).relativise(indexFileDir).getPath();
            DataInfo.SegmentInfo lastSegmentInfo = collectionContext.dataInfo().getLatestSegmentInfo();
            String createTime = null;
            if(lastSegmentInfo != null) {
                createTime = Formatter.formatDate(new Date(lastSegmentInfo.getCreateTime()));
            } else {
                createTime = "";
            }

//            result.setDocumentSize(documentSize);
//                    .key("documentSize").value(documentSize)
//                    .key("segmentSize").value(segmentSize)
//                    .key("diskSize").value(diskSize)
//                    .key("dataPath").value(dataPath)
//                    .key("createTime").value(createTime);
//        }

            return new JobResult(result);

    }


}
