package org.fastcatsearch.ir.index;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.settings.FieldIndexSetting;
import org.fastcatsearch.ir.settings.GroupIndexSetting;
import org.fastcatsearch.ir.settings.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 그룹인덱스에 대한 색인클래스.
 * */
public class GroupIndexesWriter {
	private static Logger logger = LoggerFactory.getLogger(GroupIndexesWriter.class);
	private GroupIndexWriter[] groupIndexWriterList;
	private int indexSize;
	
	public GroupIndexesWriter(Schema schema, File dir, int revision, IndexConfig indexConfig) throws IOException, IRException {
		List<GroupIndexSetting> groupIndexSettingList = schema.schemaSetting().getGroupIndexSettingList();
		indexSize = groupIndexSettingList.size();
		groupIndexWriterList = new GroupIndexWriter[indexSize];
		int i = 0;
		for(GroupIndexSetting groupIndexSetting : groupIndexSettingList){
			groupIndexWriterList[i++] = new GroupIndexWriter(groupIndexSetting, schema.fieldSettingMap(), schema.fieldSequenceMap(), dir, revision, indexConfig);
		}
		
	}
	
	
	public void write(Document document) throws IOException{
		for (int i = 0; i < indexSize; i++) {
			groupIndexWriterList[i].write(document);
		}
	}
	
	public void flush() throws IOException{
		for (int i = 0; i < indexSize; i++) {
			groupIndexWriterList[i].flush();
		}
	}
	
	public void close() throws IOException{
		for (int i = 0; i < indexSize; i++) {
			groupIndexWriterList[i].close();
		}
	}
}
