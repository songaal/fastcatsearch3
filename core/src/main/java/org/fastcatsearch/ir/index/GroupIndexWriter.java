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
	private IndexOutput groupDataOutput;
	private IndexOutput multiValueOutput;

	private IndexOutput groupMapOutput;
	private IndexOutput groupMapIndexOutput;
	
	private PrimaryKeyIndexWriter memoryKeyIndex;
	
	private int groupNumber;
	private SequencialDataOutput keyOutput;
	
	private int revision;
	private boolean isAppend;
	private File baseDir;
	private File revisionDir;
	private PrimaryKeyIndexReader prevPkReader; //이전 pk reader 리스트. 증분색인시에 사용됨.
	private int indexInterval;
	private int count;
	private boolean isMultiValue;

	private int fieldSize;
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

		groupDataOutput = new BufferedFileOutput(dir, IndexFileNames.getSuffixFileName(IndexFileNames.groupIndexFile, id), isAppend);

		if (isAppend) {
			
			// read previous pkmap
			File prevDir = IndexFileNames.getRevisionDir(dir, revision - 1);

			PrimaryKeyIndexReader prevPkReader = new PrimaryKeyIndexReader(prevDir, IndexFileNames.getSuffixFileName(IndexFileNames.groupKeyMap, id));
			groupNumber = prevPkReader.count();
			prevPkReader.close();
			
			
			/* READ Group Info */
//			IndexInput groupInfoInput = new BufferedFileInput(prevDir, IndexFileNames.groupInfoFile);
//			int fieldCount = groupInfoInput.readInt();
//
//			if(fieldCount != fieldSize){
//				throw new IRException("색인된 색인필드갯수와 스키마의 색인필드갯수가 다릅니다. schema="+fieldSize+", indexed="+fieldCount);
//			}
//			prevPkReaderList = new PrimaryKeyIndexReader[fieldSize];
//			long[] dataBasePositionList = new long[fieldSize];
//			long[] indexBasePositionList = new long[fieldSize];
//			int[] groupKeySize = new int[fieldSize];
//			logger.debug("groupInfoInput.size() = {}", groupInfoInput.length());
//			for (int i = 0; i < fieldCount; i++) {
//				groupKeySize[i] = groupInfoInput.readInt();
//				dataBasePositionList[i] = groupInfoInput.readLong();
//				indexBasePositionList[i] = groupInfoInput.readLong();
//			}
//			groupInfoInput.close();

//			for (int i = 0; i < fieldCount; i++) {
//				long endPos = -1;
//				if (i < fieldCount - 1) {
//					endPos = dataBasePositionList[i + 1];
//				}
//				prevPkReaderList[i] = new PrimaryKeyIndexReader(prevDir, IndexFileNames.getSuffixFileName(IndexFileNames.groupKeyMap, id),
//						dataBasePositionList[i], endPos, indexBasePositionList[i]);
//				// 이전 번호이후부터 그룹번호 부여.
//				groupNumber[i] = prevPkReaderList[i].count();
//			}

		}

		indexInterval = indexConfig.getPkTermInterval();
		int bucketSize = indexConfig.getPkBucketSize();

		
		String fieldId = groupIndexSetting.getRef();
		fieldSequence = fieldSequenceMap.get(fieldId);
		FieldSetting refFieldSetting = fieldSettingMap.get(fieldId);
		isMultiValue = refFieldSetting.isMultiValue();
		
		if (refFieldSetting.isVariableField()) {
			keyOutput = new VariableDataOutput(dir, IndexFileNames.getSuffixFileName(IndexFileNames.groupKeyFile, id), isAppend);
		} else {
			keyOutput = new FixedDataOutput(dir, IndexFileNames.getSuffixFileName(IndexFileNames.groupKeyFile, id), isAppend);
		}
		memoryKeyIndex = new PrimaryKeyIndexWriter(null, null, indexInterval, bucketSize);

		if (isMultiValue) {
			multiValueOutput = new BufferedFileOutput(dir, IndexFileNames.getMultiValueSuffixFileName(IndexFileNames.groupIndexFile, id), isAppend);
		}

		keyBuffer = new BytesDataOutput();

	}

	public void write(Document document) throws IOException {

		Field field = document.get(fieldSequence);
		if (field == null) {
			if (isMultiValue) {
				groupDataOutput.writeLong(-1L);
			} else {
				groupDataOutput.writeInt(-1);
			}
		} else {
			int groupNo = -1;
			if (field.isMultiValue()) {
				long ptr = multiValueOutput.position();
				FieldDataWriter writer = field.getDataWriter();
				int multiValueCount = writer.count();
	
				if (multiValueCount > 0) {
					groupDataOutput.writeLong(ptr);
					multiValueOutput.writeVInt(multiValueCount);
					keyBuffer.reset();
					while (writer.write(keyBuffer)) {
						groupNo = writeGroupKey(keyBuffer);
						multiValueOutput.writeInt(groupNo);
	
						keyBuffer.reset();
					}
				} else {
					groupDataOutput.writeLong(-1);
				}
	
			} else {
				keyBuffer.reset();
				field.writeDataTo(keyBuffer);
				groupNo = writeGroupKey(keyBuffer);
				groupDataOutput.writeInt(groupNo);
			}
		}

		count++;
	}

	/*
	 *  idx : 인덱스 내부필드 순차번호
	 */
	private int writeGroupKey(BytesDataOutput keyBuffer) throws IOException {
		int groupNo = -1;
		if (isAppend) {
			// find key at previous append's pkmap
			groupNo = prevPkReader.get(keyBuffer.array(), 0, (int) keyBuffer.position());
			if (groupNo == -1) {
				groupNo = memoryKeyIndex.get(keyBuffer.array(), 0, (int) keyBuffer.position());
			}
		} else {
			groupNo = memoryKeyIndex.get(keyBuffer.array(), 0, (int) keyBuffer.position());
		}
		if (groupNo == -1) {
			groupNo = groupNumber++;
			// write key index
			memoryKeyIndex.put(keyBuffer.array(), 0, (int) keyBuffer.position(), groupNo);
			keyOutput.write(keyBuffer.array(), 0, (int) keyBuffer.position());
			
			String str = "";
			for (int i = 0; i < keyBuffer.position(); i++) {
				str += (keyBuffer.array()[i] +",");
			}
			logger.debug("write group key field [{}] size[{}] >> gr[{}]", str, keyBuffer.position(), groupNo);
		}
		return groupNo;
	}

	public void flush() throws IOException {
		groupDataOutput.flush();
		if (isMultiValue) {
			multiValueOutput.flush();
		}
	}

	public void close() throws IOException {
		groupDataOutput.close();
		if (isMultiValue) {
			multiValueOutput.close();
		}

		if (count <= 0) {
			keyOutput.close();

			if (isAppend) {
				File tempGroupMapIndexFile = new File(IndexFileNames.getRevisionDir(baseDir, revision),
						IndexFileNames.getTempFileName(IndexFileNames.groupKeyMapIndex));
				tempGroupMapIndexFile.delete();
				File tempPkFile = new File(IndexFileNames.getRevisionDir(baseDir, revision), IndexFileNames.getTempFileName(IndexFileNames.groupKeyMap));
				tempPkFile.delete();
				
				prevPkReader.close();
			}
			return;
		}

		File tempPkFile = new File(revisionDir, IndexFileNames.getTempFileName(IndexFileNames.getSuffixFileName(IndexFileNames.groupKeyMap, indexId)));
		File tempPkIndexFile = new File(revisionDir, IndexFileNames.getTempFileName(IndexFileNames.getSuffixFileName(IndexFileNames.groupKeyMapIndex, indexId)));
		File pkFile = new File(revisionDir, IndexFileNames.getTempFileName(IndexFileNames.getSuffixFileName(IndexFileNames.groupKeyMap, indexId)));
		File pkIndexFile = new File(revisionDir, IndexFileNames.getTempFileName(IndexFileNames.getSuffixFileName(IndexFileNames.groupKeyMapIndex, indexId)));
		
		if(isAppend){
			//머징을 위해 일단 TEMP파일로 생성한다.
			groupMapOutput = new BufferedFileOutput(tempPkFile);
			groupMapIndexOutput = new BufferedFileOutput(tempPkIndexFile);
		}else{
			groupMapOutput = new BufferedFileOutput(revisionDir, IndexFileNames.getSuffixFileName(IndexFileNames.groupKeyMap, indexId));
			groupMapIndexOutput = new BufferedFileOutput(revisionDir, IndexFileNames.getSuffixFileName(IndexFileNames.groupKeyMapIndex, indexId));
		}
		
		int keyCount = memoryKeyIndex.count();
		//별도 저장필요없음.나중에 pk에서 키 갯수읽으면 됨.
		keyOutput.close();
		memoryKeyIndex.setDestination(groupMapOutput, groupMapIndexOutput);
		memoryKeyIndex.write();
		groupMapOutput.close();
		groupMapIndexOutput.close();
		
		/*
		 * Write Group Info File
		 */
//		long[] currentDataPosition = new long[fieldSize];

//		IndexOutput groupInfoOutput = new BufferedFileOutput(IndexFileNames.getRevisionDir(baseDir, revision), IndexFileNames.groupInfoFile);
//		groupInfoOutput.writeInt(fieldSize);

//		for (int idx = 0; idx < fieldSize; idx++) {

			// keycount, key start file position, dataBasePosition, indexBasePosition을 기록
//			int keyCount = memoryKeyIndex.count();
			// long keyPosition = groupKeyOutputChannel.position();
//			logger.debug("####### groupMapOutput = {}", groupMapOutput);
//			long dataBasePosition = groupMapOutput.position();
//			currentDataPosition[idx] = dataBasePosition;
//			long indexBasePosition = groupMapIndexOutput.position();
//			logger.debug("group-{}[{}] keycount = {}, dataBasePosition={}, indexBasePosition={}", idx, indexId, keyCount, dataBasePosition, indexBasePosition);
//			groupInfoOutput.writeInt(keyCount);
//			groupInfoOutput.writeLong(dataBasePosition);
//			groupInfoOutput.writeLong(indexBasePosition);

//			keyOutput.close();

			// no need to use anymore.
			// if merging, we create memory pkmap again.
//			memoryKeyIndex.setDestination(groupMapOutput, groupMapIndexOutput);
//			memoryKeyIndex.write();
//		}

		

		if (isAppend) {
			//임시 map index파일 삭제.
//			File tempGroupMapIndexFile = new File(IndexFileNames.getRevisionDir(baseDir, revision), IndexFileNames.getTempFileName(IndexFileNames.getSuffixFileName(IndexFileNames.groupKeyMapIndex, indexId)));
			tempPkIndexFile.delete();

			prevPkReader.close();
			
			// right after group setting count
//			groupInfoOutput.seek(IOUtil.SIZE_OF_INT);

			// read previous pkinfo
			
//			IndexInput prevGroupInfoInput = new BufferedFileInput(prevDir, IndexFileNames.groupInfoFile);
//			int fieldCount = prevGroupInfoInput.readInt();
//
//			long[] prevDataPosition = new long[fieldSize];

//			for (int i = 0; i < fieldCount; i++) {
//				prevGroupInfoInput.readInt();// key count
//				prevDataPosition[i] = prevGroupInfoInput.readLong();
//				prevGroupInfoInput.readLong();// index position

//				prevPkReader.close();
//			}

//			prevGroupInfoInput.close();

			// pkindex merge
			File prevDir = IndexFileNames.getRevisionDir(baseDir, revision - 1);
			File prevPkFile = new File(prevDir, IndexFileNames.groupKeyMap);

			IndexOutput output = new BufferedFileOutput(pkFile);
			IndexOutput indexOutput = new BufferedFileOutput(pkIndexFile);

			//3-way 머지.
			PrimaryKeyIndexMerger primaryKeyIndexMerger = new PrimaryKeyIndexMerger();
			primaryKeyIndexMerger.merge(prevPkFile, tempPkFile, output, indexOutput, indexInterval);
//			int keyCount2 = primaryKeyIndexMerger.getKeyCount();
			output.close();
			indexOutput.close();
			
//			for (int i = 0; i < fieldSize; i++) {
//				long pos1 = output.position();
//				long pos2 = indexOutput.position();
				
			
//				primaryKeyIndexMerger.merge(prevPkFile, tempPkFile, output, indexOutput, indexInterval);
//				int keyCount2 = primaryKeyIndexMerger.getKeyCount();

				// write group.info
//				groupInfoOutput.writeInt(keyCount);
//				groupInfoOutput.writeLong(pos1);
//				groupInfoOutput.writeLong(pos2);
//				logger.debug("group[{}] merge keyCount = {}, pos1 = {}, pos2 = {}", indexId, keyCount, pos1, pos2);
//			}

			// tempPkFile.delete();

		}

//		groupInfoOutput.close();

	}
}
