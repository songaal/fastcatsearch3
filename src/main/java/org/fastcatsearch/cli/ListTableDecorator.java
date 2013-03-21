package org.fastcatsearch.cli;

import java.io.IOException;
import java.util.List;

public class ListTableDecorator {
	
	private Appendable writer;
	
	private List<Integer> columnSize;
	
	public ListTableDecorator(Appendable writer, List<Integer> columnSize) {
		this.writer = writer;
		this.columnSize = columnSize;
	}
	
	public void printbar() throws IOException {
		writer.append("+");
		for(int cinx=0;cinx<columnSize.size();cinx++) {
			writer.append("-");
			for(int inx=0;inx<columnSize.get(cinx);inx++) {
				writer.append("-");
			}
			writer.append("-+");
		}
		writer.append("\n");
	}
	
	public void printData(int columnInx, Object data) throws IOException {
		if(columnInx == 0) {
			writer.append("|");
		}
		
		writer.append(String.format(" %"+columnSize.get(columnInx)+"s ", data.toString()));
		writer.append("|");
		
		if(columnInx==columnSize.size() -1) {
			writer.append("\n");
		}
	}
}
