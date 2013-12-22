package org.fastcatsearch.datasource.reader;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.search.SegmentDocumentReader;
import org.fastcatsearch.ir.settings.SchemaSetting;

/**
 * 저장된 세그먼트의 문서를 읽어온다.
 * */
public class StoredDocumentSourceReader extends AbstractDataSourceReader<Document> {
	
	public StoredDocumentSourceReader(SchemaSetting schemaSetting) throws IRException {
		super(schemaSetting);
	}

	public StoredDocumentSourceReader(File filePath, SchemaSetting schemaSetting) throws IRException {
		super(schemaSetting);
		File indexHome = new File("");
		
		//TODO segmentSize
		int segmentSize = 1;
		for(int i=0;i<segmentSize;i++){
			String segmentId = String.valueOf(i);
			File segHomePath = new File(indexHome, segmentId);
			SingleSourceReader<Document> sourceReader;
			try {
				sourceReader = new SegmentDocumentSourceReader(schemaSetting, segHomePath);
				addSourceReader(sourceReader);
			} catch (IOException e) {
				logger.error("", e);
			}
		}
		
	}
	
	@Override
	protected Document createDocument(Document nextElement) throws IRException {
		return nextElement;
	}
	
	public static class SegmentDocumentSourceReader extends SingleSourceReader<Document> {
		
		private Enumeration<Document> enumeration;
		private SegmentDocumentReader reader;
		
		public SegmentDocumentSourceReader(SchemaSetting schemaSetting, File segHomePath) throws IOException {
			
			
			reader = new SegmentDocumentReader(schemaSetting, segHomePath);
			
			
			enumeration = reader.getEnumertion();
		}

		@Override
		public void init() throws IRException {
			
		}

		@Override
		public boolean hasNext() throws IRException {
			return enumeration.hasMoreElements();
		}

		@Override
		protected Document next() throws IRException {
			return enumeration.nextElement();
		}

		@Override
		public void close() throws IRException {
			reader.close();			
		}
		
	}
}
