package org.fastcatsearch.ir.util;

import static org.junit.Assert.*;

import java.io.File;

import org.fastcatsearch.ir.io.BufferedFileInput;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexTest {
	
	private static final Logger logger = LoggerFactory.getLogger(IndexTest.class);

	@Test
	public void tetSimple() throws Exception {
		
		//File baseDir = new File("/home/websqrd/fastcatsearch/collections/webtoon/data/index0/0/0/");
		File baseDir = new File("/home/websqrd/fastcatsearch/collections/webtoon/data/index1/0/1/");
		
		BufferedFileInput lexiconInput = new BufferedFileInput(baseDir, "search.SUBJECT.lexicon");
		BufferedFileInput postingInput = new BufferedFileInput(baseDir, "search.SUBJECT.posting");
		BufferedFileInput indexInput = new BufferedFileInput(baseDir, "search.SUBJECT.index");
		
		int option = postingInput.readInt();
		
		int count = lexiconInput.readInt();
		int docCnt = indexInput.readInt();
		char[] cbuf = indexInput.readUString();
		
		logger.debug("--------------------------------------------------------------------------------");
		logger.debug("firstdoc : {}", new String(cbuf));
		logger.debug("--------------------------------------------------------------------------------");
		
		
		long nextPos = 0;
		for(int inx=0;inx<count; inx++) {
			char[] buf = lexiconInput.readUString();
			
			nextPos = lexiconInput.readLong();
			
			logger.debug("nextPos : {} / word : {}", nextPos, new String(buf) );
			
			postingInput.seek(nextPos);
			int len = postingInput.readVInt();
			int cnt = postingInput.readInt();
			int lno = postingInput.readInt();
			
			logger.debug("len:{}/cnt:{}/lno:{}",len,cnt,lno);
			
			int prevId = -1;
			int prevPos = -1;
			
			for(int inx2=0;inx2<cnt;inx2++) {
			
				int docId = postingInput.readVInt();
				
				if(prevId>=0) {
					docId += prevId + 1;
				}
				
				int tf = postingInput.readVInt();
				int pos = postingInput.readVInt();
				
				
				
				if(prevPos>=0) {
					pos+=prevPos+1;
				}
				
				prevId = docId;
				logger.debug(" >> docId:{}/tf:{}/pos:{}", docId,tf,pos);
			}
			
		}
		
		lexiconInput.close();
		postingInput.close();
	}

}
