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

package org.fastcatsearch.ir.search;

import java.io.File;
import java.io.IOException;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IRFileName;
import org.fastcatsearch.ir.group.GroupHit;
import org.fastcatsearch.ir.io.BitSet;
import org.fastcatsearch.ir.query.ClauseException;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.CloseableThreadLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton, Thread-safe 하다. 각 세그먼트에서 검색을 하고 필터링, 그룹핑을 수행한 결과 문서리스트를 리턴한다.
 * 
 * 각 세그먼트에서는 Hit문서번호, 랭크데이터를 제공해주어야 한다.
 * 
 * @author sangwook.song
 * 
 */
public class SegmentSearcher {

	private static Logger logger = LoggerFactory.getLogger(SegmentSearcher.class);

	private Schema schema;
	private SearchIndexesReader searchIndexesReader;
	private FieldIndexesReader fieldIndexesReader;
	private GroupIndexesReader groupIndexesReader;
	private BitSet deleteSet;
	private int baseDocNo;
	private int docCount;

	final CloseableThreadLocal<SearchIndexesReader> searchIndexesReaderLocal = new CloseableThreadLocal<SearchIndexesReader>() {
		@Override
		protected SearchIndexesReader initialValue() {
			return searchIndexesReader.clone();
		}
	};
	
	final CloseableThreadLocal<FieldIndexesReader> fieldIndexesReaderLocal = new CloseableThreadLocal<FieldIndexesReader>() {
		@Override
		protected FieldIndexesReader initialValue() {
			return fieldIndexesReader.clone();
		}
	};
	
	final CloseableThreadLocal<GroupIndexesReader> groupIndexesReaderLocal = new CloseableThreadLocal<GroupIndexesReader>() {
		@Override
		protected GroupIndexesReader initialValue() {
			return groupIndexesReader.clone();
		}
	};
	
	public SegmentSearcher(Schema schema, File segmentHome, int baseDocNo, int docCount) throws IOException, IRException {
		this(schema, segmentHome, baseDocNo, docCount, new BitSet(0), 0);
	}

	public SegmentSearcher(Schema schema, File segmentHome, int baseDocNo, int docCount, int revision) throws IOException, IRException {
		this(schema, segmentHome, baseDocNo, docCount, null, revision);
	}

	public SegmentSearcher(Schema schema, File segmentHome, int baseDocNo, int docCount, BitSet bitset, int revision) throws IOException, IRException {
		this.schema = schema;
		this.baseDocNo = baseDocNo;
		this.docCount = docCount;

		// reader들은 thread-safe하지 않다. clone해서 사용됨.
		this.searchIndexesReader = new SearchIndexesReader(schema, segmentHome, revision);
		
		//field index
		this.fieldIndexesReader = new FieldIndexesReader(schema, segmentHome);
		
//		// group index
		this.groupIndexesReader = new GroupIndexesReader(schema, segmentHome, revision);

		if (bitset != null) {
			deleteSet = bitset;
		} else {
			deleteSet = new BitSet(IRFileName.getRevisionDir(segmentHome, revision), IRFileName.docDeleteSet);
		}
	}

	public Hit search(Query q) throws ClauseException, IOException, IRException {
		return new Hit(q, schema, baseDocNo, docCount, searchIndexesReaderLocal.get(), fieldIndexesReaderLocal.get(), groupIndexesReaderLocal.get(), deleteSet);
	}

	public GroupHit doGrouping(Query q) throws ClauseException, IOException, IRException {
		return new GroupHit(q, schema, docCount, searchIndexesReaderLocal.get(), fieldIndexesReaderLocal.get(), groupIndexesReaderLocal.get(), deleteSet);
	}

	public void setDeleteSet(BitSet deleteSet) {
		this.deleteSet = deleteSet;
	}

	public void close() throws IOException {
		searchIndexesReader.close();
		fieldIndexesReader.close();
		groupIndexesReader.close();
	}
}
