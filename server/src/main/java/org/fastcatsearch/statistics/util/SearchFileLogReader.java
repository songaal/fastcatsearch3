package org.fastcatsearch.statistics.util;

import org.fastcatsearch.statistics.SearchLogFormatReader.SearchLog;

public class SearchFileLogReader extends LogReader<SearchLog> {

	@Override
	public SearchLog readLine(String line) {
		
		String[] el = line.split("\t");
		if(el.length > 0){
			if(el.length > 1){
				return new SearchLog(el[0], el[1]);
			}else{
				return new SearchLog(el[0], null);
			}
		}
		
		return null;
	}

}
