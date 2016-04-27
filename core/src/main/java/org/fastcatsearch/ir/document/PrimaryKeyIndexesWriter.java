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
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.io.BitSet;
import org.fastcatsearch.ir.io.BytesDataOutput;
import org.fastcatsearch.ir.settings.PrimaryKeySetting;
import org.fastcatsearch.ir.settings.RefSetting;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Formatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * pk맵파일은 pk와 내부문서번호를 맵핑시킨파일인데, 다음번 색인시 추가되는 pk를 이 파일에서 찾아, 삭제문서리스트에 추가해야한다.
 * @author sangwook.song
 * 
 */
public class PrimaryKeyIndexesWriter {
	private static Logger logger = LoggerFactory.getLogger(PrimaryKeyIndexesWriter.class);

	private boolean hasPrimaryKey;
	private BytesDataOutput pkbaos;
	private BitSet deleteSet;
	private LargePrimaryKeyIndexWriter indexWriter;
	private PrimaryKeySetting primaryKeySetting;
	private int[] primaryKeyFieldIdList;

	int MEMORY_LIMIT = 64 * 1024 * 1024; //적절은 64M
	int CHECK_COUNT = 100000;
	int count;
	public PrimaryKeyIndexesWriter(Schema schema, File dir, IndexConfig indexConfig) throws IOException, IRException {
		primaryKeySetting = schema.schemaSetting().getPrimaryKeySetting();
		
		if (primaryKeySetting != null &&  primaryKeySetting.getFieldList() != null) {
			List<RefSetting> refList = primaryKeySetting.getFieldList();
			
			if(refList != null && refList.size() > 0){
				primaryKeyFieldIdList = new int[refList.size()];
				
				int sequence = 0;
				for (RefSetting refSetting : refList) {
					String fieldId = refSetting.getRef();
					primaryKeyFieldIdList[sequence] = schema.getFieldSequence(fieldId);
					logger.debug("pk field [{}]{} >> {}", sequence, fieldId, primaryKeyFieldIdList[sequence]);
					sequence++;
				}
				
				hasPrimaryKey = true;
			}else{
				//pk 없음.
				return;
			}
		}

        indexWriter = new LargePrimaryKeyIndexWriter(dir, IndexFileNames.primaryKeyMap, indexConfig.getPkTermInterval(), indexConfig.getPkBucketSize());

		pkbaos = new BytesDataOutput(1024); //초기 1kb로 시작.
		
        deleteSet = new BitSet(dir, IndexFileNames.docDeleteSet, true);
	}

    public int getDeleteDocCount() {
        return deleteSet.getOnCount();
    }

    public void write(Document document, int localDocNo) throws IOException, IRException {
		
		if(hasPrimaryKey){
			pkbaos.reset();
			for (int fieldId : primaryKeyFieldIdList) {
				Field f = document.get(fieldId);
				if(f == null || f.isNull()){
					throw new IOException("PK field value cannot be null. fieldId="+fieldId+", field="+f+", localDocNo="+localDocNo);
				} else {
					f.writeFixedDataTo(pkbaos);
				}
//				logger.debug("PK >> {}", f);
			}
			
			int preDocNo = indexWriter.put(pkbaos.array(), 0, (int) pkbaos.position(), localDocNo);
			
			if (preDocNo >= 0) {
				if(logger.isTraceEnabled()) {
					logger.trace("PK updated! >> newDocNo[{}] docNo[{}] pk[{}]", localDocNo, preDocNo, new String(pkbaos.array(), 0, (int) pkbaos.position()));
				}
				deleteSet.set(preDocNo);
			}
			
			count++;
			if (count % CHECK_COUNT == 0) {
				long memorySize = indexWriter.checkWorkingMemorySize();
                if(logger.isTraceEnabled()) {
                    logger.trace("PK check #{} pk mem {}", count, Formatter.getFormatSize(memorySize));
                }
				if (memorySize > MEMORY_LIMIT) {
					indexWriter.flush();
				}
			}
		}
	}

    public int getDocNo(BytesDataOutput pk) throws IOException {
        return indexWriter.get(pk.array(), 0, (int) pk.position());
    }
    public void delete(BytesDataOutput pk) throws IOException {
        int preDocNo = indexWriter.get(pk.array(), 0, (int) pk.position());
        if (preDocNo >= 0) {
            deleteSet.set(preDocNo);
        }
    }

	public void close() throws IOException {
		if(indexWriter != null){
			indexWriter.close();
		}
		
		// save delete list
		deleteSet.save();
	}

}
