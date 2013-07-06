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
import java.util.List;
import java.util.Map;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.ir.common.IRFileName;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.document.PrimaryKeyIndexReader;
import org.fastcatsearch.ir.document.PrimaryKeyIndexWriter;
import org.fastcatsearch.ir.document.merge.PrimaryKeyIndexMerger;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.field.FieldDataWriter;
import org.fastcatsearch.ir.io.BufferedFileInput;
import org.fastcatsearch.ir.io.BufferedFileOutput;
import org.fastcatsearch.ir.io.ByteArrayOutput;
import org.fastcatsearch.ir.io.FixedDataOutput;
import org.fastcatsearch.ir.io.IOUtil;
import org.fastcatsearch.ir.io.Input;
import org.fastcatsearch.ir.io.SequencialDataOutput;
import org.fastcatsearch.ir.io.VariableDataOutput;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.GroupIndexSetting;
import org.fastcatsearch.ir.settings.RefSetting;
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
	private StreamOutput groupDataOutput;
	private StreamOutput multiValueOutput;

	private StreamOutput groupMapOutput;
	private StreamOutput groupMapIndexOutput;
	private FieldSetting[] fieldSettingList;
	private PrimaryKeyIndexWriter[] tempKeyIndexList; // temporary key index only use while indexing. Cannot flush this object due
														// to need whole keys while indexing.
	private int[] groupNumber;
	private SequencialDataOutput[] keyOutputList;
	private int revision;
	private boolean isAppend;
	private File baseDir;
	private PrimaryKeyIndexReader[] pkReaderList;
	private int indexInterval;
	private int count;
	private boolean hasMultiValue;

	private List<RefSetting> refSettingList;
	private int[] fieldSequenceList;
	private int fieldSize;
	private ByteArrayOutput keyBuffer;

	public GroupIndexWriter(GroupIndexSetting groupIndexSetting, Map<String, FieldSetting> fieldSettingMap, Map<String, Integer> fieldSequenceMap,
			File dir, IndexConfig indexConfig) throws IOException {
		this(groupIndexSetting, fieldSettingMap, fieldSequenceMap, dir, 0, indexConfig);
	}

	public GroupIndexWriter(GroupIndexSetting groupIndexSetting, Map<String, FieldSetting> fieldSettingMap, Map<String, Integer> fieldSequenceMap,
			File dir, int revision, IndexConfig indexConfig) throws IOException {
		this.revision = revision;
		if (revision > 0) {
			this.isAppend = true;
		}
		this.baseDir = dir;

		refSettingList = groupIndexSetting.getRefList();
		fieldSize = refSettingList.size();
		fieldSettingList = new FieldSetting[fieldSize];
		fieldSequenceList = new int[fieldSize];

		groupDataOutput = new BufferedFileOutput(dir, IRFileName.groupDataFile, isAppend);

		String id = groupIndexSetting.getId();

		if (isAppend) {
			groupMapOutput = new BufferedFileOutput(IRFileName.getRevisionDir(dir, revision), IRFileName.getTempFileName(IRFileName
					.getSuffixFileName(IRFileName.groupKeyMap, id)));
			groupMapIndexOutput = new BufferedFileOutput(IRFileName.getRevisionDir(dir, revision), IRFileName.getTempFileName(IRFileName
					.getSuffixFileName(IRFileName.groupKeyMapIndex, id)));
			// read previous pkmap
			File prevDir = IRFileName.getRevisionDir(dir, revision - 1);

			/* READ Group Info */
			StreamInput groupInfoInput = new BufferedFileInput(prevDir, IRFileName.groupInfoFile);
			int fieldCount = groupInfoInput.readInt();

			pkReaderList = new PrimaryKeyIndexReader[fieldSize];
			long[] dataBasePositionList = new long[fieldSize];
			long[] indexBasePositionList = new long[fieldSize];
			int[] groupKeySize = new int[fieldSize];
			logger.debug("groupInfoInput.size() = {}", groupInfoInput.size());
			for (int i = 0; i < fieldCount; i++) {
				groupKeySize[i] = groupInfoInput.readInt();
				dataBasePositionList[i] = groupInfoInput.readLong();
				indexBasePositionList[i] = groupInfoInput.readLong();
			}
			groupInfoInput.close();

			for (int i = 0; i < fieldCount; i++) {
				long endPos = -1;
				if (i < fieldCount - 1) {
					endPos = dataBasePositionList[i + 1];
				}
				pkReaderList[i] = new PrimaryKeyIndexReader(prevDir, IRFileName.getSuffixFileName(IRFileName.groupKeyMap, id),
						dataBasePositionList[i], endPos, indexBasePositionList[i]);
				// 이전 번호이후부터 그룹번호 부여.
				groupNumber[i] = pkReaderList[i].count();
			}

		} else {
			groupMapOutput = new BufferedFileOutput(IRFileName.getRevisionDir(dir, revision),
					IRFileName.getSuffixFileName(IRFileName.groupKeyMap, id));
			groupMapIndexOutput = new BufferedFileOutput(IRFileName.getRevisionDir(dir, revision), IRFileName.getSuffixFileName(
					IRFileName.groupKeyMapIndex, id));
		}

		tempKeyIndexList = new PrimaryKeyIndexWriter[fieldSize];
		groupNumber = new int[fieldSize];
		keyOutputList = new SequencialDataOutput[fieldSize];

		indexInterval = indexConfig.getPkTermInterval();
		int bucketSize = indexConfig.getPkBucketSize();

		for (int idx = 0; idx < fieldSize; idx++) {
			if (fieldSettingList[idx].isVariableField()) {
				keyOutputList[idx] = new VariableDataOutput(dir, IRFileName.getSuffixFileName(IRFileName.groupKeyFile, id, Integer.toString(idx)),
						isAppend);
			} else {
				keyOutputList[idx] = new FixedDataOutput(dir, IRFileName.getSuffixFileName(IRFileName.groupKeyFile, id, Integer.toString(idx)),
						isAppend);
			}
			tempKeyIndexList[idx] = new PrimaryKeyIndexWriter(null, null, indexInterval, bucketSize);

			RefSetting rs = refSettingList.get(idx);

			String fieldId = rs.getRef();
			FieldSetting fieldSetting = fieldSettingMap.get(fieldId);
			fieldSettingList[idx] = fieldSetting;
			if (fieldSetting.isMultiValue()) {
				hasMultiValue = true;
			}
			fieldSequenceList[idx] = fieldSequenceMap.get(fieldId);
		}

		if (hasMultiValue) {
			multiValueOutput = new BufferedFileOutput(dir, IRFileName.getMultiValueSuffixFileName(IRFileName.groupDataFile, id), isAppend);
		}

		keyBuffer = new ByteArrayOutput();

	}

	public void write(Document document) throws IOException {

		for (int idx = 0; idx < fieldSize; idx++) {
			int k = fieldSequenceList[idx];
			Field field = document.get(k);
			if (field == null) {
				if (fieldSettingList[idx].isMultiValue()) {
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
						while (writer.write(keyBuffer)) {
							groupNo = writeGroupKey(idx, keyBuffer);
							multiValueOutput.writeInt(groupNo);

							keyBuffer.reset();
						}
					} else {
						groupDataOutput.writeLong(-1);
					}

				} else {
					field.writeDataTo(keyBuffer);
					groupNo = writeGroupKey(idx, keyBuffer);
					groupDataOutput.writeInt(groupNo);
				}
			}
		}

		count++;
	}

	private int writeGroupKey(int idx, ByteArrayOutput keyBuffer) throws IOException {
		int groupNo = -1;
		if (isAppend) {
			// find key at previous append's pkmap
			groupNo = pkReaderList[idx].get(keyBuffer.array(), 0, (int) keyBuffer.position());
			if (groupNo == -1) {
				groupNo = tempKeyIndexList[idx].get(keyBuffer.array(), 0, (int) keyBuffer.position());
			}
		} else {
			groupNo = tempKeyIndexList[idx].get(keyBuffer.array(), 0, (int) keyBuffer.position());
		}
		if (groupNo == -1) {
			groupNo = groupNumber[idx]++;
			// write key index
			tempKeyIndexList[idx].put(keyBuffer.array(), 0, (int) keyBuffer.position(), groupNo);
			keyOutputList[idx].write(keyBuffer.array(), 0, (int) keyBuffer.position());
		}
		return groupNo;
	}

	public void flush() throws IOException {
		groupDataOutput.flush();
		if (hasMultiValue) {
			multiValueOutput.flush();
		}
	}

	public void close() throws IOException {
		groupDataOutput.close();
		if (hasMultiValue) {
			multiValueOutput.close();
		}

		if (count <= 0) {
			for (int idx = 0; idx < fieldSize; idx++) {
				keyOutputList[idx].close();
			}
			groupMapOutput.close();
			groupMapIndexOutput.close();

			if (isAppend) {
				File tempGroupMapIndexFile = new File(IRFileName.getRevisionDir(baseDir, revision),
						IRFileName.getTempFileName(IRFileName.groupKeyMapIndex));
				tempGroupMapIndexFile.delete();
				File tempPkFile = new File(IRFileName.getRevisionDir(baseDir, revision), IRFileName.getTempFileName(IRFileName.groupKeyMap));
				tempPkFile.delete();
			}
			return;
		}

		/*
		 * Write Group Info File
		 */
		long[] currentDataPosition = new long[fieldSize];

		StreamOutput groupInfoOutput = new BufferedFileOutput(IRFileName.getRevisionDir(baseDir, revision), IRFileName.groupInfoFile);
		groupInfoOutput.writeInt(fieldSize);

		for (int idx = 0; idx < fieldSize; idx++) {

			// keycount, key start file position, dataBasePosition, indexBasePosition을 기록
			int keyCount = tempKeyIndexList[idx].count();
			// long keyPosition = groupKeyOutputChannel.position();
			logger.debug("####### groupMapOutput = {}", groupMapOutput);
			long dataBasePosition = groupMapOutput.position();
			currentDataPosition[idx] = dataBasePosition;
			long indexBasePosition = groupMapIndexOutput.position();
			logger.debug("group-{} keycount = {}, dataBasePosition={}, indexBasePosition={}", idx, keyCount, dataBasePosition, indexBasePosition);
			groupInfoOutput.writeInt(keyCount);
			groupInfoOutput.writeLong(dataBasePosition);
			groupInfoOutput.writeLong(indexBasePosition);

			keyOutputList[idx].close();

			// no need to use anymore.
			// if merging, we create memory pkmap again.
			tempKeyIndexList[idx].setDestination(groupMapOutput, groupMapIndexOutput);
			tempKeyIndexList[idx].write();
		}

		groupMapOutput.close();
		groupMapIndexOutput.close();

		if (isAppend) {
			File tempGroupMapIndexkFile = new File(IRFileName.getRevisionDir(baseDir, revision),
					IRFileName.getTempFileName(IRFileName.groupKeyMapIndex));
			tempGroupMapIndexkFile.delete();

			// right after group setting count
			groupInfoOutput.seek(IOUtil.SIZE_OF_INT);

			// read previous pkinfo
			File prevDir = IRFileName.getRevisionDir(baseDir, revision - 1);
			StreamInput prevGroupInfoInput = new BufferedFileInput(prevDir, IRFileName.groupInfoFile);
			int fieldCount = prevGroupInfoInput.readInt();

			long[] prevDataPosition = new long[fieldSize];

			for (int i = 0; i < fieldCount; i++) {
				prevGroupInfoInput.readInt();// key count
				prevDataPosition[i] = prevGroupInfoInput.readLong();
				prevGroupInfoInput.readLong();// index position

				pkReaderList[i].close();
			}
			prevGroupInfoInput.close();

			// pkindex merge
			File tempPkFile = new File(IRFileName.getRevisionDir(baseDir, revision), IRFileName.getTempFileName(IRFileName.groupKeyMap));
			File prevPkFile = new File(IRFileName.getRevisionDir(baseDir, revision - 1), IRFileName.groupKeyMap);

			StreamOutput output = new BufferedFileOutput(IRFileName.getRevisionDir(baseDir, revision), IRFileName.groupKeyMap);
			StreamOutput indexOutput = new BufferedFileOutput(IRFileName.getRevisionDir(baseDir, revision), IRFileName.groupKeyMapIndex);

			PrimaryKeyIndexMerger primaryKeyIndexMerger = new PrimaryKeyIndexMerger();
			for (int i = 0; i < fieldSize; i++) {
				long pos1 = output.position();
				long pos2 = indexOutput.position();
				primaryKeyIndexMerger.merge(prevPkFile, prevDataPosition[i], tempPkFile, currentDataPosition[i], output, indexOutput, indexInterval);
				int keyCount = primaryKeyIndexMerger.getKeyCount();

				// write group.info
				groupInfoOutput.writeInt(keyCount);
				groupInfoOutput.writeLong(pos1);
				groupInfoOutput.writeLong(pos2);
				logger.debug("group-{}-merge keyCount = {}, pos1 = {}, pos2 = {}", i, keyCount, pos1, pos2);
			}

			output.close();
			indexOutput.close();

			// tempPkFile.delete();

		}

		groupInfoOutput.close();

	}
}
