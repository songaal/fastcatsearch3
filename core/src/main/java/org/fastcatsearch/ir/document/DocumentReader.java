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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.apache.commons.io.input.BoundedInputStream;
import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.field.FieldDataParseException;
import org.fastcatsearch.ir.io.BufferedFileInput;
import org.fastcatsearch.ir.io.ByteRefArrayOutputStream;
import org.fastcatsearch.ir.io.BytesDataInput;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.IOUtil;
import org.fastcatsearch.ir.io.IndexInput;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 문서번호는 세그먼트마다 0부터 시작하는 번호로 read한다. baseNo와는 상관없는 내부문서번호.
 * */

public class DocumentReader implements Cloneable {
	private static Logger logger = LoggerFactory.getLogger(DocumentReader.class);

	private static final int INFLATE_BUFFER_INIT_SIZE = 20 * 1024;
	private List<FieldSetting> fields;
	private IndexInput docInput;
	private IndexInput positionInput;
	private ByteRefArrayOutputStream inflaterOutput;
	private byte[] workingBuffer;

	private int documentCount;
	private int lastDocNo = -1;
	private DataInput lastBai;
	private long positionLimit;

    private AtomicInteger referenceCount;

	public DocumentReader() {
	}

	public DocumentReader(SchemaSetting schemaSetting, File dir) throws IOException {
		fields = schemaSetting.getFieldSettingList();
		docInput = new BufferedFileInput(dir, IndexFileNames.docStored);
		positionInput = new BufferedFileInput(dir, IndexFileNames.docPosition);
		positionLimit = positionInput.length();
		documentCount = docInput.readInt();
		logger.debug("DocumentCount = {}", documentCount);

		inflaterOutput = new ByteRefArrayOutputStream(INFLATE_BUFFER_INIT_SIZE); // 자동 증가됨. 초기 20KB으로 내림. 예전에는 3MB였음.
		workingBuffer = new byte[1024];
        referenceCount = new AtomicInteger();
	}

    public int getReferenceCount() {
        return referenceCount.intValue();
    }

    public int getDocumentCount() {
		return documentCount;
	}

	// 내부 문서번호로 호출한다.
	public Document readDocument(int docNo) throws IOException {
		return readDocument(docNo, null);
	}
	public Document readIndexableDocument(int docNo) throws IOException {
		return readDocument(docNo, null, true);
	}
	public Document readDocument(int docNo, boolean[] fieldSelectOption) throws IOException {
		return readDocument(docNo, fieldSelectOption, false);
	}
	public Document readDocument(int docNo, boolean[] fieldSelectOption, boolean indexable) throws IOException {

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
			
			//2014-11-26 검색요청이 많아서 working 버퍼가 너무 빠르게 많이 생길경우 GC 되기전에 OOM 발생할수 있음.
			// Stream으로 바꾸어 해결.
			InflaterInputStream decompressInputStream = null;
			inflaterOutput.reset();
			int count = -1;
			try {
				BoundedInputStream boundedInputStream = new BoundedInputStream(docInput, len);
				boundedInputStream.setPropagateClose(false);//하위 docInput 를 닫지않는다.
				decompressInputStream = new InflaterInputStream(boundedInputStream, new Inflater(), 512);
				while ((count = decompressInputStream.read(workingBuffer)) != -1) {
					inflaterOutput.write(workingBuffer, 0, count);
				}
			} finally {
				decompressInputStream.close();
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
			if(f != null && indexable){
				String multiValueDelimiter = fs.getMultiValueDelimiter();
				try {
					f.parseIndexable(multiValueDelimiter);
				} catch (FieldDataParseException e) {
					throw new IOException(e);
				}
			}
//            logger.debug("{}>> {} : {}", i, f.getId(), f.rawString());
			document.add(f);
		}
		
		document.setDocId(docNo);

		return document;
	}

	@Override
	public DocumentReader clone() {
		DocumentReader reader = new DocumentReader();
		reader.fields = fields;
		reader.docInput = docInput.clone();
		reader.positionInput = positionInput.clone();
		reader.documentCount = documentCount;

		reader.inflaterOutput = new ByteRefArrayOutputStream(INFLATE_BUFFER_INIT_SIZE); // 자동 증가됨.
		reader.workingBuffer = new byte[1024];
		reader.positionLimit = positionLimit;
        reader.referenceCount = referenceCount;
        referenceCount.incrementAndGet();
		return reader;
	}

	public void close() throws IOException {
		docInput.close();
		positionInput.close();
        referenceCount.decrementAndGet();
	}
}
