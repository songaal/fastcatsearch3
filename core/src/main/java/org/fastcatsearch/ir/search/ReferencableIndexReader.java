package org.fastcatsearch.ir.search;

import java.io.IOException;

import org.fastcatsearch.ir.io.DataRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ReferencableIndexReader implements Cloneable {
	protected static Logger logger = LoggerFactory.getLogger(ReferencableIndexReader.class);
	
	public abstract int getRefCount();
	
	public abstract DataRef getRef(int sequence) throws IOException;
	
	public abstract DataRef[] getRef() throws IOException;
	
	public abstract void read(int docNo) throws IOException;
	
	public abstract void read(int docNo, int sequence) throws IOException;
	
	public abstract ReferencableIndexReader clone();
	
	public abstract void close() throws IOException;
}
