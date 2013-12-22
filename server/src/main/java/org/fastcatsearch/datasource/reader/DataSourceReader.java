package org.fastcatsearch.datasource.reader;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.DeleteIdSet;

public interface DataSourceReader {
	public DeleteIdSet getDeleteList();
	
	public boolean hasNext() throws IRException;
	
	public Document nextDocument() throws IRException;
	
	public void close();
}
