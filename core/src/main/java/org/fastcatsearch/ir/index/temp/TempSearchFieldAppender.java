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

package org.fastcatsearch.ir.index.temp;

import java.io.File;
import java.io.IOException;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.index.IndexFieldOption;
import org.fastcatsearch.ir.io.BufferedFileInput;
import org.fastcatsearch.ir.io.BufferedFileOutput;
import org.fastcatsearch.ir.io.BytesDataOutput;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.io.IOUtil;
import org.fastcatsearch.ir.io.IndexInput;
import org.fastcatsearch.ir.io.IndexOutput;

public class TempSearchFieldAppender extends TempSearchFieldMerger {

	private CharVector cv;
	private CharVector cvOld;
	private BytesDataOutput tempPostingOutput;
	private BytesBuffer[] buffers;

	private int totalCount;
	private int prevDocNo;
	private byte[] buffer = new byte[1024 * 1024];
	private int bufferCount = 0;

	public TempSearchFieldAppender(String indexId, int flushCount, long[] flushPosition, File tempFile) throws IOException {
		super(indexId, flushCount, flushPosition, tempFile);

		tempPostingOutput = new BytesDataOutput(1024 * 1024);

		buffers = new BytesBuffer[flushCount];

		reader = new TempSearchFieldReader[flushCount];
		for (int m = 0; m < flushCount; m++) {
			reader[m] = new TempSearchFieldReader(m, indexId, tempFile, flushPosition[m]);
			reader[m].next();
		}

	}

	public boolean mergeAndAppendIndex(File segmentDir1, File targetDir, int indexInterval, IndexFieldOption fieldIndexOption) throws IOException,
			IRException {
		IndexInput lexiconInput1 = new BufferedFileInput(segmentDir1, IndexFileNames.getSuffixFileName(IndexFileNames.lexiconFile, indexId));
		IndexOutput lexiconOutput = new BufferedFileOutput(targetDir, IndexFileNames.getSuffixFileName(IndexFileNames.lexiconFile, indexId));

		IndexOutput indexOutput = new BufferedFileOutput(targetDir, IndexFileNames.getSuffixFileName(IndexFileNames.indexFile, indexId));

		IndexInput postingInput1 = new BufferedFileInput(segmentDir1, IndexFileNames.getSuffixFileName(IndexFileNames.postingFile, indexId));
		IndexOutput postingOutput = new BufferedFileOutput(targetDir, IndexFileNames.getSuffixFileName(IndexFileNames.postingFile, indexId));

		// 같은 텀이 있을때에 posting 문서번호를 다 읽어서 머징한다.
		// 같은 텀이 없다면 포스팅데이터를 뚝 떼어서 새로운 포스팅에 붙이면 된다.
		// 단지, 세그먼트 2의 문서번호는 동일한 수만 큼 증가했으므로 포스팅별 시작문서번호, lastDocNo가 조정되야 한다.(+seg2BaseDocNo)
		// indexOutput은 indexInterval번마다 기록해준다.

		try {

			//Posting 파일의 맨처음 int는 색인필드옵션이다.
			postingOutput.writeInt(fieldIndexOption.value());

//			prepareNextSearchField(i);
			makeHeap(flushCount);

			int termCount1 = lexiconInput1.readInt();
			bufferCount = 0;

			int termCount = 0;
			int indexTermCount = 0;
			long position = 0;

			long lexiconFileHeadPos = lexiconOutput.position();
			long indexFileHeadPos = indexOutput.position();

			lexiconOutput.writeInt(termCount);
			indexOutput.writeInt(indexTermCount);

			int cmp = 0;

			CharVector term1 = null;
			CharVector term2 = new CharVector();

			if (termCount1 > 0) {
				char[] t = lexiconInput1.readUString();
				term1 = new CharVector(t, 0, t.length);
				lexiconInput1.readLong();
			}
			termCount1--;

			boolean hasNext = readNextIndex(term2);

			CharVector term = null;

			while (termCount1 >= 0 || hasNext) {
				// logger.debug("CMP = "+new String(term1.array, term1.start, term1.length)+":"+new String(term2.array,
				// term2.start, term2.length));
				if (termCount1 >= 0 && hasNext)
					cmp = compareKey(term1, term2);
				else if (termCount1 < 0) // input1이 다 소진되었으면 input2를 읽도록 유도. 크기가 작은 걸 읽는다.
					cmp = 1;
				else if (!hasNext)
					cmp = -1;

				long pointer = lexiconOutput.position();

				if (cmp <= 0) {
					term = term1;
				} else {
					term = term2;
				}

				if (cmp == 0) {

					// posting merge.
					int len1 = postingInput1.readVInt();
					int len2 = (int) tempPostingOutput.position(); // only data length (exclude header)

					int count1 = postingInput1.readInt();
					int lastDocNo1 = postingInput1.readInt();

					int data1Length = (int) (len1 - IOUtil.SIZE_OF_INT * 2);
					if (data1Length > buffer.length) {
						buffer = new byte[data1Length];
					}

					postingInput1.readBytes(buffer, 0, data1Length);

					int count2 = totalCount;
					int lastDocNo2 = prevDocNo;
					int firstDocNo2 = IOUtil.readVInt(tempPostingOutput.array(), 0);
					int sz2 = IOUtil.lenVariableByte(firstDocNo2);
					int newFirstDocNo = firstDocNo2 - lastDocNo1 - 1;
					int delta = IOUtil.lenVariableByte(newFirstDocNo) - sz2;
					long newLen = len1 + len2 + delta;

					position = postingOutput.position();
					postingOutput.writeVLong(newLen);
					postingOutput.writeInt(count1 + count2);
					postingOutput.writeInt(lastDocNo2);
					postingOutput.writeBytes(buffer, 0, data1Length);

					len2 -= sz2;

					postingOutput.writeVInt(newFirstDocNo);
					postingOutput.writeBytes(tempPostingOutput.array(), sz2, len2);

					lexiconOutput.writeUString(term.array, term.start, term.length);
					lexiconOutput.writeLong(position);

					// read both term1, term2
					if (termCount1 > 0) {
						char[] t = lexiconInput1.readUString();
						term1 = new CharVector(t, 0, t.length);
						lexiconInput1.readLong();
					}
					termCount1--;

					hasNext = readNextIndex(term2);

				} else if (cmp < 0) {

					int len = postingInput1.readVInt();
					if (len > buffer.length)
						buffer = new byte[len];
					if (len < 8)
						throw new IOException("Terrible Error!!");
					// logger.debug("len1 = "+len);
					postingInput1.readBytes(buffer, 0, len);

					position = postingOutput.position();

					// write posting
					postingOutput.writeVInt(len);
					postingOutput.writeBytes(buffer, 0, len);

					// write lexicon
					lexiconOutput.writeUString(term.array, term.start, term.length);
					lexiconOutput.writeLong(position);

					if (termCount1 > 0) {
						char[] t = lexiconInput1.readUString();
						term1 = new CharVector(t, 0, t.length);
						lexiconInput1.readLong();
					}
					termCount1--;

				} else {

					int len = (int) tempPostingOutput.position();
					// logger.debug("len2 ="+len);
					int count = totalCount;
					int lastDocNo = prevDocNo;
					int firstDocNo = IOUtil.readVInt(tempPostingOutput.array(), 0);
					int sz = IOUtil.lenVariableByte(firstDocNo);

					len -= sz;

					int sz2 = IOUtil.lenVariableByte(firstDocNo);

					position = postingOutput.position();

					int len2 = IOUtil.SIZE_OF_INT * 2 + sz2 + len;
					if (len2 < 8)
						throw new IOException("Terrible Error!! " + len2);
					postingOutput.writeVInt(len2);
					postingOutput.writeInt(count);
					postingOutput.writeInt(lastDocNo);
					postingOutput.writeVInt(firstDocNo);
					postingOutput.writeBytes(tempPostingOutput.array(), sz, len);

					// write term
					lexiconOutput.writeUString(term.array, term.start, term.length);
					lexiconOutput.writeLong(position);

					hasNext = readNextIndex(term2);

				}

				if (indexInterval > 0 && (termCount % indexInterval) == 0) {
					indexOutput.writeUString(term.array, term.start, term.length);
					indexOutput.writeLong(pointer);
					indexOutput.writeLong(position);
					indexTermCount++;
				}
				termCount++;
			}// while

//			logger.debug("## indexTermCount = " + indexTermCount);
//			long prevPos = lexiconOutput.position();
//			lexiconOutput.seek(lexiconFileHeadPos);
//			lexiconOutput.writeInt(termCount);
//			logger.debug("WRITE LEXICON COUNT = {} at {}", termCount, lexiconFileHeadPos);
//			lexiconOutput.seek(prevPos);

			if (termCount > 0) {
				lexiconOutput.seek(0);
				lexiconOutput.writeInt(termCount);
				indexOutput.seek(0);
				indexOutput.writeInt(indexTermCount);
//				prevPos = indexOutput.position();
//				indexOutput.seek(indexFileHeadPos);
//				indexOutput.writeInt(indexTermCount);
//				indexOutput.seek(prevPos);
			} else {
//				long pointer = lexiconOutput.position();
//				indexOutput.writeLong(pointer);
			}
			logger.debug("## write index [{}] termCount[{}] indexTermCount[{}] indexInterval[{}]", indexId, termCount, indexTermCount, indexInterval);
			
			lexiconOutput.flush();
			indexOutput.flush();
			postingOutput.flush();

		} finally {
			IOException exception = null;
			try {
				if (lexiconInput1 != null) {
					lexiconInput1.close();
				}
			} catch (IOException e) {
				exception = e;
			}
			try {
				if (postingInput1 != null) {
					postingInput1.close();
				}
			} catch (IOException e) {
				exception = e;
			}
			try {
				if (postingOutput != null) {
					postingOutput.close();
				}
			} catch (IOException e) {
				exception = e;
			}
			try {
				if (lexiconOutput != null) {
					lexiconOutput.close();
				}
			} catch (IOException e) {
				exception = e;
			}
			try {
				if (indexOutput != null) {
					indexOutput.close();
				}
			} catch (IOException e) {
				exception = e;
			}

			if (exception != null) {
				throw exception;
			}
		}

		return true;
	}

	private boolean readNextIndex(CharVector term) throws IOException {
		tempPostingOutput.reset();
		boolean termMade = false;

		// int kk = 0;
		while (true) {
			int idx = heap[1];
			cv = reader[idx].term();

			if (cv == null && cvOld == null) {
				// if cv and cvOld are null, it's done
				return false;
			}

			// cv == null일경우는 모든 reader가 종료되어 null이 된경우이며
			// cvOld 와 cv 가 다른 경우는 머징시 텀이 바뀐경우. cvOld를 기록해야한다.
			if ((cv == null || !cv.equals(cvOld)) && cvOld != null) {
				// merge buffers
				prevDocNo = -1;
				totalCount = 0;
				for (int k = 0; k < bufferCount; k++) {
					BytesBuffer buf = buffers[k];
					buf.flip();

					// count 와 lastNo를 읽어둔다.
					int count = IOUtil.readInt(buf);
					int lastDocNo = IOUtil.readInt(buf);
					totalCount += count;
					// logger.debug("count="+count);
					if (k == 0) {
						// 첫번째 문서번호부터 끝까지 기록한다.
						tempPostingOutput.writeBytes(buf.array(), buf.pos(), buf.remaining());
					} else {
						int firstNo = IOUtil.readVInt(buf);
						int newDocNo = firstNo - prevDocNo - 1;
						logger.debug("newDocNo = " + newDocNo + ", firstNo=" + firstNo + ", prevDocNo = " + prevDocNo);

						IOUtil.writeVInt(tempPostingOutput, newDocNo);
						tempPostingOutput.writeBytes(buf.array(), buf.pos(), buf.remaining());
					}
					prevDocNo = lastDocNo;
				}

				termMade = true;
				term.init(cvOld.array, cvOld.start, cvOld.length);

				bufferCount = 0;

			}

			try {
				buffers[bufferCount++] = reader[idx].buffer();
			} catch (ArrayIndexOutOfBoundsException e) {
				logger.info("### bufferCount= {}, buffers.len={}, idx={}, reader={}", bufferCount, buffers.length, idx, reader.length);
				throw e;
			}

			// backup cv to old
			cvOld = cv;

			reader[idx].next();

			heapify(1, flushCount);

			if (termMade)
				return true;

		} // while(true)

	}

}
