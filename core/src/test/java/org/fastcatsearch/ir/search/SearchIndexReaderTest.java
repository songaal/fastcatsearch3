/*
 * Copyright 2013 Websquared, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fastcatsearch.ir.search;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.fastcatsearch.ir.analysis.AnalyzerPoolManager;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.io.BufferedFileInput;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.IndexInput;
import org.fastcatsearch.ir.settings.Schema;


public class SearchIndexReaderTest extends TestCase{
	String homePath = "testHome/";
	String collection ="test3";
	
	public void testConstructor() throws IOException, SettingException, IRException{
		Schema schema = new Schema(null);//collection, true);
		File targetDir = new File("");
		AnalyzerPoolManager analyzerPoolManager = null;
		SearchIndexesReader reader = new SearchIndexesReader(schema, targetDir, analyzerPoolManager, 10000);
	}

	public void testRead() throws IOException, SettingException, IRException{
		Schema schema = new Schema(null);//collection, true);
		String target = null;
		File targetDir = new File(target); 
		
		SearchIndexReader reader = new SearchIndexReader();
		
		int fieldNum = 0;
		CharVector term = new CharVector("티셔츠");//나시 , 티셔츠, 남방 
		
		PostingDocs termDocs = reader.getPosting(term);
		
		if(termDocs == null){
			System.out.println("검색실패 !");
			
		}else{
			int count = termDocs.count();
			PostingDoc[] postingDocList = termDocs.postingDocList();
//			int[] docs =  termDocs.docs();
//			int[] tfs = termDocs.tfs();
			for(int i=0;i<count;i++){
				PostingDoc postingDoc = postingDocList[i];
				if(i < 10 || i > count - 10){
					System.out.print("("+postingDoc.docNo()+":"+postingDoc.tf()+"), ");
				}
			}
		}
		System.out.println("");
		System.out.println("totalMem = "+Runtime.getRuntime().totalMemory());
		System.out.println("freeMemory = "+Runtime.getRuntime().freeMemory());
		System.out.println("availableProcessors = "+Runtime.getRuntime().availableProcessors());
		
	}
	
	public void testAndFieldRead() throws IOException, SettingException, IRException{
		Schema schema = new Schema(null);//collection, true);
		String target = null;
		File targetDir = new File(target);
		
		long st = System.currentTimeMillis();
		
		SearchIndexReader reader = new SearchIndexReader();
		
		String fieldName1 ="title";
		String fieldName2 ="title";
		CharVector term1 = new CharVector("티셔츠");//나시 , 티셔츠, 남방 
		CharVector term2 = new CharVector("바지");
		
		PostingDocs termDocs1 = reader.getPosting(term1);
		PostingDocs termDocs2 = reader.getPosting(term2);
		
		
		//AND 검색에서는 하나라도 검색결과가 없다면 검색실패이다.
		if(termDocs1 == null || termDocs2 == null){
			System.out.println("검색실패 !");
			
		}else{
			
			PostingDoc[] termDocList1 = termDocs1.postingDocList();
			PostingDoc[] termDocList2 = termDocs2.postingDocList();
			
			
//			int[] docs1 =  termDocs1.docs();
//			int[] tfs1 = termDocs1.tfs();
//			
//			int[] docs2 =  termDocs2.docs();
//			int[] tfs2 = termDocs2.tfs();
			
			int idx1 = 0;
			int idx2 = 0;
			int count = 0;
			
			System.out.println("== AND ==");
			while(true){
				if(termDocList1[idx1].docNo() == termDocList2[idx2].docNo()){
					System.out.println(termDocList1[idx1].docNo()+":"+(termDocList1[idx1].tf() + termDocList2[idx2].tf()));
					idx1++;
					idx2++;
					count++;
					
				}else if(termDocList1[idx1].docNo() < termDocList2[idx2].docNo()){
					idx1++;
				}else{
					idx2++;
				}
				
				if(idx1 == termDocList1.length)
					break;
				
				if(idx2 == termDocList2.length)
					break;
				
			}
			
			System.out.println("---------");
			
			//나머지 뿌려주기 
			for(;idx1 < termDocList1.length;idx1++){
				System.out.println(termDocList1[idx1].docNo()+":"+termDocList1[idx1].tf());
				count++;
			}
			
			System.out.println("---------");
			
			for(;idx2 < termDocList2.length;idx2++){
				System.out.println(termDocList2[idx2].docNo()+":"+termDocList2[idx2].tf());
				count++;
			}
			
			System.out.println("총결과수  = "+count);
			System.out.println("검색시간  = "+(System.currentTimeMillis() - st));
		}
		System.out.println("");
		System.out.println("totalMem = "+Runtime.getRuntime().totalMemory());
		System.out.println("freeMemory = "+Runtime.getRuntime().freeMemory());
		System.out.println("availableProcessors = "+Runtime.getRuntime().availableProcessors());
		
	}
	
	public void testPostingFileRead() throws SettingException, IOException{
		String target = null;//IRSettings.getSegmentPath(collection, 0, 1);
		File targetDir = new File(target);
		
		IndexInput postingInput = new BufferedFileInput(targetDir, IndexFileNames.getSearchTempFileName("a"));
		byte[] buffer = new byte[1024 * 1024 * 4];
		while(true){
			int len = -1;
			try{
				len = postingInput.readVInt();
			}catch(EOFException e){
				break;
			}
			if(len < 8){
				System.out.println("len = "+len);
				break;
			}
			postingInput.readBytes(buffer, 0, len);
			
		}
		
	}
	
}


