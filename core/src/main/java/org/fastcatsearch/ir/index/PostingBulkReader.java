package org.fastcatsearch.ir.index;

import java.io.File;
import java.io.IOException;

import org.fastcatsearch.ir.io.BufferedFileInput;
import org.fastcatsearch.ir.io.Input;

public class PostingBulkReader {
	Input input;
	IndexFieldOption[] fieldIndexOption;
	
	public PostingBulkReader(Input input) throws IOException{
		this.input = input;
		int fieldCount = input.readInt();
		System.out.println("field count = "+fieldCount);
		fieldIndexOption = new IndexFieldOption[fieldCount];
		for (int i = 0; i < fieldCount; i++) {
			fieldIndexOption[i] = new IndexFieldOption(input.readInt());
		}
	}
	
	public void read() throws IOException{
		
		for (int k = 0; k < fieldIndexOption.length; k++) {
			
			int dataLen = input.readVariableByte();
			int count = input.readInt();
			int lastDocNo = input.readInt();
			System.out.println("====field====");
			System.out.println("** data="+dataLen+", count="+count+", lastdoc="+lastDocNo);
			int prevDocNo = -1;
			int docNo = -1;
			for (int i = 0; i < count; i++) {
				docNo = input.readVariableByte();
				if(i > 0){
					docNo += (prevDocNo + 1);
				}
				
				prevDocNo = docNo;
				
				int tf = input.readVariableByte();
				
				System.out.println(">"+docNo +" : "+tf);
				if(fieldIndexOption[k].isStorePosition()){
					int position = input.readVariableByte();
					System.out.println(" #"+position);
				}
			}
		
		}
		
		
	}
	
}
