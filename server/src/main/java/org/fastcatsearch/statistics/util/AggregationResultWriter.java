package org.fastcatsearch.statistics.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.fastcatsearch.statistics.LogAggregator.Counter;

public class AggregationResultWriter {
	
	private Writer writer;
	
	public AggregationResultWriter(File file) throws IOException {
		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
	}
	
	public void close() {
		if(writer != null){
			try {
				writer.close();
			} catch (IOException e) {
				
			}
		}
	}

	public void write(String key, Counter value) throws IOException{
		writer.write(formatLog(key, value.value()));
	}
	
	protected String formatLog(String key, int count) {
		return key + "\t" + count;
	}

}
