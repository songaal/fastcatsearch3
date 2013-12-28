package org.fastcatsearch.statistics.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class AggregationResultFileWriter implements AggregationResultWriter {
	private Writer writer;

	public AggregationResultFileWriter(File file, String encoding) {
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
		} catch (IOException e) {
			logger.error("", e);
		}
	}

	public void write(String key, int count) throws IOException {
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
