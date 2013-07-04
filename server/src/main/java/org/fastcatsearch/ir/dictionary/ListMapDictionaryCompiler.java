package org.fastcatsearch.ir.dictionary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ListMapDictionaryCompiler {
	
	public static void main(String[] args) throws IOException {

		String sourceFilePath = args[0];
		String binaryFilePath = args[1];
		File sourceFile = new File(sourceFilePath);
		File binaryFile = new File(binaryFilePath);
		
		ListMapDictionary dictionary = new ListMapDictionary();
		dictionary.loadSource(sourceFile);
		
		OutputStream out = new FileOutputStream(binaryFile); 
		dictionary.writeTo(out);
		out.close();
		
		System.out.println("바이너리 사전을 생성하였습니다. "+binaryFile.getAbsolutePath()+" ("+binaryFile.length()+"B)");
	}
}
