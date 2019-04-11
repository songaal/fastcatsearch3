package org.fastcatsearch.job;

import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.search.SegmentDelayedClose;

import java.util.concurrent.DelayQueue;

/**
 * Created by swsong on 2016. 2. 24..
 */
public class SegmentDelayCloseScheduleJob extends ScheduledJob {

    private boolean isCanceled;

    private DelayQueue<SegmentDelayedClose> segmentDelayCloseQueue;

    public SegmentDelayCloseScheduleJob(String key, DelayQueue<SegmentDelayedClose> segmentDelayCloseQueue) {
        super(key);
        this.segmentDelayCloseQueue = segmentDelayCloseQueue;
    }

    @Override
    public void cancel() {
        isCanceled = true;
    }

    @Override
    public boolean isCanceled() {
        return isCanceled;
    }

    @Override
    public JobResult doRun() throws FastcatSearchException {
        while (!isCanceled) {
            try {
                SegmentDelayedClose segmentDelayedClose = segmentDelayCloseQueue.take();
                if(segmentDelayedClose != null) {
                    segmentDelayedClose.closeReader();
                }
            } catch (Throwable e) {
                //ignore
            }
        }
        logger.debug("Segment delay closer finished!");
        return new JobResult();
    }
}
