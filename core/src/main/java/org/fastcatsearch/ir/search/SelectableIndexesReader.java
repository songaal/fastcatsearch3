package org.fastcatsearch.ir.search;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.ir.settings.MultiRefFieldSetting;
import org.fastcatsearch.ir.settings.RefSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SelectableIndexesReader<T extends ReferencableIndexReader, S extends MultiRefFieldSetting> implements
		Cloneable {
	protected static Logger logger = LoggerFactory.getLogger(SelectableIndexesReader.class);

	protected List<T> readerList;
	protected List<S> indexSettingList;

	protected Map<String, IndexRef<T>> selectIndexCache;

	public SelectableIndexesReader() {
	}

	// 결합인덱스는 전체일치시에만 사용되면, 나머지는 단일인덱스들의 조합으로 사용됨.
	// 인덱스가 index(A,B), index(A), index(B), index(C) 가 존재할때.
	// A,B => index(A,B) 사용
	// A,B,C => index(A,B) + index(C) 사용
	// B => index(B) 사용.

	public IndexRef<T> selectIndexRef(String[] fieldList) throws IOException {
		IndexRef<T> indexRef = new IndexRef<T>();

		// 1. 일단 갯수가 동일한 인덱스를 찾는다.
		OUTTER: for (int j = 0; j < indexSettingList.size(); j++) {
			S setting = indexSettingList.get(j);
			List<RefSetting> refList = setting.getRefList();

			if (fieldList.length == refList.size()) {
				// 필드갯수가 같을때에만 사용.
				// TODO 나중에 부분적으로도 사용하게끔 수정하면 더 유연하게 사용할수 있음.

				for (int i = 0; i < fieldList.length; i++) {
					String fieldId = fieldList[i];
					if (!refList.contains(fieldId)) {
						// 필드가 없으면 실패하고 다음 인덱스를 본다.
						continue OUTTER;
					}
				}

				// 여기까지 오면 모든 필드가 존재하는 것이다.
				T reader = cloneReader(j);
				for (int i = 0; i < fieldList.length; i++) {
					String fieldId = fieldList[i];
					int sequence = refList.indexOf(fieldId);
					indexRef.add(fieldId, reader, sequence);
				}
			}
		}

		if (indexRef.getSize() > 0) {
			return indexRef;
		}

		// 2. 없으면 단일인덱스를 조합한다.

		for (int i = 0; i < fieldList.length; i++) {
			String fieldId = fieldList[i];

			for (int j = 0; j < indexSettingList.size(); j++) {
				S setting = indexSettingList.get(j);
				List<RefSetting> refList = setting.getRefList();
				if (refList.size() == 1) {
					for (int k = 0; k < refList.size(); k++) {
						// 동일필드명을 찾는다.
						if (refList.get(k).getRef().equals(fieldId)) {
							T reader = cloneReader(j);// readerList.get(j).clone();
							indexRef.add(fieldId, reader, 0); // 단일필드이므로 sequence는 0이다.
						}
					}
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
