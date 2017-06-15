package org.fastcatsearch.job.indexing;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.DynamicIndexModule;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;

import java.io.IOException;

/**
 * Created by swsong on 2016. 1. 27..
 */
public class UpdateDynamicIndexingScheduleJob extends Job implements Streamable{

    private static final String FLAG_ON = "on";
    private static final String FLAG_OFF = "off";

    private String collectionId;
    private String flag;

    public UpdateDynamicIndexingScheduleJob() {
    }

    public UpdateDynamicIndexingScheduleJob(String collectionId, String flag) {
        this.collectionId = collectionId;
        this.flag = flag;
    }

    @Override
    public JobResult doRun() throws FastcatSearchException {
        IRService irService = ServiceManager.getInstance().getService(IRService.class);
        DynamicIndexModule dynamicIndexModule = irService.getDynamicIndexModule(collectionId);
        if (dynamicIndexModule != null) {

            if(FLAG_ON.equalsIgnoreCase(flag)) {
                dynamicIndexModule.startIndexingSchedule();
            } else if (FLAG_OFF.equalsIgnoreCase(flag)) {
                dynamicIndexModule.stopIndexingSchedule();
            }

            return new JobResult(dynamicIndexModule.isIndexingScheduled());
        } else {
            logger.error("Cannot find collection : " + collectionId);
        }
        return new JobResult(false);
    }

    @Override
    public void readFrom(DataInput input) throws IOException {
        collectionId = input.readString();
        flag = input.readString();
    }

    @Override
    public void writeTo(DataOutput output) throws IOException {
        output.writeString(collectionId);
        output.writeString(flag);
    }
}
