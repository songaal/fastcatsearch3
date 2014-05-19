package org.fastcatsearch.ir.index;

import java.io.IOException;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.document.Document;

public interface SingleIndexWriter {
	
	public void write(Document doc, int docNo) throws IRException, IOException;
	
	public void flush() throws IRException;
	
	public void close() throws IRException, IOException;
	
}
