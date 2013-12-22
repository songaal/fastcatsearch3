package org.fastcatsearch.ir.index;

import java.io.IOException;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface IndexWritable {
	
	public static Logger logger = LoggerFactory.getLogger(IndexWritable.class);

	public int getDocumentCount();
	
	public int addDocument(Document document) throws IRException, IOException;
	
	public void close() throws IOException, IRException;
}
