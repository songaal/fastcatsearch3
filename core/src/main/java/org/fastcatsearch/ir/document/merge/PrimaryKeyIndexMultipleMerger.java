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

package org.fastcatsearch.ir.document.merge;

import java.io.IOException;

import org.fastcatsearch.ir.document.TempPrimaryKeyIndexReader;
import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.io.IndexOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * pk색인시 flush한 임시 pk파일들을 머징한다.
 * */
public class PrimaryKeyIndexMultipleMerger {
	protected static Logger logger = LoggerFactory.getLogger(PrimaryKeyIndexMultipleMerger.class);

	protected int[] heap;
	protected TempPrimaryKeyIndexReader[] reader;
	protected int flushCount;

	private KeyValue kv;
	private KeyValue kvOld;
	protected int totalCount;

	public PrimaryKeyIndexMultipleMerger(TempPrimaryKeyIndexReader[] reader) throws IOException {
		this.flushCount = reader.length;
		this.reader = reader;
		for (TempPrimaryKeyIndexReader r : reader) {
			r.next();
		}
		kv = new KeyValue();
		kvOld = new KeyValue();
	}

	public void mergeAndMakeIndex(IndexOutput output, IndexOutput indexOutput, int indexInterval) throws IOException {
		logger.debug("**** mergeAndMakeIndex ****");
		logger.debug("flushCount={}", flushCount);

		if (flushCount <= 0) {
			output.writeInt(0);
			indexOutput.writeInt(0);
			return;
		}

		// to each field
		logger.debug("## MERGE PK field");

		makeHeap(flushCount);

		int termCount = 0;
		int indexTermCount = 0;

		output.writeInt(termCount);// termCount
		indexOutput.writeInt(indexTermCount);// indexTermCount
		KeyValue keyValue = new KeyValue();
		while (readNextTempIndex(keyValue)) {
			// logger.debug("####keyValue > {}", keyValue);
			BytesBuffer key = keyValue.key();
			// write pkmap index
			if (indexInterval > 0 && termCount % indexInterval == 0) {
				indexOutput.writeVInt(key.length());
				indexOutput.writeBytes(key.bytes, key.offset, key.length());
				indexOutput.writeLong(output.position());
				indexTermCount++;
			}

			output.writeVInt(key.length());
			output.writeBytes(key.bytes, key.offset, key.length());
			output.writeInt(keyValue.value());

			termCount++;
		}

		logger.debug("pk index count = {}", indexTermCount);
		logger.debug("filesize = {} bytes", output.position());

		// write idxCount
		// long p = indexOutput.position();
		if (termCount > 0) {
			output.seek(0);
			output.writeInt(termCount);
			indexOutput.seek(0);
			indexOutput.writeInt(indexTermCount);
		} else {
			// 이미 indexTermCount는 0으로 셋팅되어 있으므로 기록할 필요없음.
		}
		logger.debug("## write PK termCount[{}] indexTermCount[{}] indexInterval[{}]", termCount, indexTermCount, indexInterval);

		output.flush();
		indexOutput.flush();

	}

	class KeyValue {
		private BytesBuffer key;
		private int value;

		public KeyValue() {
		}

		public void init(BytesBuffer key, int value) {
			this.key = key;
			this.value = value;
		}

		public BytesBuffer key() {
			return key;
		}

		public int value() {
			return value;
		}

		public boolean isNull() {
			return key == null;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || ((KeyValue) obj).key == null) {
				return false;
			}

			return key.equals(((KeyValue) obj).key);
		}

		@Override
		public String toString() {
			return (key != null ? key.toAlphaString() : key) + " >> " + value;
		}
	}

	// 여러번 flush된 임시 posing 파일에서 정렬된 단어들을 읽어들여 posting을 tempPostingOutput 하나로 머징한다.
	protected boolean readNextTempIndex(KeyValue keyValue) throws IOException {
		boolean isMade = false;

		// int kk = 0;
		while (true) {
			int idx = heap[1];
			kv.init(reader[idx].key(), reader[idx].docNo());

			// logger.debug("kvOld > {}", kvOld);
			// logger.debug("kv > {}", kv);
			// logger.debug("---");
			// if (kv.isNull() && kvOld == null) {
			if (kv.isNull()) {
				// if cv and cvOld are null, it's done
				return false;
			}

			// cv == null일경우는 모든 reader가 종료되어 null이 된경우이며
			// cvOld 와 cv 가 다른 경우는 머징시 텀이 바뀐경우. cvOld를 기록해야한다.
			if ((kv.isNull() || !kv.equals(kvOld)) && !kvOld.isNull()) {
				keyValue.init(kvOld.key(), kvOld.value());
				isMade = true;
			}

			// backup cv to old
			kvOld.init(kv.key, kv.value);

			reader[idx].next();

			heapify(1, flushCount);

			if (isMade) {
				return true;
			}
		} // while(true)

	}

	public void close() throws IOException {
		IOException exception = null;
		for (int i = 0; i < flushCount; i++) {
			if (reader[i] != null) {
				try {
					reader[i].close();
				} catch (IOException e) {
					exception = e;
				}
			}
		}
		if (exception != null) {
			throw exception;
		}
	}

	protected void makeHeap(int heapSize) {
		heap = new int[heapSize + 1];
		// index starts from 1
		for (int i = 0; i < heapSize; i++) {
			heap[i + 1] = i;
		}

		int n = heapSize >> 1; // last inner node index

		for (int i = n; i > 0; i--) {
			heapify(i, heapSize);
		}

	}

	protected void heapify(int idx, int heapSize) {

		int temp = -1;
		int child = -1;

		while (idx <= heapSize) {
			int left = idx << 1;// *=2
			int right = left + 1;

			if (left <= heapSize) {
				if (right <= heapSize) {
					// 키워드가 동일할 경우 먼저 flush된 reader가 우선해야, docNo가 오름차순 정렬순서대로 올바로 기록됨.
					// flush후 머징시 문제가 생기는 버그 해결됨 2013-5-21 swsong
					int c = compareKey(left, right);
					if (c < 0) {
						child = left;
					} else if (c > 0) {
						child = right;
					} else {
						// 하위 value 둘이 같아서 seq확인.
						// 같다면 id가 작은게 우선.
						int a = heap[left];
						int b = heap[right];
						if (reader[a].docNo() < reader[b].docNo()) {
							child = left;
						} else {
							child = right;
						}
					}
				} else {
					// if there is no right el.
					child = left;
				}
			} else {
				// no children
				break;
			}

			// compare and swap
			int c = compareKey(child, idx);
			if (c < 0) {
				temp = heap[child];
				heap[child] = heap[idx];
				heap[idx] = temp;
				idx = child;
			} else if (c == 0) {
				// 하위와 자신의 value가 같아서 seq확인
				// 같다면 seq가 작은게 우선.
				int a = heap[idx];
				int b = heap[child];
				if (reader[a].docNo() > reader[b].docNo()) {
					// 하위의 seq가 작아서 child채택!
					temp = heap[child];
					heap[child] = heap[idx];
					heap[idx] = temp;
					idx = child;
				} else {
					// 내것을 그대로 사용.
					// sorted
					break;
				}
			} else {
				// sorted, then do not check child
				break;
			}

		}
	}

	protected int compareKey(int one, int another) {

		int a = heap[one];
		int b = heap[another];

		return compareKey(reader[a].key(), reader[b].key());
	}

	protected int compareKey(BytesBuffer key1, BytesBuffer key2) {

		// reader gets EOS, returns null
		if (key1 == null && key2 == null) {
			return 0;
		} else if (key1 == null)
			return 1;
		else if (key2 == null)
			return -1;

		return BytesBuffer.compareBuffer(key1, key2);
	}
}
