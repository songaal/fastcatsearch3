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
import java.util.List;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.index.IndexFieldOption;
import org.fastcatsearch.ir.io.BufferedFileInput;
import org.fastcatsearch.ir.io.BufferedFileOutput;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.IOUtil;
import org.fastcatsearch.ir.io.IndexInput;
import org.fastcatsearch.ir.io.IndexOutput;

public class TempSearchFieldAppender extends TempSearchFieldMerger {

	private byte[] buffer = new byte[1024 * 1024];

	private int oldIndexTermCount;
	
	public TempSearchFieldAppender(String indexId, List<Long> flushPosition, File tempFile) throws IOException {
		super(indexId, flushPosition, tempFile);
	}

	public boolean mergeAndAppendIndex(File segmentDir1, File targetDir, int indexInterval, IndexFieldOption fieldIndexOption) throws IOException,
			IRException {
		IndexInput lexiconInput1 = new BufferedFileInput(segmentDir1, IndexFileNames.getSearchLexiconFileName(indexId));
		IndexOutput lexiconOutput = new BufferedFileOutput(targetDir, IndexFileNames.getSearchLexiconFileName(indexId));

		IndexOutput indexOutput = new BufferedFileOutput(targetDir, IndexFileNames.getSearchIndexFileName(indexId));

		IndexInput postingInput1 = new BufferedFileInput(segmentDir1, IndexFileNames.getSearchPostingFileName(indexId));
		IndexOutput postingOutput = new BufferedFileOutput(targetDir, IndexFileNames.getSearchPostingFileName(indexId));

		IndexFieldOption option1 = new IndexFieldOption(postingInput1.readInt());
		if(!fieldIndexOption.equals(option1)){
			throw new IRException("Cannot append indexes. Index option is the same. new="+fieldIndexOption.value() +", old="+ option1.value());
		}
		
		// 같은 텀이 있을때에 posting 문서번호를 다 읽어서 머징한다.
		// 같은 텀이 없다면 포스팅데이터를 뚝 떼어서 새로운 포스팅에 붙이면 된다.
		// 단지, 세그먼트 2의 문서번호는 동일한 수만 큼 증가했으므로 포스팅별 시작문서번호, lastDocNo가 조정되야 한다.(+seg2BaseDocNo)
		// indexOutput은 indexInterval번마다 기록해준다.

		try {

			//Posting 파일의 맨처음 int는 색인필드옵션이다.
			postingOutput.writeInt(fieldIndexOption.value());

			makeHeap(flushCount);

			oldIndexTermCount = lexiconInput1.readInt();

			int termCount = 0;
			int indexTermCount = 0;
			long position = 0;

			lexiconOutput.writeInt(termCount);
			indexOutput.writeInt(indexTermCount);

			int cmp = 0;

			CharVector term1 = readNextOldLexicon(lexiconInput1);
			CharVector term2 = new CharVector();

			boolean hasNext = readNextTempIndex(term2);

			CharVector term = null;

			while (oldIndexTermCount >= 0 || hasNext) {
				if (oldIndexTermCount >= 0 && hasNext)
					cmp = compareKey(term1, term2);
				else if (oldIndexTermCount < 0) // input1이 다 소진되었으면 input2를 읽도록 유도. 크기가 작은 걸 읽는다.
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
//					logger.debug("1term={} >> len1 = {}", term, len1);
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

					lexiconOutput.writeUString(term.array(), term.start(), term.length());
					lexiconOutput.writeLong(position);

					term1 = readNextOldLexicon(lexiconInput1);
					hasNext = readNextTempIndex(term2);

				} else if (cmp < 0) {

					int len = postingInput1.readVInt();
					if (len > buffer.length){
						buffer = new byte[len];
					}
					
					postingInput1.readBytes(buffer, 0, len);

					position = postingOutput.position();

					// write posting
					postingOutput.writeVInt(len);
					postingOutput.writeBytes(buffer, 0, len);

					// write lexicon
					lexiconOutput.writeUString(term.array(), term.start(), term.length());
					lexiconOutput.writeLong(position);

					term1 = readNextOldLexicon(lexiconInput1);

				} else {

					int len = (int) tempPostingOutput.position();
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
					lexiconOutput.writeUString(term.array(), term.start(), term.length());
					lexiconOutput.writeLong(position);

					hasNext = readNextTempIndex(term2);

				}

				if (indexInterval > 0 && (termCount % indexInterval) == 0) {
					indexOutput.writeUString(term.array(), term.start(), term.length());
					indexOutput.writeLong(pointer);
					indexOutput.writeLong(position);
					indexTermCount++;
				}
				termCount++;
			}// while

			if (termCount > 0) {
				lexiconOutput.seek(0);
				lexiconOutput.writeInt(termCount);
				indexOutput.seek(0);
				indexOutput.writeInt(indexTermCount);
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
	private CharVector readNextOldLexicon(IndexInput lexiconInput1) throws IOException {
		CharVector term = null;
		if (oldIndexTermCount > 0) {
			char[] t = lexiconInput1.readUString();
			term = new CharVector(t, 0, t.length);
			lexiconInput1.readLong();
		}
		oldIndexTermCount--;
		
		return term;
	}
	
}
