package org.fastcatsearch.ir.search;

import org.fastcatsearch.ir.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CompoundReferenceableIndexReader implements ReferenceableReader {
	protected static Logger logger = LoggerFactory.getLogger(CompoundReferenceableIndexReader.class);

    private ReferenceableReader[] readers;
    private CompoundDataRef dataRef;

	public CompoundReferenceableIndexReader(ReferenceableReader[] readers) throws IOException {
        this.readers = readers;
        DataRef[] refs = new DataRef[readers.length];
        for(int i = 0; i < readers.length; i++) {
            refs[i] = readers[i].getRef();
        }
        dataRef = new CompoundDataRef(refs);
    }
	
	@Override
	public DataRef getRef() throws IOException{
		return dataRef;
	}

    @Override
	public void read(int docNo) throws IOException{

        for(int i = 0; i < readers.length; i++) {
            readers[i].read(docNo);
        }
	}
	
	public CompoundReferenceableIndexReader clone() {
        ReferenceableReader[] newReaders = new ReferenceableReader[readers.length];

        for(int i = 0; i < readers.length; i++) {
            newReaders[i] = readers[i].clone();
        }

        try {
            return new CompoundReferenceableIndexReader(newReaders);
        } catch (IOException e) {
            logger.error("", e);
        }
        return null;
    }

    @Override
	public void close() throws IOException {
        for(int i = 0; i < readers.length; i++) {
            readers[i].close();
        }
	}
}
