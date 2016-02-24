package org.fastcatsearch.ir.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Created by swsong on 2016. 2. 24..
 */
public class SegmentDelayedClose implements Delayed {
    private static Logger logger = LoggerFactory.getLogger(SegmentDelayedClose.class);
    private String collectionId;
    private String segmentId;
    private Map<String, SegmentReader> tempReaderMap;

    private int delayInSec;
    private long startTime;
    private boolean deleteDirectory;

    public SegmentDelayedClose(String collectionId, String segmentId, Map<String, SegmentReader> tempReaderMap, boolean deleteDirectory) {
        this(collectionId, segmentId, 10, tempReaderMap, deleteDirectory);
    }
    public SegmentDelayedClose(String collectionId, String segmentId, int delayInSec, Map<String, SegmentReader> tempReaderMap, boolean deleteDirectory) {
        this.collectionId = collectionId;
        this.segmentId = segmentId;
        this.delayInSec = delayInSec;
        this.startTime = System.currentTimeMillis() + delayInSec * 1000;
        this.tempReaderMap = tempReaderMap;
        this.deleteDirectory = deleteDirectory;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = startTime - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);

    }

    public int getDelayInSec() {
        return delayInSec;
    }

    public void closeReader() {
        SegmentReader reader = tempReaderMap.remove(segmentId);
        if(reader != null) {
            try {
                if(deleteDirectory) {
                    reader.closeAndDelete();
                    logger.debug("## [{}] Segment [{}] is closed and deleted.", collectionId, segmentId);
                } else {
                    reader.close();
                    logger.debug("## [{}] Segment [{}] is closed.", collectionId, segmentId);
                }
            } catch (IOException e) {
                logger.error("error while close segment reader = " + reader.segmentInfo().getId(), e);

            }
        }
    }

    @Override
    public int compareTo(Delayed o) {
        if (this.startTime < ((SegmentDelayedClose) o).startTime) {
            return -1;
        }
        if (this.startTime > ((SegmentDelayedClose) o).startTime) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "SegmentDelayedClose { collectionId=" + collectionId + ", segmentId=" + segmentId + ", startTime=" + startTime + "}";
    }
}
