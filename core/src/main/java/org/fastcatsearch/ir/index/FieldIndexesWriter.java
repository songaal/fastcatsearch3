package org.fastcatsearch.ir.index;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.settings.FieldIndexSetting;
import org.fastcatsearch.ir.settings.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 여러 필드인덱스에 대한 색인클래스.
 * */
public class FieldIndexesWriter implements WriteInfoLoggable {
	private static Logger logger = LoggerFactory.getLogger(FieldIndexesWriter.class);
	private FieldIndexWriter[] fieldIndexWriterList;
	private int indexSize;

	public FieldIndexesWriter(Schema schema, File dir, RevisionInfo revisionInfo) throws IOException, IRException {
		List<FieldIndexSetting> fieldIndexSettingList = schema.schemaSetting().getFieldIndexSettingList();
		indexSize = fieldIndexSettingList == null ? 0 : fieldIndexSettingList.size();
		fieldIndexWriterList = new FieldIndexWriter[indexSize];
		boolean isAppend = revisionInfo.isAppend();
		for (int i = 0; i < indexSize; i++) {
			fieldIndexWriterList[i] = new FieldIndexWriter(fieldIndexSettingList.get(i), schema.fieldSettingMap(), schema.fieldSequenceMap(), dir, isAppend);
		}

	}

	public void write(Document document) throws IOException, IRException {
		for (int i = 0; i < indexSize; i++) {
			fieldIndexWriterList[i].write(document);
		}
	}

	public void flush() throws IOException {
		for (int i = 0; i < indexSize; i++) {
			fieldIndexWriterList[i].flush();
		}
	}

	public void close() throws IOException {
		for (int i = 0; i < indexSize; i++) {
			fieldIndexWriterList[i].close();
		}
	}

	@Override
	public void getIndexWriteInfo(IndexWriteInfoList writeInfoList) {
		for (int i = 0; i < indexSize; i++) {
			fieldIndexWriterList[i].getIndexWriteInfo(writeInfoList);
		}
	}
}
