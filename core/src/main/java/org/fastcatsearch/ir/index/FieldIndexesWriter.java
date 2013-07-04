package org.fastcatsearch.ir.index;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.settings.FieldIndexSetting;
import org.fastcatsearch.ir.settings.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 여러 필드인덱스에 대한 색인클래스.
 * */
public class FieldIndexesWriter {
	private static Logger logger = LoggerFactory.getLogger(FieldIndexesWriter.class);
	private FieldIndexWriter[] fieldIndexWriterList;
	private int indexSize;
	
	public FieldIndexesWriter(Schema schema, File dir, boolean isAppend) throws IOException {
		List<FieldIndexSetting> fieldIndexSettingList = schema.schemaSetting().getFieldIndexSettingList();
		indexSize = fieldIndexSettingList.size();
		int i = 0;
		for(FieldIndexSetting fieldIndexSetting : fieldIndexSettingList){
			fieldIndexWriterList[i++] = new FieldIndexWriter(fieldIndexSetting, schema.fieldSettingMap(), schema.fieldSequenceMap(), dir, isAppend);
		}
		
	}
	
	
	public void write(Document document) throws IOException{
		for (int i = 0; i < indexSize; i++) {
			fieldIndexWriterList[i].write(document);
		}
	}
	
	public void flush() throws IOException{
		for (int i = 0; i < indexSize; i++) {
			fieldIndexWriterList[i].flush();
		}
	}
	
	public void close() throws IOException{
		for (int i = 0; i < indexSize; i++) {
			fieldIndexWriterList[i].close();
		}
	}
}
