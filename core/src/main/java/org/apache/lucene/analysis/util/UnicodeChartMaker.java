package org.apache.lucene.analysis.util;

import java.io.InputStream;

public class UnicodeChartMaker {
	public static void main(String[] args) {
		UnicodeChartMaker maker = new UnicodeChartMaker();
		maker.make();
	}
	
	public void make(){
		InputStream is = getClass().getResourceAsStream("/org/apache/lucene/analysis/common/unicode_type.txt");
		System.out.println(is);
		//TODO byte[]로 만들어서 dataoutputstream으로 쓴다. unicode.bin 파일로.
		
	}
}
