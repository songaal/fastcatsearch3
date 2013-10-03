package org.fastcatsearch.ir.search;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.field.FieldDataParseException;
import org.fastcatsearch.ir.index.PrimaryKeys;
import org.fastcatsearch.ir.io.BytesDataOutput;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.PrimaryKeySetting;
import org.fastcatsearch.ir.settings.RefSetting;
import org.fastcatsearch.ir.settings.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrimaryKeysToBytesRef {
	private static Logger logger = LoggerFactory.getLogger(PrimaryKeysToBytesRef.class);
	
	private BytesDataOutput pkOutput;
	private int pkSize;
	private FieldSetting[] pkFieldSettingList;
	
	public PrimaryKeysToBytesRef(Schema schma) {
		PrimaryKeySetting primaryKeySetting = schma.schemaSetting().getPrimaryKeySetting();
		List<FieldSetting> fieldSettingList = schma.schemaSetting().getFieldSettingList();
		List<RefSetting> pkRefSettingList = primaryKeySetting.getFieldList();
		pkSize = pkRefSettingList.size();
		pkFieldSettingList = new FieldSetting[pkSize];

		int pkByteSize = 0;
		for (int i = 0; i < pkSize; i++) {
			String fieldId = pkRefSettingList.get(i).getRef();
			int fieldSequence = schma.getFieldSequence(fieldId);
			pkFieldSettingList[i] = fieldSettingList.get(fieldSequence);
			pkByteSize += pkFieldSettingList[i].getByteSize();
		}

		pkOutput = new BytesDataOutput(pkByteSize);
	}

	public BytesRef getBytesRef(PrimaryKeys keys) throws IOException {
		pkOutput.reset();
		
		// multivalue는 불가능.
		for (int i = 0; i < pkSize; i++) {
			String idString = keys.getKey(i);

			Field field = null;
			try {
				field = pkFieldSettingList[i].createPrimaryKeyField(idString);
				field.writeFixedDataTo(pkOutput);
			} catch (FieldDataParseException e) {
				// id 값을 필드로 만드는데 실패했다면 건너뛴다.
				logger.error("ID필드를 만들수 없습니다. {}, {}", idString, e);
			}
		}
		return pkOutput.bytesRef();
		
	}

}
