package org.fastcatsearch.cli;

import java.util.List;
import java.util.Map;

public class ListTableDecorator {
	
	private Appendable writer;
	
	private List<Integer> columnSize;
	
	public ListTableDecorator(Appendable writer) {
		this.writer = writer;
	}
	
	public void printbar() {
		
		for(int cinx=0;cinx<columnSize.size();cinx++) {
			
		}
	}
	
//	public void printData(List<Object> data) {
//		for(Object record : data) {
//			if(record instanceof List) {
//				
//			} else if(record instanceof Map) {
//				
//				//print header from map key
//				
//				//print record from map data
//				
//			}
//		}
//	}
}
