package org.fastcatsearch.processlogger;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.fastcatsearch.common.BytesReference;
import org.fastcatsearch.common.io.BytesStreamInput;
import org.fastcatsearch.common.io.BytesStreamOutput;
import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.processlogger.log.IndexingStartProcessLog;
import org.junit.Test;

public class ProcessLoggerJobTest {

	@Test
	public void testMany() throws IOException {
		long st = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			test();
		}
		System.out.println((System.currentTimeMillis() - st) + "ms");
	}

	@Test
	public void test() throws IOException {

		IndexingStartProcessLog log = new IndexingStartProcessLog("test", IndexingType.FULL, 11111111111111L, false);

		ProcessLoggerJob job = new ProcessLoggerJob(IndexingProcessLogger.class, log);

		BytesStreamOutput output = new BytesStreamOutput();

		job.writeTo(output);

		BytesReference ref = output.bytesReference();

		StreamInput input = new BytesStreamInput(ref);

		ProcessLoggerJob job2 = new ProcessLoggerJob();
		job2.readFrom(input);

		IndexingStartProcessLog indexingStartProcessLog = (IndexingStartProcessLog) job2.getProcessLog();

		assertEquals("test", indexingStartProcessLog.getCollectionId());
		assertEquals(IndexingType.FULL, indexingStartProcessLog.getIndexingType());
		assertEquals(11111111111111L, indexingStartProcessLog.getStartTime());
	}

}
