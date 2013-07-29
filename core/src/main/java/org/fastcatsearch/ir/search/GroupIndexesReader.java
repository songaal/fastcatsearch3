package org.fastcatsearch.ir.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.settings.GroupIndexSetting;
import org.fastcatsearch.ir.settings.Schema;

public class GroupIndexesReader extends SelectableIndexesReader<GroupIndexReader, GroupIndexSetting> {
	
	public GroupIndexesReader(){ }
	
	public GroupIndexesReader(Schema schema, File dir, int revision) throws IOException, IRException{
		indexSettingList = schema.schemaSetting().getGroupIndexSettingList();
		int indexCount = indexSettingList.size();
		
		//색인파일열기.
		readerList = new ArrayList<GroupIndexReader>(indexCount);
		for (int i = 0; i < indexCount; i++) {
			GroupIndexSetting setting = indexSettingList.get(i);
			readerList.add(new GroupIndexReader(setting, schema.fieldSettingMap(), dir));
		}
		
	}
	
	@Override
	public GroupIndexesReader clone(){
		GroupIndexesReader reader = new GroupIndexesReader();
		reader.indexSettingList = indexSettingList;
    	reader.readerList = new ArrayList<GroupIndexReader>(readerList.size());
    	for (GroupIndexReader r : readerList) {
    		reader.readerList.add(r.clone());
		}
    	
    	return reader;
    }
	
	@Override
	protected GroupIndexReader cloneReader(int sequence) {
		return readerList.get(sequence).clone();
	}

}
