package org.fastcatsearch.job.indexing;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.IndexingScheduleConfig;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.job.MasterNodeJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.util.JAXBConfigs;

import java.io.File;
import java.io.IOException;

/**
 * Created by swsong on 2016. 1. 27..
 */
public class UpdateIndexingScheduleJob extends MasterNodeJob implements Streamable{

    public static final String TYPE_FULL = "FULL";
    public static final String TYPE_ADD = "ADD";
    public static final String TYPE_DYNAMIC = "DYNAMIC";

    private static final String FLAG_ON = "ON";
    private static final String FLAG_OFF = "OFF";


    private String collectionId;
    private String type;
    private String flag;

    public UpdateIndexingScheduleJob() {
    }

    public UpdateIndexingScheduleJob(String collectionId, String type, String flag) {
        this.collectionId = collectionId;
        this.type = type;
        this.flag = flag;
    }

    @Override
    public JobResult doRun() throws FastcatSearchException {
        IRService irService = ServiceManager.getInstance().getService(IRService.class);
        CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
        if (collectionHandler != null) {
            CollectionContext collectionContext = irService.collectionContext(collectionId);
            IndexingScheduleConfig indexingScheduleConfig = collectionContext.indexingScheduleConfig();

            IndexingScheduleConfig.IndexingSchedule indexingSchedule = null;
            if (type.equalsIgnoreCase(TYPE_FULL)) {
                indexingSchedule = indexingScheduleConfig.getFullIndexingSchedule();
            } else if (type.equalsIgnoreCase(TYPE_ADD)) {
                indexingSchedule = indexingScheduleConfig.getAddIndexingSchedule();
            }

            boolean result = false;
            try {
                if (flag != null && (flag.equalsIgnoreCase(FLAG_ON) || flag.equalsIgnoreCase(FLAG_OFF))) {
                    boolean requestActive = flag.equalsIgnoreCase(FLAG_ON);

                    if (indexingSchedule.isActive() != requestActive) {
                        indexingSchedule.setActive(requestActive);
                        File scheduleConfigFile = collectionContext.collectionFilePaths().file(SettingFileNames.scheduleConfig);
                        JAXBConfigs.writeConfig(scheduleConfigFile, indexingScheduleConfig, IndexingScheduleConfig.class);
                        //해당 컬렉션의 스케쥴을 다시 로딩.
                        irService.reloadSchedule(collectionId);
                        result = requestActive;
                    }
                } else {
                    result = indexingSchedule.isActive();
                }
            } catch (Exception e) {

            }

            return new JobResult(result);
        } else {
            logger.error("Cannot find collection : " + collectionId);
        }
        return new JobResult(false);
    }

    @Override
    public void readFrom(DataInput input) throws IOException {
        collectionId = input.readString();
        type = input.readString();
        flag = input.readString();
    }

    @Override
    public void writeTo(DataOutput output) throws IOException {
        output.writeString(collectionId);
        output.writeString(type);
        output.writeString(flag);
    }
}
