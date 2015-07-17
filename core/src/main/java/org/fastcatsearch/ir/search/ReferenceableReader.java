package org.fastcatsearch.ir.search;

import org.fastcatsearch.ir.io.DataRef;

import java.io.IOException;

/**
 * Created by swsong on 2015. 7. 17..
 */
public interface ReferenceableReader extends Cloneable {

    public DataRef getRef() throws IOException;

    public void read(int docNo) throws IOException;

    public void close() throws IOException;

    public ReferenceableReader clone();
}
