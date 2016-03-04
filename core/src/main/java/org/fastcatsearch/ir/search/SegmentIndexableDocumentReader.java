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

import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.document.DocumentReader;
import org.fastcatsearch.ir.io.BitSet;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

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
    private final BitSet deleteSet;

    public SegmentIndexableDocumentReader(SchemaSetting schemaSetting, File segHomePath) throws IOException {
		reader = new DocumentReader(schemaSetting, segHomePath);
        deleteSet = new BitSet(segHomePath, IndexFileNames.docDeleteSet);
		limit = reader.getDocumentCount();
	}

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
		private int docNo;
		private int lastDocNo;

        private transient int validDocs;
		@Override
		public boolean hasMoreElements() {
			while(docNo < limit) {
                if(!deleteSet.isSet(docNo)) {
                    lastDocNo = docNo;
                    validDocs++;
                } else {
                    logger.trace("doc {} is deleted and ignored for merging", docNo);
                    lastDocNo = -1;
                }
                docNo++;
                if(lastDocNo != -1) {
                    return true;
                }
            }
            lastDocNo = -1;
            return false;
		}

		@Override
		public Document nextElement() {
			try {
                if(lastDocNo != -1) {
                    return reader.readIndexableDocument(lastDocNo);
                } else {
                    return null;
                }
			} catch (IOException e) {
				logger.error("", e);
				return null;
			}
		}

        public int getValidDocs() {
            return validDocs;
        }
    }

}
