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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.document.DocumentWriter;
import org.fastcatsearch.ir.document.PrimaryKeyIndexesWriter;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Formatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SegmentWriter {

	private static Logger logger = LoggerFactory.getLogger(SegmentWriter.class);

	private int lastDocNo;
	private int count;
	private boolean requestStop;
	private long startTime;
	
	private int baseDocNo;
	private DocumentWriter documentWriter;
	private PrimaryKeyIndexesWriter primaryKeyIndexesWriter;
	private SearchIndexesWriter searchIndexesWriter;
	private FieldIndexesWriter fieldIndexesWriter;
	private GroupIndexesWriter groupIndexesWriter;
	
	private String segmentId;
	private File targetDir;
	private final int REVISION = 0;


	// usually this constructor is used, except for certain segment rebuild
	// for Full indexing
	public SegmentWriter(Schema schema, File targetDir, IndexConfig indexConfig) throws IRException {
		this(schema, targetDir, 0, 0, indexConfig);
	}

	// for Add indexing
	public SegmentWriter(Schema schema, File targetDir, int baseDocNo, int revision, IndexConfig indexConfig) throws IRException {
		init(schema, targetDir, baseDocNo, revision, indexConfig);
	}

	public void init(Schema schema, File targetDir, int baseDocNo, int revision, IndexConfig indexConfig) throws IRException {
		try {
			this.segmentId = targetDir.getName();
			this.targetDir = targetDir;
			this.baseDocNo = baseDocNo;
			// make a default 0 revision directory
			IndexFileNames.getRevisionDir(targetDir, REVISION).mkdirs();
			boolean isAppend = false;
			if (revision > 0) {
				isAppend = true;
			}

			documentWriter = new DocumentWriter(schema, targetDir, revision, indexConfig);
			primaryKeyIndexesWriter = new PrimaryKeyIndexesWriter(schema, targetDir, revision, indexConfig);
			searchIndexesWriter = new SearchIndexesWriter(schema, targetDir, revision, indexConfig);
			fieldIndexesWriter = new FieldIndexesWriter(schema, targetDir, isAppend);
			groupIndexesWriter = new GroupIndexesWriter(schema, targetDir, revision, indexConfig);
		} catch (IOException e) {
			// writer생성시 에러가 발생하면(ex 토크나이저 발견못함) writer들이 안 닫힌채로 색인이 끝나서 다음번 색인시 파일들을 삭제못하게 되므로 close해준다.
			try {
				closeWriter();
			} catch (Exception ignore) {
				// ignore
			}
			throw new IRException(e);
		}

	}

	public int getDocumentCount() {
		return count;
	}

	public int addDocument(Document document) throws IRException, IOException {
		logger.debug("doc >> {}", document);
		int docNo = documentWriter.write(document);
		primaryKeyIndexesWriter.write(document, docNo);
		searchIndexesWriter.write(document);
		fieldIndexesWriter.write(document);
		groupIndexesWriter.write(document);
		
		lastDocNo = docNo;
		count++;
		return docNo;
	}

	// 색인중에 문서번호가 같은 데이터가 존재할 경우 내부적으로 삭제처리된다.
	// 이 갯수를 색인결과의 삭제문서 갯수에 더해줘야 전체적인 문서수가 일치하게 된다.
	public int getDuplicateDocCount() {
		return primaryKeyIndexesWriter.getUpdateDocCount();
	}

	private void closeWriter() throws Exception {
		boolean errorOccured = false;
		Exception exception = null;
		try {
			documentWriter.close();
		} catch (Exception e) {
			logger.error("문서색인에러", e);
			exception = e;
			errorOccured = true;
		}
		try {
			primaryKeyIndexesWriter.close();
		} catch (Exception e) {
			logger.error("PK색인에러", e);
			exception = e;
			errorOccured = true;
		}
		try {
			searchIndexesWriter.close();
		} catch (Exception e) {
			logger.error("검색필드 색인에러", e);
			exception = e;
			errorOccured = true;
		}
		try {
			fieldIndexesWriter.close();
		} catch (Exception e) {
			logger.error("필드색인필드 색인에러", e);
			exception = e;
			errorOccured = true;
		}
		try {
			groupIndexesWriter.close();
		} catch (Exception e) {
			logger.error("그룹색인필드 색인에러", e);
			exception = e;
			errorOccured = true;
		}

		if (errorOccured) {
			throw exception;
		}
	}

	public SegmentInfo close() throws IOException, IRException {
		try {
			closeWriter();
			SegmentInfo segmentInfo = new SegmentInfo(segmentId, baseDocNo);
			segmentInfo.update(REVISION, lastDocNo, primaryKeyIndexesWriter.getUpdateDocCount(), 0, Formatter.formatDate());
			logger.info(
					"Total {} documents indexed, elapsed = {}, mem = {}",
					new Object[] { lastDocNo, Formatter.getFormatTime(System.currentTimeMillis() - startTime),
							Formatter.getFormatSize(Runtime.getRuntime().totalMemory()) });

			return segmentInfo;
		} catch (Exception e) {
			File revisionDir = IndexFileNames.getRevisionDir(targetDir, REVISION);
			FileUtils.deleteDirectory(revisionDir);
			throw new IRException(e);
		}
		//
		// 문서가 0건일 경우 새로생성한 리비전 디렉토리를 삭제하고
		// SegmentInfo를 업데이트 하지 않는다.
		//
		// if(lastDocNo == 0){
		// File revisionDir = IRFileName.getRevisionDir(targetDir, REVISION);
		// FileUtils.deleteDirectory(revisionDir);
		// return;
		// }

	}

}
