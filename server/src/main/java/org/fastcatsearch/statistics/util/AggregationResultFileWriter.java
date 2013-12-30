package org.fastcatsearch.statistics.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class AggregationResultFileWriter implements AggregationResultWriter {
	private Writer writer;
	private int minimumCount; //유효한 최소 검색횟수. 이 횟수보다 작으면 기록하지 않는다.
	 
	public AggregationResultFileWriter(File file, String encoding) {
		this(file, encoding, 0);
	}
	public AggregationResultFileWriter(File file, String encoding, int minimumCount) {
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
		} catch (IOException e) {
			logger.error("", e);
		}
		this.minimumCount = minimumCount;
	}

	public void write(String key, int count) throws IOException {
//		logger.debug("final write >> {} : {} ( {} )",key, count, minimumCount);
		if(count < minimumCount){
			return;
		}
		
		writer.write(formatLog(key, count));
		writer.write("\n");
	}

	protected String formatLog(String key, int count) {
		return key + "\t" + count;
	}

	public void close() {
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException ignore) {
			}
		}
	}

}
