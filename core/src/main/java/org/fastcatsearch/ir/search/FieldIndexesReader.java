package org.fastcatsearch.ir.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.settings.FieldIndexSetting;
import org.fastcatsearch.ir.settings.Schema;

public class FieldIndexesReader extends SelectableIndexesReader<FieldIndexReader, FieldIndexSetting> {

	public FieldIndexesReader() {
	}

	public FieldIndexesReader(Schema schema, File dir) throws IOException, IRException {
		indexSettingList = schema.schemaSetting().getFieldIndexSettingList();
		int indexCount = indexSettingList == null ? 0 : indexSettingList.size();

		// 색인파일열기.
		readerList = new ArrayList<FieldIndexReader>(indexCount);
		for (int i = 0; i < indexCount; i++) {
			FieldIndexSetting setting = indexSettingList.get(i);
			FieldIndexReader reader = null;
			try {
				reader = new FieldIndexReader(setting, schema.fieldSettingMap(), dir);
			} catch (Exception e) {
				logger.error("필드색인 {}로딩중 에러 >> {}", setting.getId(), e);
			}
			readerList.add(reader);
		}
        referenceCount = new AtomicInteger();
	}
    public int getReferenceCount() {
        return referenceCount.intValue();
    }
	@Override
	public FieldIndexesReader clone() {
		FieldIndexesReader reader = new FieldIndexesReader();
		reader.indexSettingList = indexSettingList;
		reader.readerList = new ArrayList<FieldIndexReader>(readerList.size());
		for (FieldIndexReader r : readerList) {
			FieldIndexReader newReader = null;
			if (r != null) {
				newReader = r.clone();
			}
			reader.readerList.add(newReader);
		}
        reader.referenceCount = referenceCount;
        referenceCount.incrementAndGet();
		return reader;
	}

	protected FieldIndexReader cloneReader(int sequence) {
		return readerList.get(sequence).clone();
	}

}
