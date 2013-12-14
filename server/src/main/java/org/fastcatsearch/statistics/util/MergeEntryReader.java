package org.fastcatsearch.statistics.util;

import org.fastcatsearch.statistics.LogFileRunEntryReader;

public class MergeEntryReader<E extends RunEntry> {
	public MergeEntryReader(RunEntryReader<E>[] runReaderList){
		
	}
	
	public MergeEntryReader(LogFileRunEntryReader[] entryReaderList) {
		// TODO Auto-generated constructor stub
	}

	public E read(){
		
		return null;
	}
	
	//TODO heap을 사용하여 구현.
	
	
	
	
	
	
//	protected boolean readNextTempIndex(CharVector term) throws IOException {
//	tempPostingOutput.reset();
//	boolean termMade = false;
//
//	// int kk = 0;
//	while (true) {
//		int idx = heap[1];
//		cv = reader[idx].term();
//
//		if (cv == null && cvOld == null) {
//			// if cv and cvOld are null, it's done
//			return false;
//		}
//
//		// cv == null일경우는 모든 reader가 종료되어 null이 된경우이며
//		// cvOld 와 cv 가 다른 경우는 머징시 텀이 바뀐경우. cvOld를 기록해야한다.
//		if ((cv == null || !cv.equals(cvOld)) && cvOld != null) {
//			// merge buffers
//			prevDocNo = -1;
//			totalCount = 0;
//			for (int k = 0; k < bufferCount; k++) {
//				BytesRef buf = buffers[k];
//				// buf.reset();
//
//				// count 와 lastNo를 읽어둔다.
//				int count = IOUtil.readInt(buf);
//				int lastDocNo = IOUtil.readInt(buf);
//				totalCount += count;
//				// logger.debug("count="+count);
//				if (k == 0) {
//					// 첫번째 문서번호부터 끝까지 기록한다.
//					tempPostingOutput.writeBytes(buf.array(), buf.pos(), buf.remaining());
//				} else {
//					int firstNo = IOUtil.readVInt(buf);
//					int newDocNo = firstNo - prevDocNo - 1;
////					logger.debug("newDocNo={}, firstNo={}, prevDocNo={}", newDocNo, firstNo, prevDocNo);
//
//					IOUtil.writeVInt(tempPostingOutput, newDocNo);
//					tempPostingOutput.writeBytes(buf.array(), buf.pos(), buf.remaining());
//				}
//				prevDocNo = lastDocNo;
//			}
//
//			termMade = true;
//			term.init(cvOld.array, cvOld.start, cvOld.length);
//
//			bufferCount = 0;
//
//		}
//
//		try {
//			buffers[bufferCount++] = reader[idx].buffer();
//		} catch (ArrayIndexOutOfBoundsException e) {
//			logger.info("### bufferCount= {}, buffers.len={}, idx={}, reader={}", bufferCount, buffers.length, idx, reader.length);
//			throw e;
//		}
//
//		// backup cv to old
//		cvOld = cv;
//
//		reader[idx].next();
//
//		heapify(1, flushCount);
//
//		if (termMade) {
//			return true;
//		}
//	} // while(true)
//
//}
//
//protected void makeHeap(int heapSize) {
//	heap = new int[heapSize + 1];
//	// index starts from 1
//	for (int i = 0; i < heapSize; i++) {
//		heap[i + 1] = i;
//	}
//
//	int n = heapSize >> 1; // last inner node index
//
//	for (int i = n; i > 0; i--) {
//		heapify(i, heapSize);
//	}
//
//}
//
//protected void heapify(int idx, int heapSize) {
//
//	int temp = -1;
//	int child = -1;
//
//	while (idx <= heapSize) {
//		int left = idx << 1;// *=2
//		int right = left + 1;
//
//		if (left <= heapSize) {
//			if (right <= heapSize) {
//				// 키워드가 동일할 경우 먼저 flush된 reader가 우선해야, docNo가 오름차순 정렬순서대로 올바로 기록됨.
//				// flush후 머징시 문제가 생기는 버그 해결됨 2013-5-21 swsong
//				int c = compareKey(left, right);
//				if (c < 0) {
//					child = left;
//				} else if (c > 0) {
//					child = right;
//				} else {
//					// 하위 value 둘이 같아서 seq확인.
//					// 같다면 id가 작은게 우선.
//					int a = heap[left];
//					int b = heap[right];
//					if (reader[a].sequence() < reader[b].sequence()) {
//						child = left;
//					} else {
//						child = right;
//					}
//				}
//			} else {
//				// if there is no right el.
//				child = left;
//			}
//		} else {
//			// no children
//			break;
//		}
//
//		// compare and swap
//		int c = compareKey(child, idx);
//		if (c < 0) {
//			temp = heap[child];
//			heap[child] = heap[idx];
//			heap[idx] = temp;
//			idx = child;
//			// System.out.println("idx1="+idx);
//		} else if (c == 0) {
//			// 하위와 자신의 value가 같아서 seq확인
//			// 같다면 seq가 작은게 우선.
//			int a = heap[idx];
//			int b = heap[child];
//			if (reader[a].sequence() > reader[b].sequence()) {
//				// 하위의 seq가 작아서 child채택!
//				temp = heap[child];
//				heap[child] = heap[idx];
//				heap[idx] = temp;
//				idx = child;
//			} else {
//				// 내것을 그대로 사용.
//				// sorted
//				break;
//			}
//		} else {
//			// sorted, then do not check child
//			break;
//		}
//
//	}
//}
//
//protected int compareKey(int one, int another) {
//
//	int a = heap[one];
//	int b = heap[another];
//
//	return compareKey(reader[a].term(), reader[b].term());
//}
}
