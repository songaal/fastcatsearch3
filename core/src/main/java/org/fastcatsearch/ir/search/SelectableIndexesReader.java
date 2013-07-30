package org.fastcatsearch.ir.search;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

			T reader = null;
			for (int j = 0; j < indexSettingList.size(); j++) {
				S setting = indexSettingList.get(j);
				String refFieldId = setting.getRef();
				// 동일필드명을 찾는다.
				if (refFieldId.equals(fieldId)) {
					reader = cloneReader(j);
					
					break;
				}

			}
			
			indexRef.add(fieldId, reader);
			logger.debug("Select Index2 field={} r={}", fieldId, reader);
			
			if(reader == null){
				logger.error("색인된 필드를 찾지못함.>>{}", fieldId);
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
