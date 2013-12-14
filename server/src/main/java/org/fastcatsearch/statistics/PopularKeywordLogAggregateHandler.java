package org.fastcatsearch.statistics;

import java.io.File;
import java.util.Comparator;

import org.fastcatsearch.statistics.SearchLogFormatReader.SearchLog;
import org.fastcatsearch.statistics.util.LogWriter;
import org.fastcatsearch.statistics.util.SearchFileLogReader;

public class PopularKeywordLogAggregateHandler extends LogAggregateHandler<SearchLog> {

	public static Comparator<String> comparator = new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
			return o1.compareTo(o2);
		}
	};
	public PopularKeywordLogAggregateHandler(int runSize, String encoding) {
		super(new SearchFileLogReader(), comparator, runSize, encoding);
	}

	@Override
	protected LogWriter newLogWriter(File file) {
		// TODO Auto-generated method stub
		return null;
	}

}
