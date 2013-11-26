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

package org.fastcatsearch.ir.document;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.io.BufferedFileInput;
import org.fastcatsearch.ir.io.BytesDataInput;
import org.fastcatsearch.ir.io.ByteRefArrayOutputStream;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.IOUtil;
import org.fastcatsearch.ir.io.IndexInput;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 문서번호는 세그먼트마다 0부터 시작하는 번호로 read한다. baseNo와는 상관없는 내부문서번호.
 * */

public class DocumentReader implements Cloneable {
	private static Logger logger = LoggerFactory.getLogger(DocumentReader.class);

	private List<FieldSetting> fields;
	private IndexInput docInput;
	private IndexInput positionInput;
	private ByteRefArrayOutputStream inflaterOutput;
	private byte[] workingBuffer;
	private byte[] docReadBuffer;

	private int baseDocNo;
	private int documentCount;
	private int lastDocNo = -1;
	private DataInput lastBai;
	private long positionLimit;
	
	public DocumentReader() {
	}

	public DocumentReader(Schema schema, File dir) throws IOException {
		this(schema, dir, 0);
	}

	public DocumentReader(Schema schema, File dir, int baseDocNo) throws IOException {
		this.baseDocNo = baseDocNo;
		fields = schema.schemaSetting().getFieldSettingList();
		docInput = new BufferedFileInput(dir, IndexFileNames.docStored);
		positionInput = new BufferedFileInput(dir, IndexFileNames.docPosition);
		positionLimit = positionInput.length();
		documentCount = docInput.readInt();
		logger.info("DocumentCount = {}", documentCount);

		inflaterOutput = new ByteRefArrayOutputStream(3 * 1024 * 1024); // 자동 증가됨.
		workingBuffer = new byte[1024];
		docReadBuffer = new byte[3 * 1024 * 1024];
	}

	public int getDocumentCount() {
		return documentCount;
	}

	public int getBaseNumber() {
		return baseDocNo;
	}
	
	// 내부 문서번호로 호출한다.
	public Document readDocument(int docNo) throws IOException {
		return readDocument(docNo, null);
	}

	public Document readDocument(int docNo, boolean[] fieldSelectOption) throws IOException {
		// if(docNo < baseDocNo) throw new
		// IOException("Request docNo cannot less than baseDocNo! docNo = "+docNo+", baseDocNo = "+baseDocNo);

		// baseDocNo만큼 빼서 세그먼트별 내부문서번호를 만든다.
		// docNo -= baseDocNo;

		DataInput bai = null;

		if (docNo != lastDocNo) {
			long positionOffset = docNo * IOUtil.SIZE_OF_LONG;
			if(positionOffset >= positionLimit){
				//없는문서.
				return null;
			}
			positionInput.seek(positionOffset);
			long pos = positionInput.readLong();
			// find a document block
			docInput.seek(pos);
			int len = docInput.readInt();

			if (len > docReadBuffer.length) {
				docReadBuffer = new byte[len];
			}

			docInput.readBytes(docReadBuffer, 0, len);

			Inflater decompressor = new Inflater();

			inflaterOutput.reset();
			try {
				decompressor.setInput(docReadBuffer, 0, len);

				while (!decompressor.finished()) {
					int count = decompressor.inflate(workingBuffer);
					inflaterOutput.write(workingBuffer, 0, count);
				}

			} catch (DataFormatException e) {
				throw new IOException("DataFormatException");
			} finally {
				decompressor.end();
			}

			BytesRef bytesRef = inflaterOutput.getBytesRef();
			bai = new BytesDataInput(bytesRef.bytes, 0, bytesRef.length);

			lastDocNo = docNo;
			lastBai = bai;
		} else {
			lastBai.reset();
			bai = lastBai;
		}

		Document document = new Document(fields.size());
		for (int i = 0; i < fields.size(); i++) {
			FieldSetting fs = fields.get(i);
			Field f = null;
			boolean hasValue = bai.readBoolean();
//			logger.debug("read hasValue={}, select={}, fs={} ", hasValue, fieldSelectOption, fs);
			if (hasValue) {
				//1. fieldSelectOption 옵션이 없으면 모두 읽음.
				//2. 옵션이 존재한다면, true인 필드만을 읽는다.
				if(fieldSelectOption == null || (fieldSelectOption != null && fieldSelectOption[i])){
					f = fs.createEmptyField();
					f.readRawFrom(bai);
				}else{
					bai.skipVIntData();
				}
//				logger.debug("fill {} >> {}", i, f);
			}else{
				//값이 없는 필드도 빈 필드를 추가해준다.
				f = fs.createEmptyField();
//				logger.debug("fill {} >> empty", i);
			}
			
			document.set(i, f);
		}
		document.setDocId(docNo + baseDocNo);
		return document;
	}

	@Override
	public DocumentReader clone() {
		DocumentReader reader = new DocumentReader();
		reader.fields = fields;
		reader.docInput = docInput;
		reader.positionInput = positionInput;
		// reader.baseDocNo = baseDocNo;
		reader.documentCount = documentCount;

		reader.inflaterOutput = new ByteRefArrayOutputStream(3 * 1024 * 1024); // 자동 증가됨.
		reader.workingBuffer = new byte[1024];
		reader.docReadBuffer = new byte[3 * 1024 * 1024];
		reader.positionLimit = positionLimit;
		return reader;
	}

	public void close() throws IOException {
		docInput.close();
		positionInput.close();
	}
}
