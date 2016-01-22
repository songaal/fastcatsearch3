package org.fastcatsearch.ir.index;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public interface IndexWritable {
	
	public static Logger logger = LoggerFactory.getLogger(IndexWritable.class);

	public int getDocumentCount();
	
	public int addDocument(Document document) throws IRException, IOException;

//	void deleteDocument(String pkVal) throws IRException, IOException;

	public DataInfo.SegmentInfo close() throws IOException, IRException;
}
