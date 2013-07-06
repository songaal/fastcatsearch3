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

package org.fastcatsearch.ir.index;

import java.io.CharArrayReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.CharsRef;
import org.fastcatsearch.ir.analysis.AnalyzerPool;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IRFileName;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.index.temp.TempSearchFieldAppender;
import org.fastcatsearch.ir.index.temp.TempSearchFieldMerger;
import org.fastcatsearch.ir.io.BufferedFileOutput;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.Counter;
import org.fastcatsearch.ir.io.IndexOutput;
import org.fastcatsearch.ir.settings.IndexSetting;
import org.fastcatsearch.ir.settings.RefSetting;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Formatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchIndexesWriter {
	private static Logger logger = LoggerFactory.getLogger(SearchIndexesWriter.class);

	private List<IndexSetting> indexSettingList;

	private MemoryPosting[] memoryPosting;
	// private boolean[] isNumericField;
	private IndexFieldOption[] fieldIndexOptions;
	private HashMap<CharVector, Counter> collector;
	private AnalyzerPool[] analyzerPools;
	private Analyzer[] analyzerList;
	// private CharVector token1 = new CharVector();
	private File baseDir;
	// inmemory lexicon ratio = 1/indexInterval
	private int indexInterval;
	private int indexBucketSize;

	// limit memory use. if exeed this value, flush.
	private long workMemoryLimit;
	private int workMemoryCheck;

	private File tempFile;
	private IndexOutput tempOutput;
	private long[] flushPosition; // each flush file position
	private int flushCount;
	private int count;
	private boolean isAppend;
	private int revision;
	private int[][] indexFieldSequence;
	private int[] positionIncrementGapList;

	public SearchIndexesWriter(Schema schema, File dir, IndexConfig indexConfig) throws IOException, IRException {
		this(schema, dir, 0, indexConfig);
	}

	public SearchIndexesWriter(Schema schema, File dir, int revision, IndexConfig indexConfig) throws IOException, IRException {
		this.indexSettingList = schema.schemaSetting().getIndexSettingList();
		int indexSettingSize = indexSettingList.size();
		this.baseDir = dir;
		this.revision = revision;
		if (revision > 0)
			isAppend = true;

		indexInterval = indexConfig.getIndexTermInterval();// irconfig.getInt("index.term.interval");
		indexBucketSize = indexConfig.getIndexWorkBucketSize();// irconfig.getByteSize("index.work.bucket.size");
		workMemoryLimit = indexConfig.getIndexWorkMemorySize();// irconfig.getByteSize("index.work.memory");
		workMemoryCheck = 10000; // 10000번에 한번씩 메모리 체크.
		analyzerPools = new AnalyzerPool[indexSettingSize];
		analyzerList = new Analyzer[indexSettingSize];
		memoryPosting = new MemoryPosting[indexSettingSize];
		fieldIndexOptions = new IndexFieldOption[indexSettingSize];

		indexFieldSequence = new int[indexSettingSize][];
		positionIncrementGapList = new int[indexSettingSize];

		for (int i = 0; i < indexSettingSize; i++) {
			IndexSetting is = indexSettingList.get(i);
			String indexAnalyzer = is.getIndexAnalyzer();
			analyzerPools[i] = schema.getAnalyzerPool(indexAnalyzer);

			analyzerList[i] = analyzerPools[i].getFromPool();

			fieldIndexOptions[i] = new IndexFieldOption();
			if (is.isStorePosition()) {
				memoryPosting[i] = new MemoryPostingWithPosition(indexBucketSize);
				fieldIndexOptions[i].setStorePosition();
			} else {
				memoryPosting[i] = new MemoryPosting(indexBucketSize);
			}

			List<RefSetting> refList = is.getFieldList();
			indexFieldSequence[i] = new int[refList.size()];
			int cursor = 0;
			for (RefSetting refSetting : refList) {
				String fieldId = refSetting.getRef();
				indexFieldSequence[i][cursor++] = schema.getFieldSequence(fieldId);
			}

			positionIncrementGapList[i] = is.getPositionIncrementGap();

		}

		collector = new HashMap<CharVector, Counter>();
		flushPosition = new long[1024];
		flushCount = 0;

		tempFile = new File(dir, IRFileName.tempFile);
		tempOutput = new BufferedFileOutput(tempFile, false);
	}

	public void write(Document doc) throws IRException, IOException {
		write(doc, count++);
	}

	public void write(Document doc, int docNo) throws IRException, IOException {

		for (int i = 0; i < indexSettingList.size(); i++) {
			int[] sequenceList = indexFieldSequence[i];
			int positionIncrementGap = positionIncrementGapList[i];
			for (int sequence : sequenceList) {
				write(i, count, doc.get(sequence), analyzerList[i], positionIncrementGap);
				// positionIncrementGap은 필드가 증가할때마다 동일량으로 증가. 예) 0, 100, 200, 300...
				positionIncrementGap += positionIncrementGap;
			}
		}

		if (count % workMemoryCheck == 0) {
			int workingMemorySize = checkWorkingMemorySize();
			logger.debug("SearchField Memory = {}, limit = {}", Formatter.getFormatSize(workingMemorySize), Formatter.getFormatSize(workMemoryLimit));
			if (workingMemorySize > workMemoryLimit) {
				logger.info("write memory used = {}", workingMemorySize);
				flush();
			}
		}

		count++;
	}

	private void write(int indexFieldNum, int docNo, Field field, Analyzer analyzer, int positionIncrementGap) throws IRException, IOException {
		if (field == null) {
			return;
		}
		collector.clear();

		// 같은문서에 indexFieldNum가 중복되어서 들어오면 multi-field-index로 처리한다.
		if (field.isMultiValue()) {
			Iterator<Object> iterator = field.getValueIterator();
			while (iterator.hasNext()) {
				indexValue(indexFieldNum, docNo, iterator.next(), positionIncrementGap);
				// 멀티밸류도 positionIncrementGap을 증가시킨다. 즉, 필드가 다를때처럼 position거리가 멀어진다.
				positionIncrementGap += positionIncrementGap;
			}
		} else {
			indexValue(indexFieldNum, docNo, field.getValue(), positionIncrementGap);
		}
	}

	private void indexValue(int indexFieldNum, int docNo, Object value, int positionIncrementGap) throws IOException, IRException {
		char[] fieldValue = value.toString().toCharArray();
		TokenStream tokenStream = analyzerList[indexFieldNum].tokenStream(Integer.toString(indexFieldNum), new CharArrayReader(fieldValue));
		tokenStream.reset();
		CharsRefTermAttribute termAttribute = null;
		PositionIncrementAttribute positionAttribute = null;
		if (tokenStream.hasAttribute(CharsRefTermAttribute.class)) {
			termAttribute = tokenStream.getAttribute(CharsRefTermAttribute.class);
		}
		if (tokenStream.hasAttribute(PositionIncrementAttribute.class)) {
			positionAttribute = tokenStream.getAttribute(PositionIncrementAttribute.class);
		}
		CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);

		while (tokenStream.incrementToken()) {
			CharVector key = null;
			if (termAttribute != null) {
				CharsRef charRef = termAttribute.charsRef();
				char[] buffer = new char[charRef.length()];
				System.arraycopy(charRef.chars, charRef.offset, buffer, 0, charRef.length);
				key = new CharVector(buffer, 0, buffer.length);
			} else {
				key = new CharVector(charTermAttribute.buffer(), 0, charTermAttribute.length());
			}
			// 영문 토크나이저 사용시 스테밍된 결과가 소문자로 반환되어 다시한번 key를 uppercase로 변환필요.
			key.toUpperCase();
			int position = -1;
			if (positionAttribute != null) {
				position = positionAttribute.getPositionIncrement() + positionIncrementGap;
			}
			// logger.debug("FIELD#{}: {} >> {} ({})", indexFieldNum, key, docNo, position);
			memoryPosting[indexFieldNum].add(key, docNo, position);
		}
	}

	private int checkWorkingMemorySize() {
		int totalMemorySize = 0;
		for (int i = 0; i < memoryPosting.length; i++) {
			totalMemorySize += memoryPosting[i].workingMemorySize();
			logger.debug("mem-{} = {}, cnt = {}", i, Formatter.getFormatSize(memoryPosting[i].staticMemorySize()), memoryPosting[i].count());
		}
		return totalMemorySize;
	}

	private int checkStaticMemorySize() {
		int totalMemorySize = 0;
		for (int i = 0; i < memoryPosting.length; i++) {
			totalMemorySize += memoryPosting[i].staticMemorySize();
			logger.debug("mem-{} = {}, cnt = {}", i, Formatter.getFormatSize(memoryPosting[i].staticMemorySize()), memoryPosting[i].count());
		}
		return totalMemorySize;
	}

	private int checkTotalCount() {
		int totalCount = 0;
		for (int i = 0; i < memoryPosting.length; i++) {
			totalCount += memoryPosting[i].count();
		}
		return totalCount;
	}

	private long writeMemoryPosting(IndexOutput output) throws IOException {
		int i = 0;
		logger.debug("## Save index-{} storepos = {}", i, fieldIndexOptions[i].isStorePosition());
		long pos = memoryPosting[i].save(output);
		for (i = 1; i < memoryPosting.length; i++) {
			logger.debug("## Save index-{} storepos = {}", i, fieldIndexOptions[i].isStorePosition());
			memoryPosting[i].save(output);
		}
		return pos;
	}

	private void clearMemoryPosting() throws IOException {
		for (int i = 0; i < memoryPosting.length; i++) {
			memoryPosting[i].clear();
			// if(fieldIndexOptions[i].isStorePosition()){
			// memoryPosting[i] = new MemoryPostingWithPosition(indexBucketSize);
			// }else{
			// memoryPosting[i] = new MemoryPosting(indexBucketSize);
			// }
		}
	}

	public void flush() throws IRException {
		if (count <= 0) {
			return;
		}
		if (indexSettingList.size() <= 0) {
			logger.warn("스키마에 색인필드가 없습니다.");
			return;
		}

		logger.info("flush...{}", count);

		try {
			// flush postion array 증가.
			if (flushPosition.length == flushCount) {
				long[] newFlushPosition = new long[flushPosition.length * 2];
				logger.info("Widen flush position array capacity >> {}", newFlushPosition.length);
				System.arraycopy(flushPosition, 0, newFlushPosition, 0, flushPosition.length);
				flushPosition = newFlushPosition;
			}
			flushPosition[flushCount] = writeMemoryPosting(tempOutput);
			// ensure every data wrote on disk!
			tempOutput.flush();

			flushCount++;

			clearMemoryPosting();
		} catch (IOException e) {
			throw new IRException(e);
		}
	}

	public void close() throws IRException, IOException {
		logger.debug("Final Flush");

		// Analyzer 리턴.
		for (int i = 0; i < indexSettingList.size(); i++) {
			analyzerPools[i].releaseToPool(analyzerList[i]);
		}

		try {
			flush();
		} finally {
			tempOutput.close();
		}

		try {
			if (count > 0 && indexSettingList.size() > 0) {
				logger.debug("Close, flushCount={}", flushCount);

				for (int i = 0; i < flushCount; i++) {
					logger.debug("flush position-{} : {}", i, flushPosition[i]);
				}

				if (isAppend) {
					File prevAppendDir = IRFileName.getRevisionDir(baseDir, revision - 1);
					File revisionDir = IRFileName.getRevisionDir(baseDir, revision);

					TempSearchFieldAppender appender = new TempSearchFieldAppender(flushCount, flushPosition, tempFile);
					// no need baseDocNo param. it;s already added.., docWriter appending job makes doc number follows prev doc
					// number
					try {
						appender.mergeAndAppendIndex(prevAppendDir, revisionDir, indexInterval, fieldIndexOptions);
					} finally {
						appender.close();
					}
				} else {
					TempSearchFieldMerger merger = new TempSearchFieldMerger(flushCount, flushPosition, tempFile);
					try {
						merger.mergeAndMakeIndex(baseDir, indexInterval, fieldIndexOptions);
					} finally {
						merger.close();
					}
				}
			}
		} finally {
			// delete temp file
			tempFile.delete();
		}
	}

}
