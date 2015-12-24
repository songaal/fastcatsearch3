package org.fastcatsearch.datasource.reader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.search.SegmentIndexableDocumentReader;
import org.fastcatsearch.ir.settings.SchemaSetting;

/**
 * 저장된 컬렉션하위 모든 세그먼트의 문서를 읽어온다.
 * */
public class StoredDocumentSourceReader extends AbstractDataSourceReader<Document> {

	public StoredDocumentSourceReader(SchemaSetting schemaSetting) throws IRException {
		super(schemaSetting);
	}

	public StoredDocumentSourceReader(File indexPath, SchemaSetting schemaSetting) throws IRException {
		super(schemaSetting);

		
		
		//세그먼트 id를 찾아내서 작은순서대로 정렬한다.
		String[] segmentIdList = indexPath.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				try {
					int i = Integer.parseInt(name);
					return true;
				} catch (NumberFormatException e) {
					return false;
				}
			}
		});
		logger.debug("StoredDocumentSourceReader indexHome >> {}, segmentIdList >> {}", indexPath, segmentIdList);
		
		Arrays.sort(segmentIdList, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				int id1 = Integer.parseInt(o1);
				int id2 = Integer.parseInt(o2);
				return id1 - id2;
			}
		});

		//segment document reader를 만들어 single source reader에 추가한다.
		for (int i = 0; i < segmentIdList.length; i++) {
			String segmentId = String.valueOf(segmentIdList[i]);
			File segHomePath = new File(indexPath, segmentId);
			try {
				SingleSourceReader<Document> sourceReader = new SegmentDocumentSourceReader(schemaSetting, segHomePath);
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
			// TODO Auto-generated method stub
			
		}

	}
}
