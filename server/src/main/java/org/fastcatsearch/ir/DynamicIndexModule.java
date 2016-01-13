package org.fastcatsearch.ir;

import org.fastcatsearch.env.Environment;
import org.fastcatsearch.module.AbstractModule;
import org.fastcatsearch.module.ModuleException;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.util.LimitTimeSizeLogger;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by swsong on 2016. 1. 13..
 */
public class DynamicIndexModule extends AbstractModule {
    private String collectionId;

    private LimitTimeSizeLogger dataLogger;
    private Timer timer;
    private int bulkSize;
    private File dir;
    private int flushPeriod = 2;
    public DynamicIndexModule(Environment environment, Settings settings, String collectionId, int bulkSize) {
        super(environment, settings);
        this.collectionId = collectionId;
        this.bulkSize = bulkSize;
        dir = environment.filePaths().getCollectionsRoot().collectionFilePaths(collectionId).file("indexlog");
    }

    class IndexFireTask extends TimerTask {

        @Override
        public void run() {
            File file = dataLogger.pollFile();
            //TODO file 을 증분색인하도록 요청한다.
            logger.debug("Found file to be indexed > {}", file.getAbsolutePath());

        }

    }

    @Override
    protected boolean doLoad() throws ModuleException {
        timer = new Timer();
        timer.schedule(new IndexFireTask(), 1000, 1000);
        dataLogger = new LimitTimeSizeLogger(dir, bulkSize, flushPeriod);
        return true;
    }

    @Override
    protected boolean doUnload() throws ModuleException {
        timer.cancel();
        return true;
    }

    public boolean insertDocument(List<String> jsonList) {
        for(String json : jsonList) {
            dataLogger.log("I " + json);
        }
        return true;
    }
    public boolean updateDocument(List<String> jsonList) {
        for(String json : jsonList) {
            dataLogger.log("U " + json);
        }
        return true;
    }
    public boolean deleteDocument(List<String> jsonList) {
        for(String json : jsonList) {
            dataLogger.log("D " + json);
        }
        return true;
    }
}
