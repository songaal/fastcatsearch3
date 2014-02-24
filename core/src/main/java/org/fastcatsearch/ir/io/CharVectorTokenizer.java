package org.fastcatsearch.ir.io;


public class CharVectorTokenizer {
	private CharVector resultVector;
	
	private char[] buffer;
	private int cursor;
	private int offset;
	private int length;
	private int limit;
	
	public CharVectorTokenizer(CharVector charVector){
		this.buffer = charVector.array();
		this.cursor = charVector.start();
		this.limit = charVector.start() + charVector.length();
		resultVector = charVector.clone();
	}
	
	public boolean hasNext(){
		for (;cursor < limit; cursor++) {
			if(Character.isWhitespace(buffer[cursor])){
				if(length == 0){
					continue;
				}else{
					return true;
				}
			}else{
				if(length == 0){
					offset = cursor;
				}
			}
			
			length++;
		}
		
		return length > 0;
	}
	
	public CharVector next(){
		resultVector.init(offset, length);
		length = 0;
		return resultVector;
	}
}
