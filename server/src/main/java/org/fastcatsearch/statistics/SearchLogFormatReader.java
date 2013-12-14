package org.fastcatsearch.statistics;

public class SearchLogFormatReader extends LogFormatReader {

	public SearchLog readLine(String line) {
		
		String[] el = line.split("\t");
		String keyword = null;
		String prevKeyword = null;
		if(el.length > 0){
			keyword = el[0];
			if(el.length > 1){
				prevKeyword = el[1];
			}
			return new SearchLog(keyword, prevKeyword);
		}
		
		return null;
	}

	public static class SearchLog extends AbstractLog {
		private String prevKeyword;
		
		public SearchLog(String keyword, String prevKeyword) {
			super(keyword);
			this.prevKeyword = prevKeyword;
		}

		public String getPrevKeyword() {
			return prevKeyword;
		}

	}
}
