package org.fastcatsearch.datasource.reader;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.document.DocumentReader;
import org.fastcatsearch.ir.io.BitSet;
import org.fastcatsearch.ir.search.SegmentIndexableDocumentReader;
import org.fastcatsearch.ir.settings.SchemaSetting;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;

/**
 * 저장된 세그먼트의 문서를 읽어온다.
 * */
public class SegmentSourceReader extends AbstractDataSourceReader<Document> {

	public SegmentSourceReader(SchemaSetting schemaSetting) throws IRException {
		super(schemaSetting);
	}

	public SegmentSourceReader(File[] segmentPaths, SchemaSetting schemaSetting) throws IRException {
		super(schemaSetting);

		logger.debug("StoredSegmentSourceReader segmentPaths >> {}", segmentPaths);

		for (int i = 0; i < segmentPaths.length; i++) {
			String segmentId = segmentPaths[i].getName();
			try {
				SingleSourceReader<Document> sourceReader = new SegmentDocumentSourceReader(schemaSetting, segmentPaths[i]);
				logger.debug("SegmentDocumentSourceReader {} >> {}", segmentId, sourceReader);
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

        private final DocumentReader reader;
        private final int limit;
        private final BitSet deleteSet;

        private int docNo;
        private int lastDocNo;
        private transient int validDocs;
        private File segHomePath;

		public SegmentDocumentSourceReader(SchemaSetting schemaSetting, File segHomePath) throws IOException {

            this.segHomePath = segHomePath;
            reader = new DocumentReader(schemaSetting, segHomePath);
            deleteSet = new BitSet(segHomePath, IndexFileNames.docDeleteSet);
            limit = reader.getDocumentCount();

            logger.debug("[{}] segment document reader start. total[{}] delete[{}] ", segHomePath.getName(), limit, deleteSet.getOnCount());
		}

		@Override
		public void init() throws IRException {

		}

		@Override
		public boolean hasNext() throws IRException {
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
		protected Document next() throws IRException {
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

		@Override
		public void close() throws IRException {
            logger.debug("[{}] segment document reader close. total[{}] valid[{}] ", segHomePath.getName(), limit, validDocs);
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.error("", e);
                }
            }
		}

		@Override
		protected void initParameters() {

		}

	}
}
