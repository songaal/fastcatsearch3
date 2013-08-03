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
import java.util.Map;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.document.PrimaryKeyIndexReader;
import org.fastcatsearch.ir.document.PrimaryKeyIndexWriter;
import org.fastcatsearch.ir.document.merge.PrimaryKeyIndexMerger;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.field.FieldDataWriter;
import org.fastcatsearch.ir.io.BufferedFileOutput;
import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.io.BytesDataOutput;
import org.fastcatsearch.ir.io.FixedDataOutput;
import org.fastcatsearch.ir.io.IndexOutput;
import org.fastcatsearch.ir.io.SequencialDataOutput;
import org.fastcatsearch.ir.io.VariableDataOutput;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.GroupIndexSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 그룹필드는 가변길이필드허용. multi-value도 가변길이 가능.
 * 
 * @author sangwook.song
 * 
 */
public class GroupIndexWriter {
	private static Logger logger = LoggerFactory.getLogger(GroupIndexWriter.class);

	private String indexId;
	private IndexOutput groupIndexOutput;
	private IndexOutput multiValueOutput;

	private PrimaryKeyIndexWriter memoryKeyIndex;

	private int groupNumber;
	private SequencialDataOutput keyOutput;

	private int revision;
	private boolean isAppend;
	private File baseDir;
	private File revisionDir;
	private PrimaryKeyIndexReader prevPkReader; // 이전 pk reader 리스트. 증분색인시에 사용됨.
	private int indexInterval;
	private int count;
	private boolean isMultiValue;
	private boolean isIgnoreCase;
	
	private BytesDataOutput keyBuffer;

	private int fieldSequence;

	public GroupIndexWriter(GroupIndexSetting groupIndexSetting, Map<String, FieldSetting> fieldSettingMap, Map<String, Integer> fieldSequenceMap,
			File dir, IndexConfig indexConfig) throws IOException, IRException {
		this(groupIndexSetting, fieldSettingMap, fieldSequenceMap, dir, 0, indexConfig);
	}

	public GroupIndexWriter(GroupIndexSetting groupIndexSetting, Map<String, FieldSetting> fieldSettingMap, Map<String, Integer> fieldSequenceMap,
			File dir, int revision, IndexConfig indexConfig) throws IOException, IRException {
		this.revision = revision;
		if (revision > 0) {
			this.isAppend = true;
		}
		this.baseDir = dir;
		this.revisionDir = IndexFileNames.getRevisionDir(dir, revision);

		String id = groupIndexSetting.getId();
		this.indexId = id;

		groupIndexOutput = new BufferedFileOutput(dir, IndexFileNames.getGroupIndexFileName(id), isAppend);

		indexInterval = indexConfig.getPkTermInterval();
		int bucketSize = indexConfig.getPkBucketSize();

		String refFieldId = groupIndexSetting.getRef();
		fieldSequence = fieldSequenceMap.get(refFieldId);
		FieldSetting refFieldSetting = fieldSettingMap.get(refFieldId);
		isMultiValue = refFieldSetting.isMultiValue();
		isIgnoreCase = groupIndexSetting.isIgnoreCase();
		
		if (refFieldSetting.isVariableField()) {
			keyOutput = new VariableDataOutput(dir, IndexFileNames.getGroupKeyFileName(id), isAppend);
		} else {
			keyOutput = new FixedDataOutput(dir, IndexFileNames.getGroupKeyFileName(id), isAppend);
		}
		memoryKeyIndex = new PrimaryKeyIndexWriter(indexInterval, bucketSize);

		if (isMultiValue) {
			multiValueOutput = new BufferedFileOutput(dir, IndexFileNames.getMultiValueFileName(IndexFileNames.getGroupIndexFileName(id)), isAppend);
		}

		keyBuffer = new BytesDataOutput();
		
		if (isAppend) {
			// read previous pkmap
			File prevDir = IndexFileNames.getRevisionDir(dir, revision - 1);
			prevPkReader = new PrimaryKeyIndexReader(prevDir, IndexFileNames.getGroupKeyMapFileName(id));
			groupNumber = prevPkReader.count();
		}

	}

	public void write(Document document) throws IOException {

		Field field = document.get(fieldSequence);
		if (field == null) {
			if (isMultiValue) {
				groupIndexOutput.writeLong(-1L);
			} else {
				groupIndexOutput.writeInt(-1);
			}
		} else {
			
			int groupNo = -1;
			if (isMultiValue) {
				long ptr = multiValueOutput.position();
				FieldDataWriter writer = field.getDataWriter();
				writer.setUpperCase(isIgnoreCase);
				int multiValueCount = writer.count();

				if (multiValueCount > 0) {
					groupIndexOutput.writeLong(ptr);
					multiValueOutput.writeVInt(multiValueCount);
					keyBuffer.reset();
					while (writer.write(keyBuffer)) {
						BytesRef bytesRef = keyBuffer.bytesRef();
						groupNo = writeGroupKey(bytesRef);
						multiValueOutput.writeInt(groupNo);
						keyBuffer.reset();
					}
				} else {
					groupIndexOutput.writeLong(-1);
				}

			} else {
				keyBuffer.reset();
				field.writeDataTo(keyBuffer, isIgnoreCase);
				BytesRef bytesRef = keyBuffer.bytesRef();
				groupNo = writeGroupKey(bytesRef);
				groupIndexOutput.writeInt(groupNo);
			}
		}

		count++;
	}

	/*
	 * idx : 인덱스 내부필드 순차번호
	 */
	private int writeGroupKey(BytesBuffer keyBuffer) throws IOException {
		int groupNo = -1;
		if (isAppend) {
			// find key at previous append's pkmap
			groupNo = prevPkReader.get(keyBuffer);
			if (groupNo == -1) {
				groupNo = memoryKeyIndex.get(keyBuffer);
			}
		} else {
			groupNo = memoryKeyIndex.get(keyBuffer);
		}
		if (groupNo == -1) {
			groupNo = groupNumber++;
			// write key index
			memoryKeyIndex.put(keyBuffer, groupNo);
			keyOutput.write(keyBuffer);

//			String str = "";
//			for (int i = 0; i < keyBuffer.position(); i++) {
//				str += ((char)keyBuffer.array()[i] + ",");
//			}
//			logger.debug("write group key field [{}] size[{}] >> gr[{}]", str, keyBuffer.position(), groupNo);
		}
		return groupNo;
	}

	public void flush() throws IOException {
		groupIndexOutput.flush();
		if (isMultiValue) {
			multiValueOutput.flush();
		}
	}

	public void close() throws IOException {
		groupIndexOutput.close();
		keyOutput.close();
		
		if (isMultiValue) {
			multiValueOutput.close();
		}

		if (count <= 0) {
			if (isAppend) {
				prevPkReader.close();
			}
			return;
		}

		String pkFilename = IndexFileNames.getGroupKeyMapFileName(indexId);
		String pkIndexFilename = IndexFileNames.getIndexFileName(pkFilename);

		File tempPkFile = new File(revisionDir, IndexFileNames.getTempFileName(pkFilename));
		File tempPkIndexFile = new File(revisionDir, IndexFileNames.getTempFileName(pkIndexFilename));

		File pkFile = new File(revisionDir, pkFilename);
		File pkIndexFile = new File(revisionDir, pkIndexFilename);

		IndexOutput groupPkOutput = null;
		IndexOutput groupPkIndexOutput = null;
		
		if (isAppend) {
			// 머징을 위해 일단 TEMP파일로 생성한다.
			groupPkOutput = new BufferedFileOutput(tempPkFile);
			groupPkIndexOutput = new BufferedFileOutput(tempPkIndexFile);
		} else {
			groupPkOutput = new BufferedFileOutput(pkFile);
			groupPkIndexOutput = new BufferedFileOutput(pkIndexFile);
		}

		// int keyCount = memoryKeyIndex.count();
		// 별도 저장필요없음.나중에 pk에서 키 갯수읽으면 됨.
		
		memoryKeyIndex.setDestination(groupPkOutput, groupPkIndexOutput);
		memoryKeyIndex.write();
		groupPkOutput.close();
		groupPkIndexOutput.close();

		if (isAppend) {
			// 임시 map index파일 삭제.
			tempPkIndexFile.delete();
			prevPkReader.close();

			// pkindex merge
			File prevDir = IndexFileNames.getRevisionDir(baseDir, revision - 1);
			File prevPkFile = new File(prevDir, pkFilename);

			IndexOutput output = new BufferedFileOutput(pkFile);
			IndexOutput indexOutput = new BufferedFileOutput(pkIndexFile);

			// 3-way 머지.
			PrimaryKeyIndexMerger primaryKeyIndexMerger = new PrimaryKeyIndexMerger();
			primaryKeyIndexMerger.merge(prevPkFile, tempPkFile, output, indexOutput, indexInterval);
			output.close();
			indexOutput.close();

			tempPkFile.delete();

		}

	}
}
