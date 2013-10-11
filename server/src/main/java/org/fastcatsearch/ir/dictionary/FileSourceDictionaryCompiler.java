package org.fastcatsearch.ir.dictionary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileSourceDictionaryCompiler {
	
	public static void main(String[] args) throws IOException {

		String sourceFilePath = args[0];
		String binaryFilePath = args[1];
		String type = args[2];
		boolean ignoreCase = true;
		if(args.length > 3){
			ignoreCase = args[3].trim().equalsIgnoreCase("true");
		}
		
		//FIXME
		boolean[] valuesIgnoreCase = new boolean[1];
		
		File sourceFile = new File(sourceFilePath);
		File binaryFile = new File(binaryFilePath);
		
		SourceDictionary dictionary = null;
		if(type.equals("set")){
			dictionary = new SetDictionary();
		}else if(type.equals("map")){
			dictionary = new MapDictionary();
		}else if(type.equals("synonym")){
			dictionary = new SynonymDictionary();
		}else if(type.equals("custom")){
			dictionary = new CustomDictionary();
		}
		dictionary.loadSource(sourceFile, ignoreCase, valuesIgnoreCase);
		
		OutputStream out = new FileOutputStream(binaryFile); 
		dictionary.writeTo(out);
		out.close();
		
		System.out.println("바이너리 사전을 생성하였습니다. "+binaryFile.getAbsolutePath()+" ("+binaryFile.length()+"B)");
	}
}
