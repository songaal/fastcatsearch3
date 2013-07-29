package org.fastcatsearch.ir.search;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.ir.settings.RefSetting;
import org.fastcatsearch.ir.settings.ReferencableFieldSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SelectableIndexesReader<T extends ReferencableIndexReader, S extends ReferencableFieldSetting> implements Cloneable {
	protected static Logger logger = LoggerFactory.getLogger(SelectableIndexesReader.class);

	protected List<T> readerList;
	protected List<S> indexSettingList;

	protected Map<String, IndexRef<T>> selectIndexCache;

	public SelectableIndexesReader() {
	}


	public IndexRef<T> selectIndexRef(String[] fieldList) throws IOException {
		IndexRef<T> indexRef = new IndexRef<T>();

		// 단일인덱스를 조합한다.

		for (int i = 0; i < fieldList.length; i++) {
			String fieldId = fieldList[i];

			for (int j = 0; j < indexSettingList.size(); j++) {
				S setting = indexSettingList.get(j);
				String refFieldId = setting.getRef();
				// 동일필드명을 찾는다.
				if (refFieldId.equals(fieldId)) {
					T reader = cloneReader(j);
					indexRef.add(fieldId, reader); // 단일필드이므로 sequence는 0이다.
					logger.debug("Select Index2 field={} r={}, seq={}", fieldId, reader, 0);
					break;
				}

			}
		}

		return indexRef;
	}

	protected abstract T cloneReader(int indexSequence);

	public T getIndexReader(int indexSequence) {
		return readerList.get(indexSequence);
	}

	public void close() throws IOException {
		for (T reader : readerList) {
			reader.close();
		}
	}

}
