package org.fastcatsearch.datasource.reader;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.document.Document;
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
public class StoredSegmentSourceReader extends AbstractDataSourceReader<Document> {

	public StoredSegmentSourceReader(SchemaSetting schemaSetting) throws IRException {
		super(schemaSetting);
	}

	public StoredSegmentSourceReader(File[] segmentPaths, SchemaSetting schemaSetting) throws IRException {
		super(schemaSetting);

		logger.debug("StoredSegmentSourceReader segmentPaths >> {}", segmentPaths);

		//segment document reader를 만들어 single source reader에 추가한다.
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

		private Enumeration<Document> enumeration;
		private SegmentIndexableDocumentReader reader;

		public SegmentDocumentSourceReader(SchemaSetting schemaSetting, File segHomePath) throws IOException {

			reader = new SegmentIndexableDocumentReader(schemaSetting, segHomePath);

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

		@Override
		protected void initParameters() {

		}

	}
}
