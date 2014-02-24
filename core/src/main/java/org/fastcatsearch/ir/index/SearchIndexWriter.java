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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.CharsRef;
import org.fastcatsearch.ir.analysis.AnalyzerPool;
import org.fastcatsearch.ir.analysis.AnalyzerPoolManager;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.index.temp.TempSearchFieldAppender;
import org.fastcatsearch.ir.index.temp.TempSearchFieldMerger;
import org.fastcatsearch.ir.io.BufferedFileOutput;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.IndexOutput;
import org.fastcatsearch.ir.settings.IndexSetting;
import org.fastcatsearch.ir.settings.RefSetting;
import org.fastcatsearch.ir.settings.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchIndexWriter {
	private static Logger logger = LoggerFactory.getLogger(SearchIndexWriter.class);
	
	private static Logger testLogger = LoggerFactory.getLogger("TEST_LOG");
	
	private String indexId;
	private MemoryPosting memoryPosting;
	private IndexFieldOption fieldIndexOption;
	private AnalyzerPool analyzerPool;
	private Analyzer analyzer;
	private File baseDir;

	private boolean ignoreCase;
	private IndexConfig indexConfig;

	private File tempFile;
	private IndexOutput tempOutput;
	private List<Long> flushPosition; // each flush file position
	private int count;
	private int[] indexFieldSequence; // index내에 색인할 필드가 여러개일 경우 필드 번호.
	private int positionIncrementGap;

	private RevisionInfo revisionInfo;
	
	public SearchIndexWriter(IndexSetting indexSetting, Schema schema, File dir, RevisionInfo revisionInfo, IndexConfig indexConfig, AnalyzerPoolManager analyzerPoolManager) throws IOException,
			IRException {
		this.indexId = indexSetting.getId();
		this.baseDir = dir;
		this.revisionInfo = revisionInfo;
		this.indexConfig = indexConfig;
		
		ignoreCase = indexSetting.isIgnoreCase();
		int indexBucketSize = indexConfig.getIndexWorkBucketSize();

		String indexAnalyzerId = indexSetting.getIndexAnalyzer();
		analyzerPool = analyzerPoolManager.getPool(indexAnalyzerId);

		if (analyzerPool == null) {
			// 분석기 못찾음.
			throw new IRException("분석기를 찾을 수 없습니다. " + indexAnalyzerId);
		}

		analyzer = analyzerPool.getFromPool();

		fieldIndexOption = new IndexFieldOption();
		if (indexSetting.isStorePosition()) {
			memoryPosting = new MemoryPostingWithPosition(indexBucketSize);
			fieldIndexOption.setStorePosition();
		} else {
			memoryPosting = new MemoryPosting(indexBucketSize);
		}

		List<RefSetting> refList = indexSetting.getFieldList();
		indexFieldSequence = new int[refList.size()];
		int cursor = 0;
		for (RefSetting refSetting : refList) {
			String fieldId = refSetting.getRef();
			indexFieldSequence[cursor++] = schema.getFieldSequence(fieldId);
		}

		positionIncrementGap = indexSetting.getPositionIncrementGap();

		flushPosition = new ArrayList<Long>();

		tempFile = new File(dir, IndexFileNames.getSearchTempFileName(indexId));
		tempOutput = new BufferedFileOutput(tempFile, false);
	}

	public void write(Document doc) throws IRException, IOException {
		write(doc, count);
	}

	public void write(Document doc, int docNo) throws IRException, IOException {

		int[] sequenceList = indexFieldSequence;
		for (int sequence : sequenceList) {
			if(sequence < 0){
				continue;
			}
			write(docNo, doc.get(sequence), ignoreCase, positionIncrementGap);
			// positionIncrementGap은 필드가 증가할때마다 동일량으로 증가. 예) 0, 100, 200, 300...
			positionIncrementGap += positionIncrementGap;
		}

		count++;
	}

	private void write(int docNo, Field field, boolean upperCase, int positionIncrementGap) throws IRException, IOException {
		if (field == null) {
			return;
		}

		// 같은문서에 indexFieldNum가 중복되어서 들어오면 multi-field-index로 처리한다.
		if (field.isMultiValue()) {
			Iterator<Object> iterator = field.getMultiValueIterator();
			if (iterator != null) {
				while (iterator.hasNext()) {
					indexValue(docNo, iterator.next(), upperCase, positionIncrementGap);
					// 멀티밸류도 positionIncrementGap을 증가시킨다. 즉, 필드가 다를때처럼 position거리가 멀어진다.
					positionIncrementGap += positionIncrementGap;
				}
			}
		} else {
			indexValue(docNo, field.getValue(), upperCase, positionIncrementGap);
		}
	}

	private void indexValue(int docNo, Object value, boolean upperCase, int positionIncrementGap) throws IOException, IRException {
		if(value == null){
			return;
		}
		char[] fieldValue = value.toString().toCharArray();
		TokenStream tokenStream = analyzer.tokenStream(indexId, new CharArrayReader(fieldValue));
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
			
			if (upperCase) {
//				key.toUpperCase();
				key.setIgnoreCase();
			}
			int position = -1;
			if (positionAttribute != null) {
				position = positionAttribute.getPositionIncrement() + positionIncrementGap;
			}
			// logger.debug("FIELD#{}: {} >> {} ({})", indexFieldNum, key, docNo, position);
			
			memoryPosting.add(key, docNo, position);
		}
	}

	public int checkWorkingMemorySize() {
		return memoryPosting.workingMemorySize();
	}

	public int checkStaticMemorySize() {
		return memoryPosting.staticMemorySize();
	}

	public int checkTotalCount() {
		return memoryPosting.count();
	}

	public void flush() throws IRException {
		if (count <= 0) {
			return;
		}

		logger.info("[{}] Flush#{} [documents {}th..]", indexId, flushPosition.size() + 1, count);

		try {
			flushPosition.add(memoryPosting.save(tempOutput));
			// ensure every data wrote on disk!
			tempOutput.flush();

			memoryPosting.clear();
		} catch (IOException e) {
			throw new IRException(e);
		}
	}

	public void close() throws IRException, IOException {

		// Analyzer 리턴.
		analyzerPool.releaseToPool(analyzer);

		try {
			flush();
		} finally {
			tempOutput.close();
		}

		try {
			if (count > 0) {
				logger.debug("Close, flushCount={}", flushPosition.size());

				if (revisionInfo.isAppend()) {
					File prevAppendDir = IndexFileNames.getRevisionDir(baseDir, revisionInfo.getRef());
					File revisionDir = IndexFileNames.getRevisionDir(baseDir, revisionInfo.getId());
					TempSearchFieldAppender appender = new TempSearchFieldAppender(indexId, flushPosition, tempFile);
					try {
						appender.mergeAndAppendIndex(prevAppendDir, revisionDir, indexConfig.getIndexTermInterval(), fieldIndexOption);
					} finally {
						appender.close();
					}
				} else {
					TempSearchFieldMerger merger = new TempSearchFieldMerger(indexId, flushPosition, tempFile);
					try {
						merger.mergeAndMakeIndex(baseDir, indexConfig.getIndexTermInterval(), fieldIndexOption);
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
