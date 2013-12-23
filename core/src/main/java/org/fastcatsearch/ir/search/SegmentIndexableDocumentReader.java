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

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.document.DocumentReader;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 각 세그먼트별 문서를 색인가능하도록 읽어온다.
 * 
 * @author sangwook.song
 * 
 */
public class SegmentIndexableDocumentReader {
	private static Logger logger = LoggerFactory.getLogger(SegmentIndexableDocumentReader.class);
	private final DocumentReader reader;
	private final int limit;
	
	public SegmentIndexableDocumentReader(SchemaSetting schemaSetting, File segHomePath) throws IOException {
		reader = new DocumentReader(schemaSetting, segHomePath);
		limit = reader.getDocumentCount();
	}

//	public Document[] getDocumentList(int[] docNos) throws IOException {
//		Document[] result = new Document[docNos.length];
//		for (int i = 0; i < docNos.length; i++) {
//			int docNo = docNos[i];
//			result[i] = reader.readDocument(docNo);
//		}
//		return result;
//	}
//
//	public Document getDocument(int docNo) throws IOException {
//		return reader.readDocument(docNo);
//	}

	public Enumeration<Document> getEnumertion() {
		return new SegmentDocumentEnumeration();
	}

	public void close() {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				logger.error("", e);
			}
		}
	}

	private class SegmentDocumentEnumeration implements Enumeration<Document> {
		private int pos;
		
		@Override
		public boolean hasMoreElements() {
			return pos < limit;
		}

		@Override
		public Document nextElement() {
			try {
				Document document =  reader.readIndexableDocument(pos++);
				return document;
			} catch (IOException e) {
				logger.error("", e);
				return null;
			}
		}

	}

}
